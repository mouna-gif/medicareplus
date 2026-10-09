package com.medicareplus.api;

import com.medicareplus.domain.Enums.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

public final class DomainDtos {
    private DomainDtos() {}

    public record UtilisateurRequest(@NotBlank String username, @NotBlank @Email String email,
        @NotBlank @Size(min = 12) String password, @NotBlank String nom, @NotBlank String prenom, @NotNull Role role) {}
    public record UtilisateurView(Long id, String username, String email, String nom, String prenom, Role role, boolean actif) {}
    public record MedecinRequest(@NotNull Long utilisateurId, @NotBlank String matricule, @NotBlank String specialite, String telephone) {}
    public record MedecinView(Long id, Long utilisateurId, String nomComplet, String matricule, String specialite, String telephone) {}

    public record PatientRequest(@NotBlank String nom, @NotBlank String prenom, @NotNull @Past LocalDate dateNaissance,
        String sexe, @Email String email, String telephone, String adresse, String groupeSanguin) {}
    public record PatientView(Long id, String numeroPatient, String nom, String prenom, LocalDate dateNaissance,
        String sexe, String email, String telephone, String adresse, String groupeSanguin, boolean archive) {}
    public record DossierMedicalRequest(String antecedents, String allergies, String notes) {}
    public record DossierMedicalView(Long id, Long patientId, String patient, String antecedents, String allergies, String notes) {}
    public record HistoriqueMedicalView(LocalDateTime date, String type, String description, String statut) {}

    public record DisponibiliteRequest(@NotNull Long medecinId, @NotNull JourSemaine jourSemaine,
        @NotNull LocalTime heureDebut, @NotNull LocalTime heureFin) {}
    public record DisponibiliteView(Long id, Long medecinId, JourSemaine jourSemaine, LocalTime heureDebut, LocalTime heureFin, boolean active) {}

    public record RendezVousRequest(@NotNull Long patientId, @NotNull Long medecinId, @NotNull @Future LocalDateTime dateHeure,
        @Min(10) @Max(480) int dureeMinutes, @NotBlank @Size(max = 500) String motif) {}
    public record RendezVousView(Long id, Long patientId, String patient, Long medecinId, String medecin,
        LocalDateTime dateHeure, int dureeMinutes, String motif, StatutRendezVous statut) {}
    public record StatutRendezVousRequest(@NotNull StatutRendezVous statut) {}

    public record LignePrescriptionRequest(@NotNull Long medicamentId, @NotBlank String dosage,
        @Min(1) int dureeJours, @Min(1) int quantite) {}
    public record PrescriptionRequest(@NotNull Long rendezVousId, String instructions, boolean overrideAllergie,
        @Size(max = 500) String motifOverrideAllergie, @NotEmpty List<@Valid LignePrescriptionRequest> lignes) {}
    public record LignePrescriptionView(Long medicamentId, String medicament, String dosage, int dureeJours, int quantite, boolean stockFaible) {}
    public record PrescriptionView(Long id, Long rendezVousId, LocalDate datePrescription, String instructions,
        boolean validee, boolean overrideAllergie, List<LignePrescriptionView> lignes, List<String> avertissements) {}

    public record MedicamentRequest(@NotBlank String code, @NotBlank String nom, String description,
        @NotNull @DecimalMin("0.00") BigDecimal prix, @Min(0) int stock, @Min(0) int seuilAlerte) {}
    public record MedicamentView(Long id, String code, String nom, String description, BigDecimal prix,
        int stock, int seuilAlerte, boolean stockFaible, boolean actif) {}

    public record PaiementRequest(@NotNull @DecimalMin("0.01") BigDecimal montant, @NotNull ModePaiement mode,
        @Size(max = 100) String referencePaiement) {}
    public record PaiementView(Long id, Long factureId, BigDecimal montant, ModePaiement mode, String referencePaiement,
        LocalDateTime datePaiement, BigDecimal soldeRestant, StatutPaiement statutFacture) {}

    public record FactureRequest(@NotNull Long rendezVousId, @NotNull @DecimalMin("0.00") BigDecimal remise,
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal tauxTva) {}
    public record FactureView(Long id, String numero, Long rendezVousId, BigDecimal montantConsultation,
        BigDecimal montantMedicaments, BigDecimal remise, BigDecimal tauxTva, BigDecimal total, StatutPaiement statut) {}

    public record NotificationRequest(Long rendezVousId, @NotBlank String destinataire, @NotNull CanalNotification canal,
        @NotNull TypeNotification type, @NotBlank String contenu) {}
    public record NotificationView(Long id, String destinataire, CanalNotification canal, TypeNotification type,
        StatutEnvoi statut, int nombreTentatives, String erreur, LocalDateTime dateEnvoi) {}

    public record StatistiqueView(long patientsCeMois, long rendezVousTotal, BigDecimal revenus,
        BigDecimal tauxAbsenteisme, List<MedicamentPrescriptionStat> medicamentsLesPlusPrescrits) {}
    public record MedicamentPrescriptionStat(String medicament, long quantite) {}
}
