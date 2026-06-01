# Guide de Création d'un Compte Admin

## Compte Admin Créé

**Informations du compte:**
- **Nom complet:** Admin User
- **Email:** admin@messagerie.com
- **Mot de passe:** SecurePassword123!
- **Rôle:** ADMIN

## 3 Méthodes pour Insérer le Compte Admin dans la Base de Données

### Méthode 1: Exécuter le Script SQL (RECOMMANDÉ)

#### Étape 1: Ouvrir MySQL et se connecter

```bash
# Windows
mysql -u root -p

# Entrez le mot de passe (appuyez sur Entrée si aucun mot de passe)
```

#### Étape 2: Charger et exécuter le script

```sql
USE messagerie_db;

source c:/Java/messagerie/backend/create_admin.sql;
```

Ou copier-coller la commande SQL directement:

```sql
USE messagerie_db;

INSERT INTO user (first_name, last_name, email, password_hash, phone, role, blocked, two_factor_enabled, created_at, updated_at) 
VALUES ('Admin', 'User', 'admin@messagerie.com', 'pTYlflcDqa1+OpNNc3Y+DYokJTOg1tauV8LJNcmdre8Tpix1WX0pmLTPPT1/f0TZ', '', 'ADMIN', false, false, '2026-05-31 16:40:27', '2026-05-31 16:40:27');
```

#### Étape 3: Vérifier que l'admin a été créé

```sql
SELECT id, first_name, last_name, email, role FROM user WHERE email = 'admin@messagerie.com';
```

---

### Méthode 2: Utiliser une Interface Graphique (MySQL Workbench)

1. **Ouvrir MySQL Workbench**
2. **Se connecter à la base de données** (localhost:3306, user: root)
3. **Sélectionner la base de données:** `messagerie_db`
4. **Cliquer sur "File" → "Open SQL Script"** et sélectionner `create_admin.sql`
5. **Exécuter la requête** (Ctrl + Shift + Enter ou le bouton Execute)

---

### Méthode 3: Utiliser Docker (si MySQL est en Docker)

```bash
# Récupérer l'ID du conteneur MySQL
docker ps

# Exécuter la commande SQL dans le conteneur
docker exec -i <container_id> mysql -u root messagerie_db < c:\Java\messagerie\backend\create_admin.sql
```

---

## Se Connecter avec le Compte Admin

### Via l'Application Frontend

1. **Ouvrir la page de connexion** (http://localhost:5173/login)
2. **Email:** admin@messagerie.com
3. **Mot de passe:** SecurePassword123!

### Via l'API (pour tester)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@messagerie.com",
    "password": "SecurePassword123!"
  }'
```

Response attendue:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "...",
  "user": {
    "id": 1,
    "firstName": "Admin",
    "lastName": "User",
    "email": "admin@messagerie.com",
    "role": "ADMIN"
  }
}
```

---

## Vérification de l'Accès Admin

Une fois connecté en tant qu'admin, vous pouvez accéder à:

### API Admin Endpoints (tous protégés par `@PreAuthorize("hasRole('ADMIN')")`)

- **GET** `/api/admin/stats` - Voir les statistiques système
- **GET** `/api/admin/users` - Lister tous les utilisateurs
- **GET** `/api/admin/logs` - Afficher les logs de sécurité
- **PUT** `/api/admin/users/{id}/block` - Bloquer un utilisateur
- **PUT** `/api/admin/users/{id}/unblock` - Débloquer un utilisateur
- **DELETE** `/api/admin/users/{id}` - Supprimer un utilisateur

### Frontend Admin Dashboard

- Accéder à http://localhost:5173/admin/dashboard
- Gestion des utilisateurs
- Affichage des logs de sécurité
- Statistiques du système

---

## Modifier le Mot de Passe Admin

Si vous souhaitez changer le mot de passe de l'admin:

### Option 1: Via l'API (une fois connecté)

```bash
# À implémenter: endpoint de changement de mot de passe
```

### Option 2: Générer un nouveau hash et mettre à jour la base de données

```bash
# Exécuter l'utilitaire CreateAdminUser avec un nouveau mot de passe
cd c:\Java\messagerie\backend
java CreateAdminUser Admin User admin@messagerie.com "NouveauMotDePasse123!"
```

Puis mettre à jour la base de données:

```sql
UPDATE user 
SET password_hash = '<NOUVEAU_HASH>' 
WHERE email = 'admin@messagerie.com';
```

---

## Créer d'Autres Comptes Admin

Pour créer d'autres comptes admin avec le même utilitaire:

```bash
cd c:\Java\messagerie\backend

# Compiler (si pas déjà fait)
javac CreateAdminUser.java

# Générer le hash pour un nouvel admin
java CreateAdminUser "Prénom" "Nom" "email@example.com" "MotDePasse123!"
```

Puis copier la commande SQL INSERT générée et l'exécuter dans MySQL.

---

## Notes de Sécurité

⚠️ **IMPORTANT:**
1. **Changez le mot de passe par défaut** dès que possible
2. **Activez la 2FA** sur le compte admin pour plus de sécurité
3. **Gardez les logs de sécurité** à jour
4. **Limitez le nombre de comptes admin** au strict nécessaire
5. **Protégez les fichiers** CreateAdminUser.java et create_admin.sql en production

---

## Dépannage

### Erreur: "Access denied for user 'root'@'localhost'"
- Vérifiez que MySQL est démarré
- Utilisez le mot de passe correct (par défaut vide sur dev)

### Erreur: "Table 'messagerie_db.user' doesn't exist"
- Assurez-vous que le backend a démarré au moins une fois (pour créer les tables via Hibernate)
- Vérifiez que la base de données `messagerie_db` existe

### Erreur: "Duplicate entry for key 'email'"
- L'email existe déjà dans la base de données
- Utilisez un email différent ou supprimez l'utilisateur existant

---

## Fichiers Générés

- **CreateAdminUser.java** - Utilitaire pour générer les hashes de mot de passe
- **create_admin.sql** - Script SQL pour insérer l'admin
- **ADMIN_CREATION_GUIDE.md** - Ce guide
