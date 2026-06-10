# 📊 Guide de Déploiement Local (Pour le Binôme)

Bonjour ! Si vous lisez ce fichier, c'est que vous allez lancer l'infrastructure *SympNet* complète sur votre ordinateur.
L'architecture a été mise à jour pour refléter vos 3 microservices distincts (API Backend, Dashboard Web, Moteur IA).

## 📋 Prérequis

1. Vous devez avoir *Docker Desktop* (ou Docker Engine) installé et lancé sur votre machine.
2. Assurez-vous que les ports suivants sont libres sur votre PC : 8000, 5057, 5002, 5432 (PostgreSQL), 9090, 9091, 3001, 3002.

---

## 🚀 Étape 1 : Lancer l'API Backend et le Dashboard (.NET)

Le fichier docker-compose.yml du dossier web-service va créer : la base de données, l'API .NET, le Dashboard Blazor, Prometheus et Grafana.

1. Ouvrez un terminal (PowerShell ou Invite de commandes).
2. Déplacez-vous dans le dossier du backend :
   ```bash
   cd chemin\vers\votre\dossier\fin\sympnet-web-service
   ```
3. Lancez la construction et l'exécution des conteneurs :
   ```bash
   docker compose up -d --build
   ```
   *(Le téléchargement des images .NET et PostgreSQL peut prendre quelques minutes).*

4. **🔍 Vérification :**
   * **L'API Backend** tourne sur : http://localhost:5057
   * **Le Dashboard Web** tourne sur : http://localhost:5002 *(Ouvrez ce lien dans votre navigateur !)*
   * La base de données tourne sur `localhost:5432`
   * Prometheus tourne sur http://localhost:9090
   * Grafana tourne sur http://localhost:3001

---

## 🧠 Étape 2 : Lancer le Moteur IA (FastAPI Python)

Le moteur d'intelligence artificielle utilise LLaMA-3 (via Groq API) et s'exécute avec Python.

1. Allez chercher votre clé API Groq (votre binôme doit vous la fournir).
2. Dans un terminal, déplacez-vous dans le dossier de l'IA :
   ```bash
   cd chemin\vers\votre\dossier\fin\sympnet-ai
   ```
3. Exécutez le docker compose en injectant la variable d'environnement (remplacez `VOTRE_CLE_ICI` par la vraie clé) :

   * **Sur Windows (PowerShell) :**
     ```powershell
     $env:GROQ_API_KEY="VOTRE_CLE_ICI"
     docker compose up -d --build
     ```

   * **Sur Mac/Linux (Bash) :**
     ```bash
     GROQ_API_KEY="VOTRE_CLE_ICI" docker compose up -d --build
     ```

4. **🔍 Vérification :**
   * Le moteur IA tourne sur : http://localhost:8000
   * La documentation Swagger IA est sur : http://localhost:8000/docs
   * Prometheus (IA) tourne sur http://localhost:9091
   * Grafana (IA) tourne sur http://localhost:3002

---

## 🛠️ Commandes Utiles

* Pour voir si tout tourne bien : `docker ps`
* Pour voir les logs : `docker logs -f nom_du_conteneur`
* Pour tout arrêter (à faire dans chaque dossier) : `docker compose down`
