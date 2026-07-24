# File Importer — Technical Architecture and Engineering Constraints

## 1. Rolul agentului

Construiește aplicația completă conform `FEATURE.md`.

Agentul este responsabil pentru:

- arhitectură;
- alegerea structurii interne;
- implementarea backend;
- implementarea server-side frontend;
- persistență;
- Spring Batch;
- concurență;
- securitatea stocării fișierelor;
- infrastructura locală;
- migrarea bazei de date;
- testare automată;
- documentație;
- quality gates.

Cerințele funcționale din `FEATURE.md` sunt sursa de adevăr pentru comportament.

---

# 2. Stack obligatoriu

Folosește:

- Java 25;
- Spring Boot 4.1;
- Spring MVC;
- Thymeleaf;
- Bootstrap 5 prin CDN;
- vanilla JavaScript numai unde este necesar;
- Spring Data JPA;
- Spring Batch;
- PostgreSQL;
- Liquibase;
- Maven;
- Maven Wrapper;
- Jakarta Bean Validation;
- JUnit 5;
- Testcontainers PostgreSQL.

---

# 3. Tehnologii interzise

Nu folosi:

- React;
- Angular;
- Vue;
- Node.js;
- npm;
- WebFlux;
- H2;
- Flyway;

---

# 4. Stil arhitectural

Aplicația este o singură aplicație modulară Spring Boot.

Frontend-ul și backend-ul sunt împachetate împreună în același JAR executabil.

Separă clar:

```text
web/
  controllers/
  dto/

service/
  business rules

persistence/
  entities/
  repositories/

batch/
  jobs/
  steps/
  readers/
  processors/
  writers/
  listeners/

common/
  json/
  errors/
  validation/
  utilities/
```

Structura exactă poate varia, dar responsabilitățile trebuie să rămână separate.

---

# 5. Reguli de design

Obligatoriu:

- controllers fără logică de business;
- constructor injection exclusiv;
- entitățile JPA nu sunt expuse direct prin endpoint-uri JSON;
- DTO-uri dedicate pentru input/output web unde este necesar;
- servicii pentru reguli de business;
- repository layer separat;
- cod modular și testabil;
- preferă clase mici cu responsabilitate clară;
- evită duplicarea logicii;
- folosește principiile SOLID fără over-engineering.

---

# 6. Persistență

## PostgreSQL

PostgreSQL este singura bază de date.

Tipuri obligatorii:

- `uuid` pentru identificatori;
- `jsonb` pentru dynamic properties;
- `timestamp with time zone` pentru timestamps.

Hibernate nu trebuie să creeze sau să modifice schema.

Configurație:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate

  liquibase:
    enabled: true

  batch:
    job:
      enabled: false
```

Aplicația trebuie să eșueze la startup dacă:

- Liquibase eșuează;
- schema nu corespunde mapping-ului Hibernate.

---

# 7. Mapping JPA

## Parent

- UUID v7 generat în aplicație;
- `externalKey` unic;
- `displayName`;
- JSONB pentru `dynamicProperties`;
- timestamps;
- optimistic locking prin `@Version`;
- relație lazy one-to-many cu Child.

## Child

- UUID v7 generat în aplicație;
- relație lazy many-to-one către Parent;
- `externalKey` unic;
- `displayName`;
- JSONB pentru `dynamicProperties`;
- timestamps;
- optimistic locking.

Reguli tehnice:

- nu folosi `CascadeType.ALL`;
- FK Child -> Parent trebuie să fie restrictiv la delete;
- integritatea trebuie impusă și în baza de date, nu doar în Java.

---

# 8. Liquibase

Liquibase creează schema completă pentru:

- `parent`;
- `child`;
- `file_import`;
- `file_import_error`;
- indecși;
- constraints;
- Spring Batch metadata tables, dacă nu sunt inițializate separat într-un mod controlat.

Folosește nume explicite pentru:

- primary keys;
- foreign keys;
- unique constraints;
- indexes.

Trebuie să existe cel puțin:

- FK restrictiv `child.parent_id -> parent.id`;
- index pe `child.parent_id`;
- unique constraint Parent external key;
- unique constraint Child external key;
- unique constraint `file_import.stored_filename`;
- index cronologic pentru imports;
- FK cu cascade delete pentru import errors;
- index pentru erorile unui import.

---

# 9. Dynamic JSON

Folosește PostgreSQL `jsonb`.

În Java proprietățile sunt modelate ca:

```java
Map<String, Object>
```

Creează o componentă comună pentru:

- parsare;
- validare root object;
- normalizare empty -> `{}`;
- pretty-print;
- mapping pentru formular.

Nu duplica această logică între Parent, Child și import CSV.

---

# 10. Server-side frontend

Folosește:

- Spring MVC;
- Thymeleaf;
- Bootstrap 5 CDN;

Creează fragmente Thymeleaf reutilizabile pentru:

- header;
- navigation;
- alerts;
- validation errors;
- footer;
- progress bar.

Nu introduce toolchain frontend separat.

Nu introduce npm.

---

# 11. Spring Batch

Importul CSV trebuie implementat cu Spring Batch.

---

# 12. Procesarea CSV

Fișierul trebuie citit streaming.

Nu încărca întregul fișier în memorie.

Procesarea trebuie să fie chunk-oriented.

Chunk size:

- configurabil;
- implicit `100`.

Parents trebuie procesați înainte de Children indiferent de ordinea fizică a rândurilor.

Implementarea trebuie să suporte corect:

- quoted CSV fields;
- comma în câmp quoted;
- newline în câmp quoted;
- LF;
- CRLF;
- UTF-8;
- UTF-8 BOM;
- numărarea liniei fizice relevante pentru erori.

Folosește un parser CSV robust, compatibil cu cerințele. Nu implementa manual un parser CSV naiv cu `String.split(",")`.

---

# 13. Import asincron

`POST /imports` nu execută procesarea integral în thread-ul HTTP.

Flux tehnic:

```text
HTTP upload
    ->
validate
    ->
secure file storage
    ->
persist FileImport
    ->
submit async Spring Batch job
    ->
redirect immediately
```

Executorul asincron trebuie să fie:

- configurabil;
- bounded;
- cu capacity limit;
- cu shutdown controlat.

---

# 14. Stocarea sigură a fișierelor

Fișierele uploadate sunt stocate în afara resurselor statice.

Reguli:

- numele original nu este folosit drept nume fizic;
- elimină componentele de path din numele original;
- generează numele stocat cu UUID + `.csv`;
- normalizează calea;
- calea finală trebuie să rămână sub upload root;
- respinge path traversal;
- respinge evadarea prin symlink;
- dacă validarea conținutului eșuează după copiere, șterge copia.

Fișierele valide sunt păstrate după succes sau eșec pentru audit.

Nu implementa retenție automată în această funcționalitate.

---

# 15. Concurență

Folosește optimistic locking pentru:

- Parent;
- Child;
- FileImport unde este necesar.

UI-ul trebuie să transmită versiunea curentă la editare și ștergere.

Mapează conflictul de versiune la HTTP 409.

---

# 16. Error handling

Folosește `@ControllerAdvice` global.

Trebuie să existe pagini/răspunsuri prietenoase pentru:

- 404;
- 409;
- 413;
- erori de validare;
- erori neașteptate.

Nu expune stack trace utilizatorului.

Pentru endpoint-urile JSON folosește DTO-uri de eroare adecvate.

---

# 17. Progress polling

Frontend-ul folosește Fetch API.

Polling:

- aproximativ 1 secundă;
- numai pentru `QUEUED` și `RUNNING`;
- următorul request începe numai după finalizarea celui curent;
- fără request-uri suprapuse;
- retry cu întârziere limitată pentru erori temporare;
- stop la status terminal;
- progresul este exclusiv bazat pe date reale persistate.

Nu simula progresul pe baza timpului.

---

# 18. Persistarea progresului

Contoarele de import sunt actualizate la limita de chunk, nu după fiecare rând.

Păstrează consistența pentru:

- `totalRows`;
- `processedRows`;
- `successfulRows`;
- `failedRows`;
- `skippedRows`.

Listener-ele Spring Batch trebuie să sincronizeze corect:

- timestamps;
- status;
- counters;
- batch job execution id;
- mesajele de eroare.

---

# 19. Configurare

Permite configurare prin properties și environment variables pentru:

- PostgreSQL URL;
- PostgreSQL username;
- PostgreSQL password;
- upload directory;
- upload maximum size;
- chunk size;
- async executor.

Valori implicite:

```text
upload directory: ./uploads
maximum file size: 20 MB
chunk size: 100
```

---

# 20. Docker

Include Docker Compose pentru PostgreSQL.

Obligatoriu:

- health check;
- persistent volume;
- configurare simplă pentru dezvoltare locală.

Pornire:

```bash
docker compose up -d
./mvnw spring-boot:run
```

---

# 21. Testare

Folosește:

- JUnit 5;
- Spring Boot Test;
- Spring MVC tests;
- integration tests;
- Testcontainers PostgreSQL.

Nu folosi H2.

Testele trebuie să ruleze pe PostgreSQL real prin Testcontainers acolo unde comportamentul bazei de date contează.

Acoperă inclusiv:

- mapping JPA;
- JSONB;
- UUID v7;
- Liquibase;
- FK;
- constraints;
- indexes;
- optimistic locking;
- MVC validation;
- CRUD;
- CSV parser;
- upload security;
- Spring Batch;
- async import;
- counters;
- progress endpoint;
- error handling.

---

# 22. Maven și quality gate

Proiectul conține:

- `pom.xml`;
- Maven Wrapper.

Comanda obligatorie înainte de livrare:

```bash
./mvnw clean verify
```

Livrarea nu este completă dacă:

- build-ul nu compilează;
- testele nu trec;
- migrarea bazei de date eșuează;
- aplicația nu pornește cu pașii documentați.

---

# 23. Livrabile tehnice

Proiectul final trebuie să conțină:

- Maven Wrapper;
- `pom.xml`;
- cod sursă complet;
- Thymeleaf templates;
- Thymeleaf fragments;
- Bootstrap CDN setup;
- JavaScript pentru polling;
- Liquibase changelogs;
- Spring Batch configuration;
- async executor configuration;
- PostgreSQL configuration;
- Docker Compose;
- teste unitare;
- teste web;
- teste de integrare;
- fișiere CSV exemplu;
- `.gitignore` pentru uploads și artefacte;
- README.

README trebuie să documenteze:

- arhitectura;
- configurarea;
- pornirea;
- PostgreSQL;
- Docker;
- formatul CSV;
- rularea testelor.

---

# 24. Principii de implementare

Agentul trebuie să:

- păstreze soluția simplă;
- evite infrastructura inutilă;
- evite framework-uri care nu sunt cerute;
- nu modifice cerințele funcționale;
- nu înlocuiască Spring MVC cu WebFlux;
- nu înlocuiască PostgreSQL cu o bază embedded;
- nu proceseze CSV-ul integral în memorie;
- nu introducă frontend SPA;
- nu expună entități JPA direct prin API;
- prefere cod explicit și ușor de urmărit;
- scrie teste pentru comportamentul critic;
- mențină proiectul rulabil după fiecare etapă majoră.
