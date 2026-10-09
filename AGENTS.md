# AGENTS.md — MediCare+ Frontend

> **Guide de référence pour les agents IA et les développeurs** travaillant sur le frontend React + Vite du projet MediCare+.
> Ce document décrit la structure canonique, les conventions et les règles à respecter.

---

## 1. Vue d'ensemble

Ce dépôt contient le **frontend React** de la plateforme **MediCare+**, une application de gestion médicale (patients, praticiens, rendez-vous, prescriptions, facturation).

Le frontend est une **SPA React 19** construite avec **Vite**, stylée avec **Tailwind CSS v4**, et **TypeScript 5.7**. Il consomme exclusivement l'**API REST Spring Boot** située dans le dossier `backend/` du même dépôt.

> ⚠️ **Le frontend ne se connecte jamais directement à MySQL.** Toute donnée passe par l'API REST.

---

## 2. Stack technique

| Couche | Technologie | Version |
|--------|-------------|---------|
| Framework UI | React + React DOM | 19 |
| Build tool | Vite | 8 |
| Langage | TypeScript | 5.7 |
| Styling | Tailwind CSS (plugin Vite) | v4 |
| Formatage | oxfmt | dernière |
| Gestionnaire de paquets | pnpm | 12+ |
| Runtime | Node.js | 22+ |

---

## 3. Structure du projet

Structure canonique du frontend. **Commencer par les fichiers pertinents pour la tâche** ; ne suivre les imports ou n'inspecter d'autres fichiers que si nécessaire.
