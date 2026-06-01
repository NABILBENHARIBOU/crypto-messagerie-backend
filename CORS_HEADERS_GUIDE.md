# CORS Headers Configuration - Frontend/Backend Communication

## Overview
Cette documentation explique comment configurer les headers CORS pour la communication sécurisée entre le frontend (port 5173) et le backend (port 8080).

## CORS Configuration Actuelle

### Origines Autorisées
- `http://localhost:5173` (Frontend Vite dev)
- `http://localhost:3000` (Frontend alternative)

### Méthodes HTTP Autorisées
- `GET` - Récupérer des données
- `POST` - Créer des données
- `PUT` - Remplacer des données
- `PATCH` - Mise à jour partielle
- `DELETE` - Supprimer des données
- `OPTIONS` - CORS preflight

### Configuration de Cache
- **Max Age**: 3600 secondes (1 heure)
- **Allow Credentials**: `true` (pour les tokens JWT)

---

## Headers Acceptés (Frontend → Backend)

### 1. **Authorization** (IMPORTANT)
```
Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...
```
- **Description**: JWT access token pour l'authentification
- **Format**: `Bearer {token}`
- **Utilisé dans**: Toutes les requêtes authentifiées
- **Exemple**: 
  ```javascript
  headers: {
    'Authorization': `Bearer ${accessToken}`
  }
  ```

### 2. **Content-Type**
```
Content-Type: application/json
```
- **Description**: Type du contenu de la requête
- **Valeurs courantes**:
  - `application/json` - JSON payload
  - `application/x-www-form-urlencoded` - Form data
  - `multipart/form-data` - File uploads
- **Utilisé dans**: POST, PUT, PATCH avec body
- **Exemple**:
  ```javascript
  headers: {
    'Content-Type': 'application/json'
  }
  ```

### 3. **Accept**
```
Accept: application/json
```
- **Description**: Type de contenu attendu dans la réponse
- **Valeur**: `application/json`
- **Utilisé dans**: Toutes les requêtes
- **Exemple**:
  ```javascript
  headers: {
    'Accept': 'application/json'
  }
  ```

### 4. **X-Requested-With**
```
X-Requested-With: XMLHttpRequest
```
- **Description**: Indique que c'est une requête AJAX/XHR
- **Valeur**: `XMLHttpRequest`
- **Utilisé dans**: Toutes les requêtes (optionnel avec Fetch, nécessaire avec XMLHttpRequest)
- **Exemple**:
  ```javascript
  headers: {
    'X-Requested-With': 'XMLHttpRequest'
  }
  ```

### 5. **X-User-Id** (Optionnel)
```
X-User-Id: 1
```
- **Description**: ID de l'utilisateur pour le contexte supplémentaire
- **Format**: ID numérique
- **Utilisé dans**: Certains endpoints (optionnel)
- **Exemple**:
  ```javascript
  headers: {
    'X-User-Id': userId.toString()
  }
  ```

### 6. **X-CSRF-Token** (Sécurité)
```
X-CSRF-Token: csrf-token-string
```
- **Description**: Token CSRF pour sécuriser les requêtes state-changing
- **Utilisé dans**: POST, PUT, PATCH, DELETE
- **Exemple**:
  ```javascript
  headers: {
    'X-CSRF-Token': csrfToken
  }
  ```

### 7. **Accept-Language** (Optionnel)
```
Accept-Language: en-US,en;q=0.9,fr;q=0.8
```
- **Description**: Langues préférées pour la réponse
- **Utilisé dans**: Toutes les requêtes (défini par le navigateur)

### 8. **Origin** (Automatique)
```
Origin: http://localhost:5173
```
- **Description**: Origine de la requête (défini automatiquement par le navigateur)
- **Utilisé dans**: Toutes les requêtes cross-origin

---

## Headers de Réponse (Backend → Frontend)

### 1. **Authorization**
```
Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...
```
- **Retourné par**: `/api/auth/login`, `/api/auth/register`, `/api/auth/refresh`
- **Contient**: Nouveau JWT access token
- **Frontend utilise**: Stocker et utiliser pour les requêtes suivantes

### 2. **Content-Type**
```
Content-Type: application/json; charset=UTF-8
```
- **Retourné par**: Tous les endpoints
- **Description**: Type du contenu de la réponse

### 3. **X-Total-Count** (Pagination)
```
X-Total-Count: 150
```
- **Retourné par**: GET endpoints avec collections (ex: `/api/messages`)
- **Description**: Nombre total d'éléments disponibles
- **Frontend utilise**: Calculer le nombre de pages

### 4. **X-Page-Number** (Pagination)
```
X-Page-Number: 0
```
- **Retourné par**: GET paginated endpoints
- **Description**: Numéro de page actuelle (0-indexed)
- **Frontend utilise**: Afficher la page en cours

### 5. **X-Page-Size** (Pagination)
```
X-Page-Size: 20
```
- **Retourné par**: GET paginated endpoints
- **Description**: Nombre d'éléments par page
- **Frontend utilise**: Afficher la taille de page

### 6. **X-Auth-Token** (Authentification)
```
X-Auth-Token: token-string
```
- **Retourné par**: Endpoints d'authentification
- **Description**: Token d'authentification personnalisé (si utilisé)

---

## Exemples d'Utilisation

### Frontend Fetch API

#### 1. Login (Sans authentification)
```javascript
const response = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    'X-Requested-With': 'XMLHttpRequest'
  },
  body: JSON.stringify({
    email: 'user@example.com',
    password: 'password123'
  })
});

const data = await response.json();
const { accessToken, refreshToken } = data;

// Stocker les tokens
localStorage.setItem('accessToken', accessToken);
localStorage.setItem('refreshToken', refreshToken);
```

#### 2. Requête Authentifiée
```javascript
const accessToken = localStorage.getItem('accessToken');

const response = await fetch('http://localhost:8080/api/messages', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    'X-Requested-With': 'XMLHttpRequest'
  }
});

const messages = await response.json();
```

#### 3. Créer un Message
```javascript
const accessToken = localStorage.getItem('accessToken');

const response = await fetch('http://localhost:8080/api/messages/send', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    'X-Requested-With': 'XMLHttpRequest',
    'X-User-Id': userId.toString()
  },
  body: JSON.stringify({
    recipientId: 2,
    encryptedContent: 'encrypted-message',
    algorithm: 'AES-GCM',
    iv: 'initialization-vector',
    authTag: 'authentication-tag'
  })
});

const result = await response.json();
```

#### 4. Rafraîchir le Token
```javascript
const refreshToken = localStorage.getItem('refreshToken');

const response = await fetch('http://localhost:8080/api/auth/refresh', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    'X-Requested-With': 'XMLHttpRequest'
  },
  body: JSON.stringify({
    token: refreshToken
  })
});

const { accessToken, refreshToken: newRefreshToken } = await response.json();
localStorage.setItem('accessToken', accessToken);
localStorage.setItem('refreshToken', newRefreshToken);
```

### Frontend Axios

#### Setup avec Interceptors
```javascript
import axios from 'axios';

// Créer instance Axios
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
    'X-Requested-With': 'XMLHttpRequest',
    'Accept': 'application/json'
  }
});

// Interceptor pour ajouter le token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`;
  }
  return config;
});

// Interceptor pour gérer les erreurs 401 (token expiré)
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        const refreshToken = localStorage.getItem('refreshToken');
        const { data } = await api.post('/auth/refresh', { token: refreshToken });
        
        localStorage.setItem('accessToken', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);
        
        // Retry original request
        originalRequest.headers['Authorization'] = `Bearer ${data.accessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        // Refresh failed, redirect to login
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }
    
    return Promise.reject(error);
  }
);

export default api;
```

#### Utilisation avec Axios
```javascript
// Login
const { data } = await api.post('/auth/login', {
  email: 'user@example.com',
  password: 'password123'
});

// Get messages (token ajouté automatiquement)
const messages = await api.get('/messages');

// Send message (token ajouté automatiquement)
await api.post('/messages/send', {
  recipientId: 2,
  encryptedContent: 'message'
});
```

### React Exemple Complet

```javascript
import { useCallback, useEffect, useState } from 'react';

export const useApi = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const request = useCallback(async (
    url,
    { method = 'GET', body = null, headers = {} } = {}
  ) => {
    setLoading(true);
    setError(null);

    try {
      const accessToken = localStorage.getItem('accessToken');
      
      const response = await fetch(`http://localhost:8080/api${url}`, {
        method,
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
          'X-Requested-With': 'XMLHttpRequest',
          ...(accessToken && { 'Authorization': `Bearer ${accessToken}` }),
          ...headers
        },
        body: body ? JSON.stringify(body) : null
      });

      if (response.status === 401) {
        // Token expiré, rafraîchir
        const refreshToken = localStorage.getItem('refreshToken');
        const refreshResponse = await fetch('http://localhost:8080/api/auth/refresh', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
          },
          body: JSON.stringify({ token: refreshToken })
        });

        const refreshData = await refreshResponse.json();
        localStorage.setItem('accessToken', refreshData.accessToken);
        localStorage.setItem('refreshToken', refreshData.refreshToken);

        // Retry original request
        return request(url, { method, body, headers });
      }

      if (!response.ok) {
        throw new Error(`API Error: ${response.statusText}`);
      }

      return await response.json();
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  return { request, loading, error };
};
```

---

## Gestion des Erreurs CORS

### Erreur: "Access to XMLHttpRequest... has been blocked by CORS policy"
- **Cause**: Origine non autorisée ou headers manquants
- **Solution**: Vérifier que le frontend URL correspond à `allowedOrigins`

### Erreur: "Request header field X-Custom-Header is not allowed"
- **Cause**: Header personnalisé non listé dans `allowedHeaders`
- **Solution**: Ajouter le header à la liste `allowedHeaders` dans SecurityConfig

### Erreur: "Response to preflight request... does not have HTTP ok status"
- **Cause**: Réponse OPTIONS échouée
- **Solution**: Vérifier que SecurityConfig autorise les requêtes OPTIONS

---

## Sécurité

### Bonnes Pratiques

1. **Toujours utiliser HTTPS en production**
   ```java
   configuration.setAllowedOrigins(List.of("https://messagerie.example.com"));
   ```

2. **Stocker les tokens de manière sécurisée**
   ```javascript
   // ❌ Mauvais: localStorage pour access token
   localStorage.setItem('accessToken', token);
   
   // ✅ Bon: Memory pour access token, httpOnly cookie pour refresh token
   sessionStorage.setItem('accessToken', token);
   // ou utiliser un variable en memory
   let accessToken = token;
   ```

3. **Valider les headers côté backend**
   - Vérifier le token JWT à chaque requête
   - Utiliser HTTPS en production

4. **Limiter les origines autorisées**
   ```java
   configuration.setAllowedOrigins(List.of(
       "https://app.messagerie.com",
       "https://admin.messagerie.com"
   ));
   ```

5. **Utiliser Credentials = true avec prudence**
   - Ne pas utiliser avec `allowedOrigins = ["*"]`
   - Doit spécifier les origines explicitement

---

## Configuration Production

Pour la production, mettre à jour `application-prod.properties`:

```properties
# CORS Configuration Production
app.cors.allowed-origins=https://app.messagerie.com,https://admin.messagerie.com

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000
jwt.refresh-expiration=604800000

# Database
spring.datasource.url=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
```

---

## Checklist Frontend

- [ ] Ajouter `Authorization: Bearer {token}` pour requêtes authentifiées
- [ ] Gérer l'erreur 401 avec refresh token
- [ ] Ajouter `Content-Type: application/json` pour POST/PUT/PATCH
- [ ] Ajouter `Accept: application/json` à toutes les requêtes
- [ ] Utiliser `X-Requested-With: XMLHttpRequest` pour les XHR
- [ ] Stocker les tokens de manière sécurisée
- [ ] Implémenter le retry automatique avec refresh token
- [ ] Tester les requêtes CORS en développement

---

## Fichiers de Référence

- **Backend Config**: `src/main/java/messagerie/backend/config/SecurityConfig.java`
- **CORS Headers Config**: `src/main/java/messagerie/backend/config/CorsHeadersConfig.java`
- **JWT Documentation**: `JWT_AUTHENTICATION.md`
