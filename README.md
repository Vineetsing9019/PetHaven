# PetHaven: Animal Shelter Pet Adoption API

A REST API for an animal shelter. People register, browse the animals and send an adoption request; the shelter's admin approves or rejects it, and each animal's status follows along (Available → Pending → Adopted).

Built with **Java 17, Spring Boot, Spring Security, Spring Data JPA, Spring Data REST and MySQL**.

## Features

- **Register and log in** with email and password. Passwords are stored hashed with BCrypt.
- **Two roles:** a USER can browse animals and send adoption requests; an ADMIN manages animals and categories and decides on requests.
- **Adoption workflow with rules:**
  - A request can only be sent for an animal that is `AVAILABLE`; the animal becomes `PENDING`.
  - Approving a request marks the animal `ADOPTED`; rejecting it makes the animal `AVAILABLE` again.
  - A request that was already decided can't be approved or rejected twice.
- **Clear errors:** 404 when an animal, user or request doesn't exist, and 400 when a rule is broken.
- **Animals and categories** come with full CRUD from Spring Data REST (paging and sorting included). The user and request tables are kept private.

## API

Every call except register uses HTTP Basic auth (email + password).

| Method | Endpoint | Who | What it does |
|---|---|---|---|
| POST | `/users/register` | Anyone | Create an account (`name`, `email`, `password`, `phone`) |
| GET | `/users/login` | Logged in | Check the login and return my profile |
| GET | `/animals`, `/animals/{id}` | User, Admin | List or view animals |
| POST, PUT, PATCH, DELETE | `/animals/**` | Admin | Add, edit or remove animals |
| GET | `/categories`, `/categories/{id}` | User, Admin | List or view categories |
| POST, PUT, PATCH, DELETE | `/categories/**` | Admin | Add, edit or remove categories |
| POST | `/adoptions/request/{animalId}` | User | Send an adoption request (`message`) |
| GET | `/adoptions/my` | User | My requests and their status |
| GET | `/adoptions/all` | Admin | All requests |
| PUT | `/adoptions/{id}/approve` | Admin | Approve a request |
| PUT | `/adoptions/{id}/reject` | Admin | Reject a request |

### Example

```bash
# Register
curl -X POST http://localhost:8080/users/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Asha","email":"asha@example.com","password":"secret123","phone":"9000000000"}'

# Browse animals
curl -u asha@example.com:secret123 http://localhost:8080/animals

# Ask to adopt animal 1
curl -u asha@example.com:secret123 -X POST http://localhost:8080/adoptions/request/1 \
  -H "Content-Type: application/json" -d '{"message":"I have a big garden and lots of time."}'
```

## Data model

```
Category 1 ── * Animal 1 ── * AdoptionRequest * ── 1 User
```

- **Animal:** name, breed, age, gender, description, image URL, status
- **AdoptionRequest:** request date, message, status (`PENDING` / `APPROVED` / `REJECTED`)
- **User:** name, email (unique), BCrypt password, phone, role

## Project structure

```
src/main/java/com/itvedant/pethaven
├── controller/      AdoptionController, UserController
├── services/        AdoptionService (the adoption rules), UserService
├── repositories/    JPA repositories (animals and categories exposed by Spring Data REST)
├── models/          Animal, Category, AdoptionRequest, User, UserDto
├── SecurityConfiguration.java   role rules, BCrypt, HTTP Basic
└── MyUserDetailsService.java    loads users by email for Spring Security
```

## Run it locally

Needs Java 17+ and MySQL running on port 3306.

```bash
git clone https://github.com/Vineetsing9019/PetHaven.git
cd PetHaven

# The MySQL password comes from an environment variable, never from the code
export DB_PASSWORD=your_mysql_password      # Windows PowerShell: $env:DB_PASSWORD="your_mysql_password"

./mvnw spring-boot:run                      # Windows: mvnw.cmd spring-boot:run
```

The `pethaven_db` database and its tables are created on first start. The API runs at `http://localhost:8080`.

Everyone who registers is a USER. To make an admin, change the role in MySQL:

```sql
UPDATE users SET role = 'ROLE_ADMIN' WHERE email = 'admin@example.com';
```

## What I learned

- Securing endpoints by role with Spring Security, and storing passwords safely with BCrypt
- Modelling one-to-many relationships with JPA and avoiding JSON loops with `@JsonIgnore`
- Keeping business rules in the service layer and returning proper HTTP status codes
- Exposing simple CRUD with Spring Data REST while keeping sensitive tables private

## Next steps

- Swagger (OpenAPI) documentation
- Unit tests for the adoption rules with JUnit and Mockito
- JWT login and a React front end
