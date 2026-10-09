import { useMemo, useState, type FormEvent, type ReactNode } from "react";
import { ApiError, authenticate, logout as apiLogout } from "./api";

type IconName =
  | "grid"
  | "calendar"
  | "users"
  | "doctor"
  | "file"
  | "pill"
  | "invoice"
  | "chart"
  | "bell"
  | "settings"
  | "search"
  | "plus"
  | "arrow"
  | "clock"
  | "check"
  | "warning"
  | "wallet"
  | "more"
  | "chevron"
  | "download"
  | "filter"
  | "close";

type Role = "ADMIN" | "MEDECIN" | "SECRETAIRE";
type AppView = "landing" | "login" | "app" | "forbidden";

type UserProfile = {
  name: string;
  role: Role;
  title: string;
  initials: string;
};

const iconPaths: Record<IconName, ReactNode> = {
  grid: <><rect x="3" y="3" width="7" height="7" rx="2"/><rect x="14" y="3" width="7" height="7" rx="2"/><rect x="3" y="14" width="7" height="7" rx="2"/><rect x="14" y="14" width="7" height="7" rx="2"/></>,
  calendar: <><rect x="3" y="5" width="18" height="16" rx="3"/><path d="M16 3v4M8 3v4M3 10h18"/><path d="M8 14h.01M12 14h.01M16 14h.01M8 17.5h.01M12 17.5h.01"/></>,
  users: <><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/></>,
  doctor: <><circle cx="12" cy="7" r="4"/><path d="M5.5 21v-2.5a6.5 6.5 0 0 1 13 0V21M9.5 11v3l2.5 2 2.5-2v-3"/><path d="M18.5 17.5h2M19.5 16.5v2"/></>,
  file: <><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><path d="M14 2v6h6M8 13h8M8 17h5"/></>,
  pill: <><path d="m10.5 20.5-7-7a5 5 0 0 1 7-7l7 7a5 5 0 0 1-7 7Z"/><path d="m8 11 5-5M14 17l3-3"/></>,
  invoice: <><path d="M6 2h12v20l-3-2-3 2-3-2-3 2V2Z"/><path d="M9 7h6M9 11h6M9 15h3"/></>,
  chart: <><path d="M3 3v18h18"/><path d="m7 16 4-5 4 3 5-7"/></>,
  bell: <><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4"/></>,
  settings: <><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 0 0 .34 1.88l.06.06-2.83 2.83-.06-.06a1.7 1.7 0 0 0-1.88-.34 1.7 1.7 0 0 0-1.03 1.56V21h-4v-.09A1.7 1.7 0 0 0 8.94 19.4a1.7 1.7 0 0 0-1.88.34l-.06.06-2.83-2.83.06-.06A1.7 1.7 0 0 0 4.57 15 1.7 1.7 0 0 0 3 14H3v-4h.09A1.7 1.7 0 0 0 4.6 8.94a1.7 1.7 0 0 0-.34-1.88L4.2 7l2.83-2.83.06.06A1.7 1.7 0 0 0 9 4.57 1.7 1.7 0 0 0 10 3V3h4v.09a1.7 1.7 0 0 0 1.06 1.51 1.7 1.7 0 0 0 1.88-.34L17 4.2 19.83 7l-.06.06A1.7 1.7 0 0 0 19.43 9 1.7 1.7 0 0 0 21 10h.09v4H21a1.7 1.7 0 0 0-1.6 1Z"/></>,
  search: <><circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/></>,
  plus: <path d="M12 5v14M5 12h14"/>,
  arrow: <path d="m9 18 6-6-6-6"/>,
  clock: <><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></>,
  check: <path d="m5 12 4 4L19 6"/>,
  warning: <><path d="M10.3 3.6 2.6 17a2 2 0 0 0 1.7 3h15.4a2 2 0 0 0 1.7-3L13.7 3.6a2 2 0 0 0-3.4 0Z"/><path d="M12 9v4M12 17h.01"/></>,
  wallet: <><path d="M3 6h15a2 2 0 0 1 2 2v11H5a2 2 0 0 1-2-2V6Z"/><path d="M3 6a3 3 0 0 1 3-3h11v3M15 11h7v5h-7a2 2 0 0 1 0-5Z"/></>,
  more: <><circle cx="5" cy="12" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/></>,
  chevron: <path d="m9 18 6-6-6-6"/>,
  download: <><path d="M12 3v12M7 10l5 5 5-5M5 21h14"/></>,
  filter: <path d="M4 5h16M7 12h10M10 19h4"/>,
  close: <path d="M6 6l12 12M18 6 6 18"/>,
};

function Icon({ name, size = 18 }: { name: IconName; size?: number }) {
  return <svg className="icon" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">{iconPaths[name]}</svg>;
}

const NAV_BY_ROLE: Record<Role, { label: string; icon: IconName; count?: number }[]> = {
  ADMIN: [
    { label: "Dashboard", icon: "grid" }, { label: "Utilisateurs", icon: "users" },
    { label: "Patients", icon: "users" }, { label: "Dossiers médicaux", icon: "file" },
    { label: "Médecins", icon: "doctor" }, { label: "Disponibilités", icon: "clock" },
    { label: "Rendez-vous", icon: "calendar", count: 8 }, { label: "Prescriptions", icon: "pill" },
    { label: "Médicaments", icon: "pill" }, { label: "Factures", icon: "invoice", count: 3 },
    { label: "Paiements", icon: "wallet" }, { label: "Notifications", icon: "bell" },
    { label: "Statistiques", icon: "chart" }, { label: "Rapports", icon: "file" },
    { label: "Paramètres", icon: "settings" },
  ],
  MEDECIN: [
    { label: "Dashboard", icon: "grid" }, { label: "Mes patients", icon: "users" },
    { label: "Mes dossiers", icon: "file" }, { label: "Mes rendez-vous", icon: "calendar", count: 8 },
    { label: "Mes disponibilités", icon: "clock" }, { label: "Mes prescriptions", icon: "pill" },
    { label: "Médicaments", icon: "pill" }, { label: "Factures", icon: "invoice" },
  ],
  SECRETAIRE: [
    { label: "Dashboard", icon: "grid" }, { label: "Patients", icon: "users" },
    { label: "Médecins", icon: "doctor" }, { label: "Disponibilités", icon: "clock" },
    { label: "Rendez-vous", icon: "calendar", count: 8 }, { label: "Médicaments", icon: "pill" },
    { label: "Factures", icon: "invoice", count: 3 }, { label: "Paiements", icon: "wallet" },
  ],
};

const DEMO_USERS: Record<string, UserProfile> = {
  "admin.demo": { name: "Admin MediCare+", role: "ADMIN", title: "Administration système", initials: "AD" },
  "medecin.demo": { name: "Dr. Alexandre Martin", role: "MEDECIN", title: "Cardiologie", initials: "AM" },
  "secretaire.demo": { name: "Sonia Trabelsi", role: "SECRETAIRE", title: "Accueil & facturation", initials: "ST" },
};

const appointments = [
  { time: "09:00", duration: "30 min", name: "Sophie Bernard", initials: "SB", reason: "Suivi cardiologique", status: "Confirmé", tone: "blue" },
  { time: "10:00", duration: "45 min", name: "Marc Dubois", initials: "MD", reason: "Première consultation", status: "En attente", tone: "sand" },
  { time: "11:15", duration: "30 min", name: "Lina Moreau", initials: "LM", reason: "Contrôle post-opératoire", status: "Confirmé", tone: "violet" },
  { time: "14:00", duration: "30 min", name: "Thomas Leroy", initials: "TL", reason: "Renouvellement traitement", status: "Confirmé", tone: "green" },
];

const patients = [
  { name: "Sophie Bernard", id: "PAT-02418", meta: "42 ans · Groupe O+", status: "Actif", extra: "12 juin 2025", detail: "Dr. A. Martin" },
  { name: "Marc Dubois", id: "PAT-02417", meta: "56 ans · Groupe A+", status: "Actif", extra: "12 juin 2025", detail: "Dr. A. Martin" },
  { name: "Lina Moreau", id: "PAT-02412", meta: "31 ans · Groupe B−", status: "Actif", extra: "11 juin 2025", detail: "Dr. C. Robert" },
  { name: "Thomas Leroy", id: "PAT-02398", meta: "68 ans · Groupe AB+", status: "Archivé", extra: "10 juin 2025", detail: "Dr. A. Martin" },
  { name: "Emma Petit", id: "PAT-02391", meta: "27 ans · Groupe O−", status: "Actif", extra: "9 juin 2025", detail: "Dr. N. Garnier" },
];

function Badge({ children, tone = "neutral" }: { children: ReactNode; tone?: string }) {
  return <span className={`badge badge-${tone}`}>{children}</span>;
}

function Logo() {
  return <div className="brand"><div className="brand-mark"><span></span><span></span></div><span>MediCare<span className="brand-plus">+</span></span></div>;
}

function Sidebar({ active, onChange, user, onLogout }: { active: string; onChange: (value: string) => void; user: UserProfile; onLogout: () => void }) {
  const navigation = NAV_BY_ROLE[user.role];
  return (
    <aside className="sidebar">
      <Logo />
      <div className="workspace-label">ESPACE {user.role}</div>
      <nav className="nav-list" aria-label="Navigation principale">
        {navigation.map((item) => (
          <button key={item.label} className={`nav-item ${active === item.label ? "active" : ""}`} onClick={() => onChange(item.label)}>
            <Icon name={item.icon} /><span>{item.label}</span>{item.count && <span className="nav-count">{item.count}</span>}
          </button>
        ))}
      </nav>
      <div className="sidebar-bottom">
        {user.role !== "ADMIN" && <button className="nav-item"><Icon name="bell" /><span>Notifications</span><i className="notification-dot"></i></button>}
        <div className="support-card">
          <div className="support-icon"><Icon name="file" size={17}/></div>
          <strong>Centre d’aide</strong>
          <span>Guides et documentation</span>
          <button>Consulter <Icon name="arrow" size={14}/></button>
        </div>
        <div className="user-card">
          <div className="avatar avatar-dark">{user.initials}</div>
          <div><strong>{user.name}</strong><span>{user.role} · {user.title}</span></div>
          <button className="logout-mini" onClick={onLogout} title="Se déconnecter"><Icon name="arrow"/></button>
        </div>
      </div>
    </aside>
  );
}

function Topbar({ onMenu, user, onLogout }: { onMenu: () => void; user: UserProfile; onLogout: () => void }) {
  const [menuOpen, setMenuOpen] = useState(false);
  return (
    <header className="topbar">
      <button className="mobile-menu" onClick={onMenu} aria-label="Ouvrir le menu"><Icon name="grid"/></button>
      <div className="searchbox"><Icon name="search"/><input aria-label="Recherche globale" placeholder="Rechercher un patient, une facture..." /><kbd>⌘ K</kbd></div>
      <div className="top-actions">
        <button className="icon-button" aria-label="Notifications"><Icon name="bell"/><span></span></button>
        <div className="top-separator"></div>
        <button className="profile-trigger" onClick={() => setMenuOpen(!menuOpen)}>
          <div className="avatar avatar-green">{user.initials}</div>
          <div><small>{user.name}</small><strong>{user.role}</strong></div><Icon name="chevron" size={14}/>
        </button>
        {menuOpen && <div className="profile-menu">
          <div><strong>{user.name}</strong><span>{user.role} · Session sécurisée</span></div>
          <button><Icon name="users"/> Mon profil</button>
          <button onClick={onLogout}><Icon name="arrow"/> Se déconnecter</button>
        </div>}
      </div>
    </header>
  );
}

function MetricCard({ icon, label, value, note, tone }: { icon: IconName; label: string; value: string; note: string; tone: string }) {
  return (
    <article className="metric-card">
      <div className={`metric-icon ${tone}`}><Icon name={icon}/></div>
      <div className="metric-copy"><span>{label}</span><strong>{value}</strong><small><b>{note.split(" ")[0]}</b> {note.split(" ").slice(1).join(" ")}</small></div>
      <button className="metric-link" aria-label={`Voir ${label}`}><Icon name="arrow"/></button>
    </article>
  );
}

function AppointmentList({ onAdd }: { onAdd: () => void }) {
  return (
    <section className="panel schedule-panel">
      <div className="panel-heading">
        <div><h2>Planning du jour</h2><p>Jeudi 12 juin · 8 rendez-vous</p></div>
        <button className="text-button">Voir le planning <Icon name="arrow" size={15}/></button>
      </div>
      <div className="day-progress"><span style={{ width: "48%" }}></span></div>
      <div className="appointment-list">
        {appointments.map((appointment, index) => (
          <div className="appointment-row" key={appointment.time}>
            <div className="appointment-time"><strong>{appointment.time}</strong><span>{appointment.duration}</span></div>
            <div className={`avatar avatar-${appointment.tone}`}>{appointment.initials}</div>
            <div className="appointment-person"><strong>{appointment.name}</strong><span>{appointment.reason}</span></div>
            <Badge tone={appointment.status === "Confirmé" ? "success" : "warning"}>{appointment.status}</Badge>
            <button className="row-action" aria-label={`Actions pour ${appointment.name}`}><Icon name="more"/></button>
            {index < appointments.length - 1 && <div className="timeline-line"></div>}
          </div>
        ))}
      </div>
      <button className="add-line" onClick={onAdd}><Icon name="plus" size={16}/> Ajouter un rendez-vous</button>
    </section>
  );
}

function Dashboard({ onAdd, user }: { onAdd: () => void; user: UserProfile }) {
  const dashboardCopy = user.role === "ADMIN"
    ? { eyebrow: "PILOTAGE DE LA PLATEFORME", title: "Bonjour, Administrateur", detail: "Supervisez l’activité, les accès et les performances de MediCare+.", action: "Nouvel utilisateur" }
    : user.role === "SECRETAIRE"
    ? { eyebrow: "ACCUEIL & OPÉRATIONS", title: "Bonjour, Sonia", detail: "Gérez le planning, l’accueil patient et les encaissements du jour.", action: "Nouveau rendez-vous" }
    : { eyebrow: "JEUDI 12 JUIN 2025", title: "Bonjour, Dr. Martin", detail: "Voici l’activité de votre cabinet aujourd’hui.", action: "Nouveau rendez-vous" };
  return (
    <>
      <div className="page-heading">
        <div><p className="eyebrow">{dashboardCopy.eyebrow}</p><h1>{dashboardCopy.title}</h1><p>{dashboardCopy.detail}</p></div>
        <button className="primary-button" onClick={onAdd}><Icon name="plus"/> {dashboardCopy.action}</button>
      </div>
      <div className="metrics-grid">
        <MetricCard icon="calendar" label="Rendez-vous aujourd’hui" value="8" note="+2 depuis hier" tone="blue"/>
        <MetricCard icon="users" label="Patients suivis" value="248" note="+12 ce mois" tone="teal"/>
        <MetricCard icon="file" label="Consultations terminées" value="5" note="3 restantes" tone="purple"/>
        <MetricCard icon="wallet" label="Revenus ce mois" value="18 420 €" note="+8,2% vs. mai" tone="amber"/>
      </div>
      <div className="dashboard-grid">
        <AppointmentList onAdd={onAdd}/>
        <div className="right-stack">
          <section className="panel alert-panel">
            <div className="panel-heading compact"><div><h2>À traiter</h2><p>Actions prioritaires</p></div><Badge tone="danger">3</Badge></div>
            <div className="task-list">
              <button className="task"><span className="task-icon danger"><Icon name="warning"/></span><span><strong>Stock faible</strong><small>Amoxicilline · 8 unités</small></span><Icon name="arrow"/></button>
              <button className="task"><span className="task-icon warning"><Icon name="clock"/></span><span><strong>Factures en attente</strong><small>3 soldes à encaisser</small></span><Icon name="arrow"/></button>
              <button className="task"><span className="task-icon blue"><Icon name="bell"/></span><span><strong>Rappels échoués</strong><small>2 SMS à renvoyer</small></span><Icon name="arrow"/></button>
            </div>
          </section>
          <section className="panel finance-panel">
            <div className="panel-heading compact"><div><h2>Activité mensuelle</h2><p>Juin 2025</p></div><button className="row-action"><Icon name="more"/></button></div>
            <div className="chart-wrap">
              <div className="chart-y"><span>24k</span><span>16k</span><span>8k</span><span>0</span></div>
              <div className="bar-chart">
                {[42, 55, 48, 68, 62, 84].map((height, index) => <div className="bar-column" key={index}><div className={index === 5 ? "bar current" : "bar"} style={{ height: `${height}%` }}></div><span>{["Jan", "Fév", "Mar", "Avr", "Mai", "Juin"][index]}</span></div>)}
              </div>
            </div>
            <div className="finance-footer"><span><i></i>Revenus encaissés</span><strong>18 420 €</strong></div>
          </section>
        </div>
      </div>
      <section className="panel recent-panel">
        <div className="panel-heading"><div><h2>Patients récents</h2><p>Derniers dossiers consultés</p></div><button className="text-button">Voir tous les patients <Icon name="arrow" size={15}/></button></div>
        <PatientTable rows={patients.slice(0, 3)}/>
      </section>
    </>
  );
}

function PatientTable({ rows = patients }: { rows?: typeof patients }) {
  return (
    <div className="table-wrap">
      <table>
        <thead><tr><th>Patient</th><th>Identifiant</th><th>Dernière consultation</th><th>Médecin référent</th><th>Statut</th><th></th></tr></thead>
        <tbody>{rows.map((patient, index) => (
          <tr key={patient.id}>
            <td><div className="person-cell"><div className={`avatar avatar-${["blue","sand","violet","green"][index % 4]}`}>{patient.name.split(" ").map(word => word[0]).join("")}</div><div><strong>{patient.name}</strong><span>{patient.meta}</span></div></div></td>
            <td><span className="mono">{patient.id}</span></td><td>{patient.extra}</td><td>{patient.detail}</td>
            <td><Badge tone={patient.status === "Actif" ? "success" : "neutral"}>{patient.status}</Badge></td>
            <td><button className="row-action"><Icon name="more"/></button></td>
          </tr>
        ))}</tbody>
      </table>
    </div>
  );
}

const pageConfigs: Record<string, { title: string; description: string; action?: string; icon: IconName }> = {
  "Utilisateurs": { title: "Utilisateurs", description: "Gérez les comptes, rôles et états d’accès à la plateforme.", action: "Nouvel utilisateur", icon: "users" },
  "Rendez-vous": { title: "Rendez-vous", description: "Planifiez, filtrez et suivez le cycle de vie des consultations.", action: "Nouveau rendez-vous", icon: "calendar" },
  "Mes rendez-vous": { title: "Mes rendez-vous", description: "Votre planning et le cycle de vie de vos consultations.", action: "Nouveau rendez-vous", icon: "calendar" },
  "Patients": { title: "Patients", description: "Dossiers actifs et archivés · 248 patients au total.", action: "Nouveau patient", icon: "users" },
  "Mes patients": { title: "Mes patients", description: "Patients associés à vos consultations et suivis.", icon: "users" },
  "Médecins": { title: "Médecins", description: "Annuaire des praticiens et spécialités médicales.", icon: "doctor" },
  "Disponibilités": { title: "Disponibilités", description: "Plages de consultation et prévention des chevauchements.", action: "Ajouter une plage", icon: "clock" },
  "Mes disponibilités": { title: "Mes disponibilités", description: "Gérez vos plages de consultation sans chevauchement.", action: "Ajouter une plage", icon: "clock" },
  "Dossiers médicaux": { title: "Dossiers médicaux", description: "Antécédents, allergies et historique de consultation sécurisés.", action: "Ouvrir un dossier", icon: "file" },
  "Mes dossiers": { title: "Mes dossiers médicaux", description: "Dossiers des patients qui vous sont affectés.", icon: "file" },
  "Ordonnances": { title: "Ordonnances", description: "Prescriptions liées aux consultations et contrôle du stock.", action: "Nouvelle ordonnance", icon: "pill" },
  "Prescriptions": { title: "Prescriptions", description: "Ordonnances, posologies et contrôles de stock.", action: "Nouvelle prescription", icon: "pill" },
  "Mes prescriptions": { title: "Mes prescriptions", description: "Ordonnances liées à vos consultations.", action: "Nouvelle prescription", icon: "pill" },
  "Médicaments": { title: "Médicaments", description: "Catalogue, niveaux de stock et alertes de disponibilité.", icon: "pill" },
  "Facturation": { title: "Facturation", description: "Consultez les factures liées à vos consultations et leurs soldes.", icon: "invoice" },
  "Factures": { title: "Factures", description: "Factures émises, statuts et soldes restants.", icon: "invoice" },
  "Paiements": { title: "Paiements", description: "Encaissements, paiements partiels et reçus.", action: "Enregistrer un paiement", icon: "wallet" },
  "Notifications": { title: "Notifications", description: "Historique des envois, rappels et tentatives échouées.", icon: "bell" },
  "Statistiques": { title: "Statistiques", description: "Activité clinique, revenus et indicateurs opérationnels.", icon: "chart" },
  "Rapports": { title: "Rapports", description: "Générez et exportez les rapports administratifs.", action: "Générer un rapport", icon: "file" },
  "Paramètres": { title: "Paramètres", description: "Configuration générale et politiques de la plateforme.", icon: "settings" },
};

function ResourcePage({ page, onAdd }: { page: string; onAdd: () => void }) {
  const config = pageConfigs[page] || pageConfigs.Patients;
  const isPatients = page === "Patients" || page === "Mes patients";
  return (
    <>
      <div className="page-heading">
        <div><p className="eyebrow">GESTION CLINIQUE</p><h1>{config.title}</h1><p>{config.description}</p></div>
        {config.action && <button className="primary-button" onClick={onAdd}><Icon name="plus"/>{config.action}</button>}
      </div>
      <section className="panel resource-panel">
        <div className="resource-tools">
          <div className="searchbox table-search"><Icon name="search"/><input placeholder={`Rechercher dans ${page.toLowerCase()}...`} /></div>
          <button className="secondary-button"><Icon name="filter"/> Filtres <span className="filter-count">2</span></button>
          <button className="secondary-button">Trier par : récent <Icon name="chevron" size={14}/></button>
        </div>
        {isPatients ? <PatientTable/> : <DomainTable page={page}/>}
        <div className="pagination"><span>Affichage de 1 à 5 sur 48 résultats</span><div><button disabled>Précédent</button><button className="selected">1</button><button>2</button><button>3</button><button>Suivant</button></div></div>
      </section>
    </>
  );
}

function DomainTable({ page }: { page: string }) {
  const rows = page === "Facturation"
    ? [["FAC-2025-0418","Sophie Bernard","12 juin 2025","145,00 €","Payée"],["FAC-2025-0417","Marc Dubois","12 juin 2025","210,00 €","Partielle"],["FAC-2025-0412","Lina Moreau","11 juin 2025","85,00 €","En attente"]]
    : page === "Ordonnances"
    ? [["ORD-2025-0182","Sophie Bernard","Dr. A. Martin","3 médicaments","Validée"],["ORD-2025-0181","Marc Dubois","Dr. A. Martin","2 médicaments","Brouillon"],["ORD-2025-0179","Lina Moreau","Dr. C. Robert","1 médicament","Validée"]]
    : page === "Mes disponibilités"
    ? [["DIS-0612-MAT","Jeudi 12 juin","08:30 – 12:30","4 rendez-vous","Active"],["DIS-0612-AM","Jeudi 12 juin","14:00 – 17:30","3 rendez-vous","Active"],["DIS-0613-MAT","Vendredi 13 juin","09:00 – 12:00","2 rendez-vous","Active"]]
    : [["RDV-2025-1058","Sophie Bernard","Dr. A. Martin","12 juin · 09:00","Confirmé"],["RDV-2025-1059","Marc Dubois","Dr. A. Martin","12 juin · 10:00","En attente"],["RDV-2025-1060","Lina Moreau","Dr. C. Robert","12 juin · 11:15","Confirmé"]];
  return (
    <div className="table-wrap domain-table"><table><thead><tr><th>Référence</th><th>Patient</th><th>Responsable</th><th>Détail</th><th>Statut</th><th></th></tr></thead>
      <tbody>{rows.map((row) => <tr key={row[0]}>{row.slice(0,4).map((cell, index) => <td key={cell} className={index === 0 ? "mono" : ""}>{cell}</td>)}<td><Badge tone={row[4] === "Confirmé" || row[4] === "Payée" || row[4] === "Validée" ? "success" : "warning"}>{row[4]}</Badge></td><td><button className="row-action"><Icon name="more"/></button></td></tr>)}</tbody>
    </table></div>
  );
}

function AppointmentDrawer({ onClose }: { onClose: () => void }) {
  const [step, setStep] = useState(2);
  const steps = ["Médecin", "Créneau", "Patient", "Détails"];
  return (
    <div className="drawer-backdrop" role="presentation" onMouseDown={onClose}>
      <aside className="drawer" role="dialog" aria-modal="true" aria-labelledby="drawer-title" onMouseDown={(event) => event.stopPropagation()}>
        <div className="drawer-header"><div><p className="eyebrow">PLANIFICATION</p><h2 id="drawer-title">Nouveau rendez-vous</h2></div><button className="icon-button" onClick={onClose}><Icon name="close"/></button></div>
        <div className="stepper">{steps.map((label, index) => <div className={`step ${index + 1 <= step ? "complete" : ""}`} key={label}><span>{index + 1 < step ? <Icon name="check" size={14}/> : index + 1}</span><small>{label}</small></div>)}</div>
        <div className="drawer-body">
          <div className="selection-summary"><div className="avatar avatar-dark">AM</div><div><span>Médecin sélectionné</span><strong>Dr. Alexandre Martin</strong><small>Cardiologie · Cabinet 204</small></div><button onClick={() => setStep(1)}>Modifier</button></div>
          <div className="form-group"><label>Date de consultation</label><div className="field"><Icon name="calendar"/><input type="text" defaultValue="Jeudi 12 juin 2025"/></div></div>
          <div className="form-group"><label>Créneaux disponibles</label><p className="field-help">Les créneaux occupés ou conflictuels sont automatiquement exclus.</p>
            <div className="slots">{["09:00","09:30","10:00","10:30","11:15","14:00","14:30","15:30"].map((slot, index) => <button className={index === 4 ? "selected" : ""} disabled={index === 2} key={slot}>{slot}</button>)}</div>
          </div>
          <div className="availability-note"><Icon name="check"/><div><strong>Créneau disponible</strong><span>Aucun conflit médecin ou patient détecté.</span></div></div>
          <div className="form-group"><label>Durée prévue</label><div className="segmented"><button>20 min</button><button className="selected">30 min</button><button>45 min</button><button>60 min</button></div></div>
        </div>
        <div className="drawer-footer"><button className="secondary-button" onClick={onClose}>Annuler</button><button className="primary-button" onClick={() => step < 4 ? setStep(step + 1) : onClose()}>Continuer <Icon name="arrow"/></button></div>
      </aside>
    </div>
  );
}

const capabilities = [
  { icon: "users" as IconName, title: "Gestion des patients", text: "Dossiers structurés, recherche avancée et archivage logique." },
  { icon: "calendar" as IconName, title: "Rendez-vous intelligents", text: "Disponibilités, détection des conflits et suivi des statuts." },
  { icon: "file" as IconName, title: "Dossiers médicaux", text: "Historique clinique protégé et accessible aux praticiens autorisés." },
  { icon: "pill" as IconName, title: "Prescriptions sécurisées", text: "Contrôle du stock, allergies, posologies et génération PDF." },
  { icon: "invoice" as IconName, title: "Facturation & paiements", text: "Factures automatisées, paiements partiels et reçus." },
  { icon: "chart" as IconName, title: "Pilotage de l’activité", text: "Indicateurs fiables, notifications et rapports exportables." },
];

function LandingPage({ onLogin, onDiscover }: { onLogin: () => void; onDiscover: () => void }) {
  return (
    <div className="public-page">
      <header className="public-nav">
        <Logo />
        <nav><button onClick={onDiscover}>La plateforme</button><button onClick={onDiscover}>Fonctionnalités</button><button onClick={onDiscover}>Sécurité</button></nav>
        <button className="primary-button" onClick={onLogin}>Se connecter <Icon name="arrow"/></button>
      </header>
      <main>
        <section className="hero">
          <div className="hero-copy">
            <div className="trust-label"><Icon name="check" size={14}/> Une gestion clinique plus fluide, chaque jour</div>
            <h1>Le soin au centre.<br/><em>La gestion simplifiée.</em></h1>
            <p>MediCare+ réunit les équipes médicales et administratives autour d’une plateforme claire, sécurisée et conçue pour le fonctionnement réel des établissements de santé.</p>
            <div className="hero-actions">
              <button className="primary-button large" onClick={onLogin}>Se connecter <Icon name="arrow"/></button>
              <button className="secondary-button large" onClick={onDiscover}>Découvrir MediCare+</button>
            </div>
            <div className="hero-proof"><span><Icon name="check"/> Accès par rôle</span><span><Icon name="check"/> Données protégées</span><span><Icon name="check"/> Parcours traçables</span></div>
          </div>
          <div className="hero-visual" aria-label="Aperçu de la plateforme MediCare+">
            <div className="visual-glow"></div>
            <div className="preview-window">
              <div className="preview-top"><div className="mini-logo"><span></span><span></span></div><i></i><i></i><i></i><div className="preview-user"></div></div>
              <div className="preview-body">
                <div className="preview-side">{[1,2,3,4,5].map(item => <span className={item === 1 ? "active" : ""} key={item}></span>)}</div>
                <div className="preview-main">
                  <small>ACTIVITÉ DU JOUR</small><strong>Votre espace de travail</strong>
                  <div className="preview-cards"><i></i><i></i><i></i></div>
                  <div className="preview-panels"><div><b></b>{[1,2,3,4].map(item => <span key={item}></span>)}</div><div><b></b><em></em></div></div>
                </div>
              </div>
            </div>
            <div className="floating-card security-card"><div className="metric-icon teal"><Icon name="check"/></div><div><strong>Session protégée</strong><span>Accès vérifié selon votre rôle</span></div></div>
            <div className="floating-card reminder-card"><div className="metric-icon blue"><Icon name="bell"/></div><div><strong>Rappels automatisés</strong><span>Les patients restent informés</span></div></div>
          </div>
        </section>
        <section className="public-intro" id="plateforme">
          <p className="eyebrow">UNE PLATEFORME, TROIS ESPACES MÉTIERS</p>
          <h2>Chaque équipe accède exactement<br/>à ce dont elle a besoin.</h2>
          <p>Administration, médecins et secrétariat partagent une information cohérente, avec des actions et des données strictement adaptées à leurs responsabilités.</p>
          <div className="role-showcase">
            <article><span>01</span><Icon name="settings"/><h3>Administration</h3><p>Pilotez les utilisateurs, l’activité, les paramètres et les rapports.</p></article>
            <article className="featured"><span>02</span><Icon name="doctor"/><h3>Équipe médicale</h3><p>Concentrez-vous sur vos patients, consultations et prescriptions.</p></article>
            <article><span>03</span><Icon name="calendar"/><h3>Secrétariat</h3><p>Orchestrez l’accueil, les rendez-vous et les encaissements.</p></article>
          </div>
        </section>
        <section className="capabilities" id="fonctionnalites">
          <div className="section-heading"><div><p className="eyebrow">COUVERTURE FONCTIONNELLE</p><h2>Tout le parcours de soin,<br/>dans un même environnement.</h2></div><p>Des outils métier cohérents, reliés aux workflows de votre établissement et conçus pour éviter les erreurs avant qu’elles ne surviennent.</p></div>
          <div className="capability-grid">{capabilities.map(item => <article key={item.title}><div className="capability-icon"><Icon name={item.icon}/></div><h3>{item.title}</h3><p>{item.text}</p><button>En savoir plus <Icon name="arrow" size={14}/></button></article>)}</div>
        </section>
        <section className="security-band">
          <div className="security-symbol"><Icon name="check" size={26}/></div>
          <div><p className="eyebrow">SÉCURITÉ INTÉGRÉE</p><h2>La confidentialité n’est jamais une option.</h2><p>Authentification sécurisée, sessions protégées et autorisations strictes garantissent que chaque information reste accessible uniquement aux utilisateurs habilités.</p></div>
          <div className="security-points"><span><Icon name="check"/> Authentification sécurisée</span><span><Icon name="check"/> Autorisations par rôle</span><span><Icon name="check"/> Déconnexion contrôlée</span></div>
        </section>
        <section className="final-cta"><div><p className="eyebrow">MEDICARE+</p><h2>Prêt à simplifier votre quotidien ?</h2><p>Accédez à votre espace professionnel sécurisé.</p></div><button className="primary-button large" onClick={onLogin}>Se connecter à MediCare+ <Icon name="arrow"/></button></section>
      </main>
      <footer className="public-footer"><Logo/><span>© 2025 MediCare+ · Plateforme de gestion médicale sécurisée</span><div><button>Confidentialité</button><button>Mentions légales</button></div></footer>
    </div>
  );
}

function LoginPage({ onBack, onSuccess, sessionExpired = false }: { onBack: () => void; onSuccess: (user: UserProfile) => void; sessionExpired?: boolean }) {
  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");
  const [status, setStatus] = useState<"idle" | "loading" | "success" | "error" | "server">("idle");
  const [showPassword, setShowPassword] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (!identifier || password.length < 8) { setStatus("error"); return; }
    setStatus("loading");
    try {
      const authenticated = await authenticate(identifier.trim(), password);
      const words = authenticated.nomComplet.split(" ");
      const profile: UserProfile = {
        name: authenticated.nomComplet,
        role: authenticated.role,
        title: authenticated.role === "MEDECIN" ? "Espace clinique" : authenticated.role === "SECRETAIRE" ? "Accueil & facturation" : "Administration système",
        initials: words.slice(0, 2).map(word => word[0]).join("").toUpperCase(),
      };
      setStatus("success");
      window.setTimeout(() => onSuccess(profile), 450);
    } catch (error) {
      setStatus(error instanceof ApiError && error.status === 401 ? "error" : "server");
    }
  }

  return (
    <div className="login-page">
      <section className="login-aside">
        <button className="back-link" onClick={onBack}><Icon name="arrow"/> Retour à l’accueil</button>
        <Logo />
        <div className="login-message"><span className="login-quote">“</span><h2>Une plateforme pensée pour celles et ceux qui prennent soin des autres.</h2><p>Accédez à un environnement fiable, fluide et adapté à votre responsabilité au sein de l’établissement.</p></div>
        <div className="login-assurance"><Icon name="check"/><div><strong>Accès sécurisé et personnalisé</strong><span>Votre espace et vos autorisations sont déterminés automatiquement après connexion.</span></div></div>
      </section>
      <main className="login-main">
        <div className="login-card">
          <div className="mobile-login-logo"><Logo/></div>
          <p className="eyebrow">ESPACE PROFESSIONNEL</p><h1>Bienvenue sur MediCare+</h1><p className="login-subtitle">Renseignez vos identifiants pour accéder à votre espace sécurisé.</p>
          {sessionExpired && status === "idle" && <div className="auth-alert warning"><Icon name="clock"/><div><strong>Votre session a expiré</strong><span>Reconnectez-vous pour continuer en toute sécurité.</span></div></div>}
          {status === "success" && <div className="auth-alert success"><Icon name="check"/><div><strong>Authentification réussie</strong><span>Préparation de votre espace selon vos autorisations...</span></div></div>}
          {status === "error" && <div className="auth-alert error"><Icon name="warning"/><div><strong>Connexion impossible</strong><span>Vérifiez votre identifiant et votre mot de passe.</span></div></div>}
          {status === "server" && <div className="auth-alert error"><Icon name="warning"/><div><strong>Service temporairement indisponible</strong><span>Le serveur n’a pas pu répondre. Réessayez dans quelques instants.</span></div></div>}
          <form onSubmit={submit} noValidate>
            <div className="form-group"><label htmlFor="identifier">Identifiant</label><div className={`field ${status === "error" && !identifier ? "invalid" : ""}`}><Icon name="users"/><input id="identifier" value={identifier} onChange={event => { setIdentifier(event.target.value); setStatus("idle"); }} autoComplete="username" placeholder="Votre identifiant professionnel"/></div>{status === "error" && !identifier && <span className="validation-message">L’identifiant est obligatoire.</span>}</div>
            <div className="form-group"><div className="label-row"><label htmlFor="password">Mot de passe</label><button type="button">Mot de passe oublié ?</button></div><div className={`field ${status === "error" && password.length < 8 ? "invalid" : ""}`}><Icon name="file"/><input id="password" type={showPassword ? "text" : "password"} value={password} onChange={event => { setPassword(event.target.value); setStatus("idle"); }} autoComplete="current-password" placeholder="8 caractères minimum"/><button type="button" className="show-password" onClick={() => setShowPassword(!showPassword)}>{showPassword ? "Masquer" : "Afficher"}</button></div>{status === "error" && password.length < 8 && <span className="validation-message">Saisissez au moins 8 caractères.</span>}</div>
            <label className="remember"><input type="checkbox"/> <span>Maintenir ma session sur cet appareil</span></label>
            <button className="primary-button login-submit" type="submit" disabled={status === "loading"}>{status === "loading" ? <><span className="spinner"></span> Authentification...</> : <>Se connecter <Icon name="arrow"/></>}</button>
          </form>
          <div className="demo-box"><div className="demo-title"><div><Icon name="settings"/><strong>Comptes de démonstration</strong></div><span>ENV. TEST</span></div><p>Sélectionnez un profil, puis utilisez le mot de passe de test configuré côté serveur.</p>
            <div className="demo-users">{Object.entries(DEMO_USERS).map(([username, user]) => <button key={username} onClick={() => { setIdentifier(username); setStatus("idle"); }}><div className="avatar avatar-green">{user.initials}</div><span><strong>{user.role}</strong><small>{username}</small></span><Icon name="arrow"/></button>)}</div>
            <small className="demo-note">Les mots de passe ne sont jamais affichés ni stockés dans l’interface.</small>
          </div>
          <div className="auth-security"><Icon name="check"/> Connexion protégée · Session chiffrée · Accès contrôlé</div>
        </div>
      </main>
    </div>
  );
}

function ForbiddenPage({ onReturn }: { onReturn: () => void }) {
  return <div className="state-page"><div className="state-code">403</div><div className="state-icon"><Icon name="warning" size={30}/></div><p className="eyebrow">ACCÈS NON AUTORISÉ</p><h1>Cette page est protégée.</h1><p>Votre rôle ne dispose pas des autorisations nécessaires pour consulter ce module. Si vous pensez qu’il s’agit d’une erreur, contactez votre administrateur.</p><button className="primary-button" onClick={onReturn}>Retour à mon dashboard <Icon name="arrow"/></button></div>;
}

export default function App() {
  const sessionExpired = window.location.hash === "#session-expired";
  const [view, setView] = useState<AppView>(sessionExpired ? "login" : window.location.hash === "#forbidden" ? "forbidden" : "landing");
  const [user, setUser] = useState<UserProfile | null>(null);
  const [active, setActive] = useState("Dashboard");
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const pageTitle = useMemo(() => active === "Dashboard" ? "Tableau de bord" : active, [active]);
  function login(profile: UserProfile) { setUser(profile); setActive("Dashboard"); setView("app"); }
  async function logout() {
    await apiLogout().catch(() => undefined);
    setUser(null); setActive("Dashboard"); setView("landing"); setMobileOpen(false);
  }
  if (view === "landing") return <LandingPage onLogin={() => setView("login")} onDiscover={() => document.getElementById("plateforme")?.scrollIntoView({ behavior: "smooth" })}/>;
  if (view === "login") return <LoginPage sessionExpired={sessionExpired} onBack={() => setView("landing")} onSuccess={login}/>;
  if (view === "forbidden") return <ForbiddenPage onReturn={() => setView("app")}/>;
  if (!user) return <LoginPage sessionExpired={sessionExpired} onBack={() => setView("landing")} onSuccess={login}/>;
  return (
    <div className="app-shell">
      <div className={mobileOpen ? "sidebar-mobile open" : "sidebar-mobile"}><Sidebar user={user} onLogout={logout} active={active} onChange={(value) => { setActive(value); setMobileOpen(false); }}/></div>
      {mobileOpen && <div className="mobile-backdrop" onClick={() => setMobileOpen(false)}></div>}
      <Sidebar user={user} onLogout={logout} active={active} onChange={setActive}/>
      <div className="main-shell">
        <Topbar user={user} onLogout={logout} onMenu={() => setMobileOpen(true)}/>
        <main className="content" aria-label={pageTitle}>
          {active === "Dashboard"
            ? <Dashboard user={user} onAdd={() => user.role === "ADMIN" ? setActive("Utilisateurs") : setDrawerOpen(true)}/>
            : <ResourcePage page={active} onAdd={() => active.includes("Rendez-vous") ? setDrawerOpen(true) : undefined}/>}
          <footer><span>MediCare+ · Interface sécurisée JWT</span><span>API Spring Boot · Dernière synchronisation à 08:42</span></footer>
        </main>
      </div>
      {drawerOpen && <AppointmentDrawer onClose={() => setDrawerOpen(false)}/>}
    </div>
  );
}
