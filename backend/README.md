# MediCare+ — API REST Spring Boot 3 / Java 21

> Plateforme de gestion médicale sécurisée : patients, praticiens, rendez-vous, prescriptions, facturation et notifications.
> Stack : **Spring Boot 3 · Java 21 · MySQL 8 · Flyway · JWT · React/Vite**.

[![Java](https://img.shields.io/badge/Java-21-orange)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)](https://www.mysql.com/)
[![Flyway](https://img.shields.io/badge/Flyway-migrations-red)](https://flywaydb.org/)
[![License](https://img.shields.io/badge/license-MIT-lightgrey)](#)

---

## 📑 Table des matières

1. [Vue d'ensemble](#1-vue-densemble)
2. [Architecture & principes](#2-architecture--principes)
3. [Prérequis](#3-prérequis)
4. [Configuration de la base de données](#4-configuration-de-la-base-de-données)
5. [Démarrage recommandé — Docker](#5-démarrage-recommandé--docker)
6. [Démarrage natif — Windows sans Docker](#6-démarrage-natif--windows-sans-docker)
7. [Comportement de Flyway](#7-comportement-de-flyway)
8. [Comptes seedés et jeu de démonstration](#8-comptes-seedés-et-jeu-de-démonstration)
9. [Authentification et cycle de vie des tokens](#9-authentification-et-cycle-de-vie-des-tokens)
10. [Documentation de l'API — Swagger & OpenAPI](#10-documentation-de-lapi--swagger--openapi)
11. [Suite de vérification (smoke tests)](#11-suite-de-vérification-smoke-tests)
12. [Frontend React / Vite](#12-frontend-react--vite)
13. [Persistance et redémarrages](#13-persistance-et-redémarrages)
14. [Dépannage](#14-dépannage)
15. [Bonnes pratiques de contribution](#15-bonnes-pratiques-de-contribution)

---

## 1. Vue d'ensemble

MediCare+ est une **API REST Spring Boot 3 / Java 21** entièrement adossée à **MySQL 8**.

Elle repose sur trois piliers :

| Pilier | Responsable | Règle d'or |
|--------|-------------|------------|
| **Schéma & données de démo** | Flyway | Migrations **immuables**, exécutées **une seule fois** |
| **Mapping objet-relationnel** | Hibernate (`ddl-auto: validate`) | Ne crée **jamais** de table, ne modifie **jamais** le schéma |
| **Sécurité** | Spring Security + JWT | Access token court + refresh token rotatif hashé SHA-256 |

> ℹ️ **Aucune migration n'est générée à la volée.** Toute évolution du schéma passe par une **nouvelle migration versionnée**.

---

## 2. Architecture & principes

```
com.medicareplus
├── config/          → configuration Spring (Security, JWT, OpenAPI, CORS)
├── controller/      → endpoints REST (contrats exposés via Swagger)
├── service/         → logique métier et règles de gestion
├── repository/      → accès données (Spring Data JPA)
├── entity/          → modèle JPA (validé au démarrage par Hibernate)
├── dto/             → objets de transfert (requêtes/réponses)
├── mapper/          → conversion entité ⇄ DTO
└── exception/       → gestion centralisée des erreurs (RFC 7807)
```

**Principes non négociables :**

- ✅ Flyway possède **le schéma** et **le jeu de démonstration**.
- ✅ Hibernate est en **`ddl-auto: validate`** — il refuse de démarrer si le modèle JPA diverge du schéma migré.
- ✅ Les mots de passe sont **exclusivement** hachés en BCrypt.
- ✅ Les refresh tokens ne sont **jamais** stockés en clair (SHA-256 uniquement).
- ✅ Le frontend **ne connaît jamais** la base de données, il n'appelle que l'API.
- ✅ Le profil `demo` est **interdit en production**.

---

## 3. Prérequis

### Option A — Docker (recommandé pour la reproductibilité)

| Outil | Version minimale |
|-------|------------------|
| Docker Desktop (WSL 2) | dernière stable |
| Git | 2.40+ |
| Node.js | 22 LTS |
| pnpm | 10+ |

### Option B — Installation native (Windows)

| Outil | Version minimale |
|-------|------------------|
| Windows 10/11 | — |
| PowerShell | 5.1 ou 7 |
| Java (Temurin) | **21** |
| Maven | 3.9+ |
| MySQL Community Server | 8.0 ou 8.4 |
| Node.js | 22 LTS |
| pnpm | 10+ |
| Git | 2.40+ |

**Vérification rapide :**

```powershell
java -version
mvn -version
node --version
pnpm --version
git --version
mysql --version     # optionnel, pour la partie native
```

---

## 4. Configuration de la base de données

L'application lit sa configuration via **variables d'environnement**.

| Variable | Rôle | Exemple local |
|----------|------|---------------|
| `DATABASE_NAME` | Schéma MySQL | `medicareplus` |
| `DATABASE_HOST` | Hôte MySQL | `localhost` |
| `DATABASE_PORT` | Port TCP MySQL | `3306` |
| `DATABASE_USERNAME` | Utilisateur applicatif | `medicareplus` |
| `DATABASE_PASSWORD` | Mot de passe applicatif | *aucun défaut* |
| `MYSQL_ROOT_PASSWORD` | Init. conteneur Docker uniquement | *aucun défaut* |
| `JWT_SECRET` | Clé HMAC, **≥ 32 caractères** | *aucun défaut* |
| `DEMO_PASSWORD` | Mot de passe des comptes de démo (profil `demo`) | *aucun défaut* |
| `FRONTEND_ORIGIN` | Origine CORS autorisée | `http://localhost:8443` |

### Mise en place

```powershell
Set-Location backend
Copy-Item .env.example .env
notepad .env
```

> 🔒 **Le fichier `.env` est ignoré par Git.** En environnement partagé, `DATABASE_PASSWORD`, `MYSQL_ROOT_PASSWORD`, `JWT_SECRET` et `DEMO_PASSWORD` doivent être **quatre valeurs fortes et distinctes**.

Génération d'un `JWT_SECRET` robuste (PowerShell) :

```powershell
-join ((48..57) + (65..90) + (97..122) | Get-Random -Count 48 | ForEach-Object {[char]$_})
```

---

## 5. Démarrage recommandé — Docker

Depuis le dossier `backend` :

```powershell
docker compose --env-file .env up --build -d
docker compose ps
docker compose logs -f api
```

**Stack lancée :**

| Service | URL | Rôle |
|---------|-----|------|
| **MySQL 8.4** | `localhost:3306` | Volume nommé persistant |
| **API MediCare+** | http://localhost:8080 | Backend Spring Boot |
| **Mailpit** | http://localhost:8025 | Capture SMTP (démo) |

**Séquence de démarrage :**

1. Docker attend le **healthcheck MySQL**.
2. Flyway applique les migrations **dans l'ordre numérique**.
3. Hibernate **valide** que le modèle JPA correspond au schéma migré.
4. Si une étape échoue (identifiants manquants, DB indisponible, checksum invalide, dérive de schéma) → **arrêt immédiat**.

**Vérification de disponibilité :**

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
# Attendu : status = UP
```

### 🧹 Réinitialisation complète (destructive)

```powershell
docker compose --env-file .env down -v
docker compose --env-file .env up --build -d
docker compose logs -f api
```

> ⚠️ **`down -v` supprime définitivement le volume.** À ne jamais exécuter sur un environnement contenant des données à conserver.

---

## 6. Démarrage natif — Windows sans Docker

### 6.1 Installer MySQL Community Server 8

Installer **MySQL 8.0** ou **8.4** via l'installateur officiel, puis dans MySQL Shell :

```sql
CREATE DATABASE medicareplus
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER 'medicareplus'@'localhost'
    IDENTIFIED BY 'REMPLACER-PAR-UN-MOT-DE-PASSE-FORT';

GRANT ALL PRIVILEGES ON medicareplus.* TO 'medicareplus'@'localhost';

FLUSH PRIVILEGES;
```

### 6.2 Définir les variables d'environnement

```powershell
$env:DATABASE_NAME     = "medicareplus"
$env:DATABASE_HOST     = "localhost"
$env:DATABASE_PORT     = "3306"
$env:DATABASE_USERNAME = "medicareplus"
$env:DATABASE_PASSWORD = "VotreMotDePasseFort123!"
$env:JWT_SECRET        = "Z87aJhOUAHiLoxmw1uQ0EjvlFStMyrzWe42Gn3CbpPX9Bs56"
$env:SPRING_PROFILES_ACTIVE = "demo"
$env:DEMO_PASSWORD     = "UnMotDePasseDemo12!"
$env:FRONTEND_ORIGIN   = "http://localhost:8443"
$env:SERVER_PORT       = "8081"   # si 8080 est déjà pris
```

**Pour rendre ces variables permanentes (utilisateur Windows) :**

```powershell
[Environment]::SetEnvironmentVariable("DATABASE_NAME", "medicareplus", "User")
# ... etc.
```

Puis **ouvrir un nouveau terminal**.

### 6.3 Compiler, tester, démarrer

```powershell
Set-Location backend
mvn clean test
mvn spring-boot:run
```

> 💡 **Astuce** : créer un fichier `start-backend.ps1` pour éviter de re-saisir les variables à chaque session.

---

## 7. Comportement de Flyway

### 7.1 Structure des migrations

```
src/main/resources/db/
├── migration/            → toujours appliquées
│   ├── V1__create_medicare_schema.sql
│   └── V3__enforce_patient_email_and_invoice_dates.sql
└── demo/                 → appliquées uniquement si profil = demo
    ├── V2__seed_demo_data.sql
    └── V4__complete_demo_business_states.sql
```

| Fichier | Rôle |
|---------|------|
| `V1__create_medicare_schema.sql` | Tables, clés étrangères, contraintes CHECK, index |
| `V2__seed_demo_data.sql` | Jeu de démonstration principal (patients, praticiens, RDV…) |
| `V3__enforce_patient_email_and_invoice_dates.sql` | Unicité email patient + index de facturation |
| `V4__complete_demo_business_states.sql` | États métier complets (factures, paiements, alertes stock, notifications…) |

### 7.2 Règles

- 🔒 **Migrations immuables** : jamais éditer une migration déjà appliquée → créer une nouvelle version.
- 🔢 **Ordre strictement numérique** (V1 < V2 < V3 < V4 < …).
- 🧾 **Checksums** enregistrés dans `flyway_schema_history` → une migration modifiée **empêche le démarrage**.
- 🧪 **Profil `demo` uniquement en dev/démo.** En production, seules les migrations de `db/migration` s'exécutent.
- 🔗 **IDs stables + clés étrangères cohérentes** dans le seed.
- 📅 **Dates relatives calculées une seule fois** lors d'une initialisation propre.

---

## 8. Comptes seedés et jeu de démonstration

### 8.1 Comptes utilisateurs

| Username | Rôle |
|----------|------|
| `admin.demo` | `ADMIN` |
| `medecin.demo` | `MEDECIN` |
| `secretaire.demo` | `SECRETAIRE` |

- Mots de passe **BCrypt uniquement**, jamais en clair.
- Quand `SPRING_PROFILES_ACTIVE=demo`, le backend lit `DEMO_PASSWORD`, le hash et l'affecte aux 3 comptes.
- **Le frontend ne contient jamais** les mots de passe de démo ni n'effectue de validation locale.

### 8.2 Contenu du seed

- 🩺 **1 cardiologue** avec plusieurs plages horaires hebdomadaires
- 👤 **Patients** avec hypertension, diabète, antécédents chirurgicaux, allergies connues
- 📅 **Rendez-vous** couvrant les 5 statuts : `EN_ATTENTE`, `CONFIRME`, `TERMINE`, `ANNULE`, `ABSENT`
- 🗓️ **Rendez-vous futurs valides** dans la disponibilité du praticien
- 💊 **Médicaments** en stock normal **et** en stock faible
- 📝 **Prescriptions validées** + **un override d'allergie explicitement confirmé**
- 💰 **Factures** couvrant les 4 statuts : `EN_ATTENTE`, `PARTIELLEMENT_PAYEE`, `PAYEE`, `NON_PAYEE`
- 💳 **Historiques de paiement** partiels et complets
- 🔔 **Logs de notifications** en succès **et** en échec
- 🧾 **Un RDV terminé réservé** aux tests de facturation/paiement

---

## 9. Authentification et cycle de vie des tokens

### 9.1 Endpoints

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `POST` | `/api/auth/login` | Authentifie et retourne `accessToken` + `refreshToken` |
| `POST` | `/api/auth/refresh` | Renouvelle les tokens (rotation) |
| `POST` | `/api/auth/logout` | Révoque le refresh token (nécessite `Authorization: Bearer` + body) |

### 9.2 Exemple de login

```powershell
$body = @{
  username = "admin.demo"
  password = $env:DEMO_PASSWORD
} | ConvertTo-Json

$session = Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8080/api/auth/login `
  -ContentType "application/json" `
  -Body $body

$session.user.role
$session.accessToken
```

### 9.3 Garanties de sécurité

- ✅ **JWT signés HMAC-SHA384** (`alg: HS384`).
- ✅ **Refresh tokens stockés uniquement en SHA-256** (colonne `refresh_token_hash`, 64 caractères hex).
- ✅ **Rotation** à chaque appel à `/refresh` (nouveau `jti`, nouveau hash).
- ✅ **Logout** invalide le hash en base → le refresh token devient inutilisable (HTTP 401).
- ✅ **Vérification BCrypt** des mots de passe contre la table MySQL `utilisateur`.

---

## 10. Documentation de l'API — Swagger & OpenAPI

| Ressource | URL |
|-----------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Mailpit UI | http://localhost:8025 |

**23 endpoints** couvrant : authentification, administration, patients, dossiers médicaux, rendez-vous, disponibilités, médicaments, prescriptions, factures, paiements, notifications, statistiques et export CSV.

### Endpoints principaux

```
[auth]      POST   /api/auth/login
            POST   /api/auth/refresh
            POST   /api/auth/logout

[admin]     GET    /api/admin/utilisateurs
            POST   /api/admin/utilisateurs
            DELETE /api/admin/utilisateurs/{id}
            GET    /api/admin/medecins
            POST   /api/admin/medecins
            DELETE /api/admin/medecins/{id}

[patients]  GET    /api/patients
            POST   /api/patients
            GET    /api/patients/{id}
            PUT    /api/patients/{id}
            DELETE /api/patients/{id}
            GET    /api/dossiers/patient/{patientId}
            GET    /api/dossiers/patient/{patientId}/historique

[rdv]       GET    /api/rendez-vous
            POST   /api/rendez-vous
            PATCH  /api/rendez-vous/{id}/statut
            GET    /api/disponibilites
            POST   /api/disponibilites

[meds]      GET    /api/medicaments
            POST   /api/medicaments
            PUT    /api/medicaments/{id}

[rx]        GET    /api/prescriptions
            POST   /api/prescriptions
            PUT    /api/prescriptions/{id}
            DELETE /api/prescriptions/{id}

[billing]   POST   /api/factures
            POST   /api/factures/{id}/paiements

[notify]    POST   /api/notifications

[stats]     GET    /api/statistiques
            GET    /api/rapports/statistiques.csv
```

---

## 11. Suite de vérification (smoke tests)

Un script PowerShell **reproductible** valide l'ensemble des comportements critiques :

```powershell
$env:DEMO_PASSWORD = "valeur-identique-à-celle-du-backend"
.\scripts\verify-api.ps1
```

**Ce qui est vérifié :**

- ✅ Les **3 logins** (admin, médecin, secrétaire)
- ✅ **JWT authorization** (401 sans token, 403 en cross-rôle)
- ✅ Codes HTTP : **201, 400, 401, 403, 404, 409**
- ✅ Création de **dossier médical** liée à la création d'un patient
- ✅ **Conflits** de rendez-vous et de disponibilité
- ✅ **Override d'allergie**
- ✅ **Calcul de facture** (consultation + TVA)
- ✅ **Statut de paiement partiel**
- ✅ **Rejet des paiements excédentaires**
- ✅ **Swagger** accessible
- ✅ **Acceptation des notifications**

**Sortie attendue :**

```
[PASS] Application health
[PASS] OpenAPI document is accessible
[PASS] ADMIN database login
[PASS] MEDECIN database login
[PASS] SECRETAIRE database login
[PASS] Unauthenticated request is rejected
[PASS] Cross-role request is forbidden
[PASS] Unknown resource returns 404
[PASS] Bean validation returns 400
[PASS] Patient creation returns 201
[PASS] Patient creation also creates a medical record
[PASS] Availability overlap returns 409
[PASS] Appointment conflict returns 409
[PASS] Allergy conflict requires explicit override
[PASS] Doctor allergy override succeeds
[PASS] Invoice creation returns 201
[PASS] Invoice calculation includes configured consultation and VAT
[PASS] Partial payment returns 201
[PASS] Partial payment updates invoice status
[PASS] Payment above remaining balance returns 409
[PASS] Asynchronous notification is accepted
[PASS] Notification outcome is persisted in MySQL
MediCare+ API verification completed successfully.
```

---

## 12. Frontend React / Vite

Depuis la racine du dépôt :

```powershell
$env:VITE_API_URL = "http://localhost:8080"
pnpm install
pnpm dev
```

Le client React :

- ✅ **appelle uniquement l'API Spring Boot**
- ❌ **ne se connecte jamais** à MySQL
- ❌ **ne persiste aucune donnée métier** dans le stockage navigateur

Accès : **https://localhost:8443** (certificat auto-signé à accepter en développement).

### Comptes de démonstration disponibles à l'écran

| Profil | Identifiant | Mot de passe |
|--------|-------------|--------------|
| ADMIN | `admin.demo` | `DEMO_PASSWORD` |
| MEDECIN | `medecin.demo` | `DEMO_PASSWORD` |
| SECRETAIRE | `secretaire.demo` | `DEMO_PASSWORD` |

> 🔒 Le frontend **affiche les identifiants** mais **ne stocke ni n'affiche jamais les mots de passe**. La validation se fait exclusivement côté backend.

---

## 13. Persistance et redémarrages

### 13.1 Docker

```powershell
docker compose --env-file .env restart mysql api
Invoke-RestMethod http://localhost:8080/actuator/health
```

- ✅ Le **volume nommé** `medicareplus_mysql` survit aux redémarrages.
- ✅ **Flyway ne réinsère ni n'écrase** les données existantes.
- ✅ Il **valide** l'historique et **n'applique que** les nouvelles versions.

### 13.2 Natif (Windows)

- ✅ Le service `MySQL80` conserve les données à travers un `Restart-Service`.
- ✅ Au redémarrage de Spring Boot, Flyway affiche `No migration necessary`.
- ✅ Les compteurs métier (`patients`, `rendez_vous`, `utilisateur`) restent **inchangés**.
- ✅ La table `flyway_schema_history` conserve ses **4 lignes d'origine** (mêmes `installed_on`).

**Test de persistance (procédure recommandée) :**

```sql
-- 1. Noter les compteurs AVANT
SELECT
  (SELECT COUNT(*) FROM patient)      AS nb_patients,
  (SELECT COUNT(*) FROM rendez_vous)  AS nb_rendez_vous,
  (SELECT COUNT(*) FROM utilisateur)  AS nb_utilisateurs;

SELECT installed_rank, version, description, success, installed_on
FROM flyway_schema_history
ORDER BY installed_rank;
```

Puis :

1. **Arrêter Spring Boot** (`Ctrl+C` dans la fenêtre `mvn spring-boot:run`).
2. **Redémarrer MySQL** (`Restart-Service MySQL80` en admin).
3. **Redémarrer Spring Boot** — observer les logs Flyway : **aucune** ligne `Migrating schema ...` ne doit apparaître.
4. Vérifier que la table `flyway_schema_history` conserve **les mêmes `installed_on`**.

---

## 14. Dépannage

| Symptôme | Cause probable | Solution |
|----------|----------------|----------|
| `Port 8080 already in use` | Docker ou autre service | Changer `SERVER_PORT` (ex. `8081`) |
| `Port 8443 already in use` | Ancienne instance Vite | `Get-Process node \| Stop-Process -Force` |
| `401 Unauthorized` sur `/refresh` | Refresh token révoqué ou expiré | Refaire un login |
| `401 Unauthorized` sur `/logout` | Header `Authorization` manquant | Envoyer `Bearer <accessToken>` |
| `Migration checksum mismatch` | Migration déjà appliquée modifiée | Restaurer le fichier ou créer une nouvelle version |
| `Script is not digitally signed` | Politique PowerShell | `Unblock-File .\scripts\verify-api.ps1` |
| `Cannot find spring-boot plugin` | Mauvais répertoire | Se placer dans `backend/` |
| `Access denied for user 'medicareplus'@'localhost'` | Mauvais mot de passe | Vérifier `DATABASE_PASSWORD` |
| `Communications link failure` | MySQL arrêté | `Start-Service MySQL80` |
| `Unable to find a @SpringBootConfiguration` | Mauvais répertoire de travail | Se placer dans `backend/` |
| Frontend : page blanche après login | Mauvais port `VITE_API_URL` | Redéfinir `VITE_API_URL` et relancer `pnpm dev` |
| CORS error dans la console navigateur | `FRONTEND_ORIGIN` incorrect | Mettre à jour `.env` et redémarrer le backend |

---

## 15. Bonnes pratiques de contribution

1. **Ne jamais modifier une migration appliquée.** Ajouter une nouvelle version `V5__…sql`.
2. **Ne jamais committer `.env`.** Utiliser `.env.example` comme contrat.
3. **Toujours écrire un test** pour un nouveau endpoint ou une nouvelle règle métier.
4. **Respecter la séparation** `controller → service → repository`.
5. **Ne jamais activer `SPRING_PROFILES_ACTIVE=demo` en production.**
6. **Vérifier les checksums Flyway** avant tout déploiement.
7. **Mettre à jour la documentation Swagger** (annotations OpenAPI) en même temps que le code.
8. **Exécuter `.\scripts\verify-api.ps1`** avant toute Pull Request.
9. **Nommer les commits** selon la convention `type(scope): message` (feat, fix, docs, refactor…).
10. **Documenter toute nouvelle variable d'environnement** dans ce README et dans `.env.example`.

---

## 📄 Licence

Distribué sous licence **MIT**. Voir `LICENSE` pour plus d'informations.

---

## 🙏 Remerciements

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Flyway](https://flywaydb.org/)
- [MySQL](https://www.mysql.com/)
- [Vite](https://vitejs.dev/)
- [Mailpit](https://github.com/axllent/mailpit)
- [Adoptium (Temurin JDK)](https://adoptium.net/)

---

