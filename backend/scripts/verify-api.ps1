param(
    [string]$BaseUrl = "http://localhost:8081"
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($env:DEMO_PASSWORD)) {
    throw "Set DEMO_PASSWORD to the value used by the backend demo profile."
}

function Invoke-Json {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Body,
        [string]$Token
    )
    $headers = @{}
    if ($Token) { $headers.Authorization = "Bearer $Token" }
    $parameters = @{
        Method = $Method
        Uri = "$BaseUrl$Path"
        Headers = $headers
        ContentType = "application/json"
    }
    if ($null -ne $Body) { $parameters.Body = ($Body | ConvertTo-Json -Depth 8) }
    Invoke-RestMethod @parameters
}

function Get-Status {
    param([scriptblock]$Operation)
    try {
        $response = & $Operation
        if ($response.StatusCode) { return [int]$response.StatusCode }
        return 200
    } catch {
        if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
            return [int]$_.Exception.Response.StatusCode
        }
        throw
    }
}

function Assert-Equal {
    param([object]$Actual, [object]$Expected, [string]$Message)
    if ($Actual -ne $Expected) {
        throw "$Message. Expected '$Expected', received '$Actual'."
    }
    Write-Host "[PASS] $Message" -ForegroundColor Green
}

function Login {
    param([string]$Username)
    Invoke-Json POST "/api/auth/login" @{
        username = $Username
        password = $env:DEMO_PASSWORD
    } $null
}

$health = Invoke-RestMethod "$BaseUrl/actuator/health"
Assert-Equal $health.status "UP" "Application health"

$swaggerStatus = Get-Status { Invoke-WebRequest "$BaseUrl/v3/api-docs" -UseBasicParsing }
Assert-Equal $swaggerStatus 200 "OpenAPI document is accessible"

$admin = Login "admin.demo"
$doctor = Login "medecin.demo"
$secretary = Login "secretaire.demo"
Assert-Equal $admin.user.role "ADMIN" "ADMIN database login"
Assert-Equal $doctor.user.role "MEDECIN" "MEDECIN database login"
Assert-Equal $secretary.user.role "SECRETAIRE" "SECRETAIRE database login"

$unauthorized = Get-Status { Invoke-WebRequest "$BaseUrl/api/admin/utilisateurs" -UseBasicParsing }
Assert-Equal $unauthorized 401 "Unauthenticated request is rejected"

$forbidden = Get-Status {
    Invoke-WebRequest "$BaseUrl/api/admin/utilisateurs" -Headers @{ Authorization = "Bearer $($secretary.accessToken)" } -UseBasicParsing
}
Assert-Equal $forbidden 403 "Cross-role request is forbidden"

$notFound = Get-Status {
    Invoke-WebRequest "$BaseUrl/api/patients/999999" -Headers @{ Authorization = "Bearer $($admin.accessToken)" } -UseBasicParsing
}
Assert-Equal $notFound 404 "Unknown resource returns 404"

$invalidPatient = Get-Status {
    Invoke-WebRequest -Method Post "$BaseUrl/api/patients" `
        -Headers @{ Authorization = "Bearer $($secretary.accessToken)" } `
        -ContentType "application/json" `
        -Body (@{ nom="Test"; prenom="Invalide"; dateNaissance=(Get-Date).AddDays(1).ToString("yyyy-MM-dd") } | ConvertTo-Json) `
        -UseBasicParsing
}
Assert-Equal $invalidPatient 400 "Bean validation returns 400"

$suffix = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$newPatientResponse = Invoke-WebRequest -Method Post "$BaseUrl/api/patients" `
    -Headers @{ Authorization = "Bearer $($secretary.accessToken)" } `
    -ContentType "application/json" `
    -Body (@{
        nom="Durand"; prenom="Camille"; dateNaissance="1990-05-14"; sexe="F"
        email="camille.durand.$suffix@example.test"; telephone="+33600000000"; groupeSanguin="A+"
    } | ConvertTo-Json) -UseBasicParsing
Assert-Equal ([int]$newPatientResponse.StatusCode) 201 "Patient creation returns 201"
$newPatient = $newPatientResponse.Content | ConvertFrom-Json
$record = Invoke-Json GET "/api/dossiers/patient/$($newPatient.id)" $null $admin.accessToken
Assert-Equal $record.patientId $newPatient.id "Patient creation also creates a medical record"

$availabilityConflict = Get-Status {
    Invoke-WebRequest -Method Post "$BaseUrl/api/disponibilites" `
        -Headers @{ Authorization = "Bearer $($secretary.accessToken)" } `
        -ContentType "application/json" `
        -Body (@{ medecinId=1; jourSemaine="LUNDI"; heureDebut="09:00:00"; heureFin="10:00:00" } | ConvertTo-Json) `
        -UseBasicParsing
}
Assert-Equal $availabilityConflict 409 "Availability overlap returns 409"

$today = (Get-Date).Date
$daysUntilMonday = (([int][DayOfWeek]::Monday - [int]$today.DayOfWeek + 7) % 7)
if ($daysUntilMonday -eq 0) { $daysUntilMonday = 7 }
$nextMonday = $today.AddDays($daysUntilMonday).AddHours(9).ToString("yyyy-MM-ddTHH:mm:ss")
$appointmentConflict = Get-Status {
    Invoke-WebRequest -Method Post "$BaseUrl/api/rendez-vous" `
        -Headers @{ Authorization = "Bearer $($secretary.accessToken)" } `
        -ContentType "application/json" `
        -Body (@{ patientId=2; medecinId=1; dateHeure=$nextMonday; dureeMinutes=30; motif="Conflit volontaire" } | ConvertTo-Json) `
        -UseBasicParsing
}
Assert-Equal $appointmentConflict 409 "Appointment conflict returns 409"

$allergyWithoutOverride = Get-Status {
    Invoke-WebRequest -Method Put "$BaseUrl/api/prescriptions/2" `
        -Headers @{ Authorization = "Bearer $($doctor.accessToken)" } `
        -ContentType "application/json" `
        -Body (@{
            rendezVousId=8; instructions="Test sans dérogation"; overrideAllergie=$false
            lignes=@(@{ medicamentId=4; dosage="1 comprimé matin et soir"; dureeJours=6; quantite=12 })
        } | ConvertTo-Json -Depth 6) -UseBasicParsing
}
Assert-Equal $allergyWithoutOverride 409 "Allergy conflict requires explicit override"

$override = Invoke-Json PUT "/api/prescriptions/2" @{
    rendezVousId=8
    instructions="Dérogation clinique confirmée"
    overrideAllergie=$true
    motifOverrideAllergie="Bénéfice clinique confirmé après évaluation médicale."
    lignes=@(@{ medicamentId=4; dosage="1 comprimé matin et soir"; dureeJours=6; quantite=12 })
} $doctor.accessToken
Assert-Equal $override.overrideAllergie $true "Doctor allergy override succeeds"

$invoiceResponse = Invoke-WebRequest -Method Post "$BaseUrl/api/factures" `
    -Headers @{ Authorization = "Bearer $($secretary.accessToken)" } `
    -ContentType "application/json" `
    -Body (@{ rendezVousId=10; remise=0; tauxTva=20 } | ConvertTo-Json) -UseBasicParsing
Assert-Equal ([int]$invoiceResponse.StatusCode) 201 "Invoice creation returns 201"
$invoice = $invoiceResponse.Content | ConvertFrom-Json
Assert-Equal ([decimal]$invoice.total) ([decimal]120.00) "Invoice calculation includes configured consultation and VAT"

$paymentResponse = Invoke-WebRequest -Method Post "$BaseUrl/api/factures/$($invoice.id)/paiements" `
    -Headers @{ Authorization = "Bearer $($secretary.accessToken)" } `
    -ContentType "application/json" `
    -Body (@{ montant=50.00; mode="CARTE"; referencePaiement="SMOKE-PAYMENT" } | ConvertTo-Json) -UseBasicParsing
Assert-Equal ([int]$paymentResponse.StatusCode) 201 "Partial payment returns 201"
$payment = $paymentResponse.Content | ConvertFrom-Json
Assert-Equal $payment.statutFacture "PARTIELLEMENT_PAYEE" "Partial payment updates invoice status"

$excessivePayment = Get-Status {
    Invoke-WebRequest -Method Post "$BaseUrl/api/factures/$($invoice.id)/paiements" `
        -Headers @{ Authorization = "Bearer $($secretary.accessToken)" } `
        -ContentType "application/json" `
        -Body (@{ montant=1000.00; mode="ESPECES" } | ConvertTo-Json) -UseBasicParsing
}
Assert-Equal $excessivePayment 409 "Payment above remaining balance returns 409"

$notificationStatus = Get-Status {
    Invoke-WebRequest -Method Post "$BaseUrl/api/notifications" `
        -Headers @{ Authorization = "Bearer $($admin.accessToken)" } `
        -ContentType "application/json" `
        -Body (@{
            rendezVousId=2; destinataire="smoke-test@medicareplus.test"; canal="EMAIL"
            type="RAPPEL_RDV"; contenu="Test de notification MediCare+"
        } | ConvertTo-Json) -UseBasicParsing
}
Assert-Equal $notificationStatus 202 "Asynchronous notification is accepted"
Start-Sleep -Seconds 2
$notificationHistory = Invoke-Json GET "/api/notifications?size=50" $null $admin.accessToken
$persistedNotification = $notificationHistory.content | Where-Object { $_.destinataire -eq "smoke-test@medicareplus.test" }
Assert-Equal ($null -ne $persistedNotification) $true "Notification outcome is persisted in MySQL"

Write-Host "MediCare+ API verification completed successfully." -ForegroundColor Cyan
