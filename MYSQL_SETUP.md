# Configuration MySQL pour le Backend

## 1. Installation de MySQL

### Windows
```bash
# Installer MySQL (via Chocolatey)
choco install mysql

# Ou télécharger depuis https://dev.mysql.com/downloads/mysql/
```

### macOS
```bash
brew install mysql
```

### Linux (Ubuntu/Debian)
```bash
sudo apt-get install mysql-server
```

---

## 2. Démarrer le service MySQL

### Windows
```bash
net start MySQL80
# ou
mysqld --console
```

### macOS / Linux
```bash
mysql.server start
# ou
sudo systemctl start mysql
```

---

## 3. Créer la base de données

```sql
-- Se connecter à MySQL
mysql -u root -p

-- Créer la base de données
CREATE DATABASE IF NOT EXISTS messagerie_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Créer un utilisateur (optionnel mais recommandé)
CREATE USER 'messagerie_user'@'localhost' IDENTIFIED BY 'messagerie_password';
GRANT ALL PRIVILEGES ON messagerie_db.* TO 'messagerie_user'@'localhost';
FLUSH PRIVILEGES;

-- Vérifier
SHOW DATABASES;
```

---

## 4. Configurer le Backend

### Variables d'environnement (Production)

```bash
# .env ou variables système
DB_HOST=localhost
DB_PORT=3306
DB_NAME=messagerie_db
DB_USER=root
DB_PASSWORD=root
```

### Profils Spring

**Développement (local) :**
```bash
java -jar backend.jar --spring.profiles.active=dev
```

**Production :**
```bash
java -jar backend.jar --spring.profiles.active=prod \
  -Dspring.datasource.url=jdbc:mysql://prod-server:3306/messagerie_db \
  -Dspring.datasource.username=$DB_USER \
  -Dspring.datasource.password=$DB_PASSWORD
```

---

## 5. Tester la connexion

```bash
cd c:\Java\messagerie\backend

# Compilation
mvn clean compile

# Tests
mvn test

# Run
mvn spring-boot:run
```

**Vérifier que le serveur démarre sans erreur de connexion à MySQL.**

---

## 6. Console MySQL pour debugging

```bash
# Se connecter
mysql -u root -p messagerie_db

# Voir les tables créées
SHOW TABLES;

# Voir la structure d'une table
DESC users;

# Requête simple
SELECT * FROM users;
```

---

## Notes

- **ddl-auto=update** (dev) : crée/met à jour les tables automatiquement
- **ddl-auto=validate** (prod) : valide seulement, ne modifie pas
- **MySQL 5.7+** supporté
- Port par défaut : `3306`
