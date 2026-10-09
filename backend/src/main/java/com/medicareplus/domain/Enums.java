package com.medicareplus.domain;

public final class Enums {
    private Enums() {}
    public enum Role { ADMIN, MEDECIN, SECRETAIRE }
    public enum StatutRendezVous { EN_ATTENTE, CONFIRME, ANNULE, TERMINE, ABSENT }
    public enum StatutPaiement { EN_ATTENTE, PARTIELLEMENT_PAYEE, PAYEE, NON_PAYEE }
    public enum ModePaiement { ESPECES, CARTE, VIREMENT, EN_LIGNE }
    public enum CanalNotification { EMAIL, SMS }
    public enum TypeNotification { RAPPEL_RDV, NOUVEAU_RDV, CONFIRMATION, ANNULATION }
    public enum StatutEnvoi { ENVOYE, ECHEC }
    public enum JourSemaine { LUNDI, MARDI, MERCREDI, JEUDI, VENDREDI, SAMEDI, DIMANCHE }
}
