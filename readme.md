# Database Setup & Application Start

## Demo Accounts

- **alice@secchamp.com** (User) - password: **password123**
- **admin@secchamp.com** (Admin) - password: **admin123**  
- **test@secchamp.com** (Manager) - password: **test123**
- **bob@secbooks.com** (User) - password: **password123**
- **carol@secbooks.com** (User - Suspended) - password: **password123**

## Running the Application

1. **Start with Docker Compose:**
   ```bash
   docker compose up --build
   ```
   The database will be automatically initialized with demo data on first startup.

2. **Clean start (removes all data and containers in case any db error for the first start):**
   ```bash
   docker compose down -v
   docker compose up --build
   ```

## Database Configuration

- **Database:** secchamp2025
- **Username:** root  
- **Password:** P@ssw0rd
- **Port:** 3306

## Troubleshooting

### Database Issues
- Database initializes automatically on first startup via `db/init-db.sql`
- Use `docker logs mysql-db` to check database logs
- Use `docker logs sechamp2025_app` to check application logs

### Authentication Issues
- Demo account credentials:
  - alice@secchamp.com: `password123`
  - admin@secchamp.com: `admin123`
  - test@secchamp.com: `test123`
  - bob@secbooks.com / carol@secbooks.com: `password123`

### Container Issues
- Use `docker ps` to check running containers
- Use `docker compose down -v` for complete cleanup

## Start the application
```
cd sourcecodes
docker-compose up --build
```

---

Spring Boot Application at `http://localhost:8080`<br>
Tomcat server at `http://localhost:18080`

---
