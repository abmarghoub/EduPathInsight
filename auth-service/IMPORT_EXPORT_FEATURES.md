# Fonctionnalités d'Import/Export d'Utilisateurs

## Description

Le service d'authentification permet maintenant d'importer et d'exporter des utilisateurs via des fichiers CSV/Excel.

## Import d'Utilisateurs

### Via Data Ingestion Service (Recommandé)

L'import passe par le service Data Ingestion qui valide les fichiers et traite les données.

**Endpoint** :
```
POST /api/auth/admin/users/import
```

**Paramètres** :
- `file` (multipart/form-data) : Fichier CSV ou Excel
- `viaIngestion` (boolean, default: true) : Utiliser le service Data Ingestion
- `async` (boolean, default: false) : Traitement asynchrone

**Exemple avec curl** :
```bash
curl -X POST http://localhost:8080/api/auth/admin/users/import \
  -H "Authorization: Bearer <token>" \
  -F "file=@users.csv" \
  -F "viaIngestion=true" \
  -F "async=false"
```

### Format du fichier CSV/Excel

Les fichiers doivent contenir les colonnes suivantes :

**CSV** :
```csv
username,email,password,first_name,last_name
student1,student1@example.com,password123,John,Doe
student2,student2@example.com,password456,Jane,Smith
```

**Colonnes requises** :
- `username` : Nom d'utilisateur (obligatoire)
- `email` : Adresse email (obligatoire)
- `password` : Mot de passe (optionnel, généré automatiquement si absent)
- `first_name` : Prénom (optionnel)
- `last_name` : Nom (optionnel)

## Export d'Utilisateurs

### Export CSV

**Endpoint** :
```
GET /api/auth/admin/users/export/csv
```

**Exemple avec curl** :
```bash
curl -X GET http://localhost:8080/api/auth/admin/users/export/csv \
  -H "Authorization: Bearer <token>" \
  -o users_export.csv
```

### Export Excel

**Endpoint** :
```
GET /api/auth/admin/users/export/excel
```

**Exemple avec curl** :
```bash
curl -X GET http://localhost:8080/api/auth/admin/users/export/excel \
  -H "Authorization: Bearer <token>" \
  -o users_export.xlsx
```

### Format d'export

Les fichiers exportés contiennent les colonnes suivantes :
- `id` : ID de l'utilisateur
- `username` : Nom d'utilisateur
- `email` : Adresse email
- `email_verified` : Email vérifié (true/false)
- `enabled` : Compte activé (true/false)
- `roles` : Rôles (séparés par des virgules)
- `created_at` : Date de création

## Flux d'Import via Data Ingestion

1. **Upload** : Le fichier est envoyé à l'auth-service
2. **Validation** : Le fichier est transféré au data-ingestion-service pour validation
3. **Parsing** : Le data-ingestion-service parse le fichier (CSV/Excel)
4. **Traitement** : 
   - Création/mise à jour des nœuds dans Neo4j (graphe)
   - Création/mise à jour des utilisateurs dans PostgreSQL (auth-service)
5. **Notification** : Notification asynchrone via RabbitMQ
6. **Réponse** : Retour des statistiques d'import

## Permissions

Tous les endpoints d'import/export nécessitent le rôle **ROLE_ADMIN**.

## Exemples de fichiers

### users.csv
```csv
username,email,password
student1,student1@example.com,Pass123!
student2,student2@example.com,Pass456!
admin_test,admin@example.com,Admin123!
```

### users.xlsx
Fichier Excel avec les mêmes colonnes que le CSV.

## Notes importantes

- Les mots de passe doivent respecter les règles de sécurité (minimum 6 caractères)
- Si un utilisateur existe déjà (par username ou email), il sera mis à jour
- Les mots de passe générés automatiquement sont envoyés par email
- Le traitement asynchrone est recommandé pour les gros volumes de données
- Les fichiers doivent être au format UTF-8 pour éviter les problèmes d'encodage


