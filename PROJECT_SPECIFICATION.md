# Specificatie pentru reconstruirea aplicatiei File Importer

Acest document este sursa de adevar pentru reconstruirea proiectului de la zero. Aplicatia trebuie livrata completa, rulabila si testata, nu doar sub forma de prototip. Cerintele descriu comportamentul asteptat; agentii care reconstruiesc proiectul pot alege detaliile interne doar acolo unde documentul nu impune explicit o solutie.

## 1. Scop

Aplicatia administreaza o structura Parent-Child si permite importul asincron al acestor date din fisiere CSV.

Utilizatorul trebuie sa poata:

- crea, consulta, modifica si sterge Parents;
- crea, consulta in contextul Parent-ului, modifica si sterge Children;
- edita proprietati dinamice sub forma de obiect JSON;
- deschide direct un Parent folosind URL-ul sau permanent;
- incarca un CSV si reveni imediat la o pagina de status;
- urmari progresul real al importului;
- consulta istoricul importurilor si erorile fiecarui rand invalid.

## 2. Tehnologii si livrare

Se va crea o singura aplicatie modulara Spring Boot, cu frontend si backend impachetate impreuna intr-un JAR executabil.

Tehnologii obligatorii:

- Java 25;
- Spring Boot 4.1;
- Spring MVC;
- Thymeleaf;
- Bootstrap 5 incarcat din CDN;
- vanilla JavaScript numai unde este necesar, inclusiv pentru polling-ul importului;
- Spring Data JPA;
- Spring Batch;
- PostgreSQL;
- Liquibase;
- Maven si Maven Wrapper;
- Jakarta Bean Validation;
- JUnit 5 si Testcontainers PostgreSQL.

Nu se vor folosi React, Angular, Vue, Node.js, npm, WebFlux, H2, Flyway sau Lombok.

Arhitectura trebuie sa separe clar:

- web/controllers si DTO-uri de prezentare;
- servicii si reguli de business;
- persistenta;
- procesare batch;
- functionalitati comune, precum parsarea JSON si tratarea erorilor.

Controllers nu contin logica de business. Se foloseste exclusiv constructor injection. Entitatile JPA nu sunt expuse direct prin endpoint-uri JSON.

## 3. Modelul de domeniu

### 3.1 Parent

Campuri obligatorii:

- `id`: UUID version 7, generat de aplicatie;
- `externalKey`: cheie de business optionala pentru import, unica global intre Parents;
- `displayName`: obligatoriu, maximum 255 de caractere;
- `dynamicProperties`: `Map<String, Object>`, stocat ca PostgreSQL `jsonb`, niciodata null;
- `createdAt`;
- `updatedAt`;
- `version`: optimistic locking;
- relatie lazy one-to-many cu Children.

`displayName` si `externalKey` sunt campuri statice permise. Celelalte atribute de business extensibile se pastreaza in `dynamicProperties`.

### 3.2 Child

Campuri obligatorii:

- `id`: UUID version 7, generat de aplicatie;
- `parent`: obligatoriu, relatie lazy many-to-one;
- `externalKey`: cheie de business optionala pentru import, unica global intre Children;
- `displayName`: obligatoriu, maximum 255 de caractere;
- `dynamicProperties`: `Map<String, Object>`, stocat ca PostgreSQL `jsonb`, niciodata null;
- `createdAt`;
- `updatedAt`;
- `version`: optimistic locking.

`displayName` si `externalKey` sunt campuri statice permise. Celelalte atribute de business extensibile se pastreaza in `dynamicProperties`.

Reguli:

- un Parent poate avea zero sau mai multi Children;
- un Child apartine exact unui Parent;
- nu se foloseste `CascadeType.ALL`;
- un Parent care are Children nu poate fi sters;
- baza de date trebuie sa aplice si ea restrictia de stergere;
- un Child nu poate fi mutat la alt Parent prin CRUD sau import;
- accesarea/modificarea/stergerea unui Child prin URL verifica faptul ca acesta apartine Parent-ului din URL;
- numele se salveaza fara spatii la inceput si sfarsit;
- crearile manuale nu solicita si nu genereaza `externalKey`; cheia este folosita de import;
- listele de Parents si Children sunt ordonate alfabetic dupa `displayName`.

## 4. Proprietati dinamice JSON

Formularele pentru Parent si Child contin un textarea in care utilizatorul editeaza JSON.

Reguli:

- valoarea trebuie sa fie un obiect JSON;
- array-urile, valorile scalare si JSON `null` nu sunt acceptate;
- continutul lipsa sau gol se transforma in `{}`;
- JSON-ul malformat produce un mesaj de validare clar, asociat campului;
- formularul invalid este reafisat cu valorile introduse si HTTP 200;
- pagina de detalii afiseaza JSON-ul formatat lizibil;
- in baza de date se salveaza un obiect gol, nu `null`.

## 5. Pagini si rute

Aplicatia trebuie sa ofere urmatoarele rute:

```text
GET  /
GET  /parents
GET  /parents/new
POST /parents
GET  /parents/{parentId}
GET  /parents/{parentId}/edit
POST /parents/{parentId}
POST /parents/{parentId}/delete

GET  /parents/{parentId}/children/new
POST /parents/{parentId}/children
GET  /parents/{parentId}/children/{childId}/edit
POST /parents/{parentId}/children/{childId}
POST /parents/{parentId}/children/{childId}/delete

GET  /imports
POST /imports
GET  /imports/{importId}
GET  /api/imports/{importId}/progress
```

Toate operatiile POST reusite folosesc Post/Redirect/Get si afiseaza un mesaj flash corespunzator.

### 5.1 Lista de Parents

Pagina `/parents` afiseaza:

- numele fiecarui Parent;
- identificatorul;
- data ultimei modificari;
- link catre URL-ul permanent;
- actiune de creare;
- o stare goala explicita daca nu exista inregistrari.

### 5.2 Detaliile Parent-ului

Pagina `/parents/{parentId}` afiseaza:

- identificatorul Parent-ului;
- numele;
- proprietatile dinamice;
- datele de creare si modificare;
- versiunea;
- lista de Children si numarul lor;
- actiuni de creare Child, editare Parent si stergere Parent;
- actiuni de editare si stergere pentru fiecare Child;
- pentru fiecare Child: identificator, nume si proprietati dinamice;
- o stare goala daca Parent-ul nu are Children.

Nu este necesara o pagina separata de detalii pentru Child.

### 5.3 Navigare directa

Parent-ul selectat este determinat exclusiv din `/parents/{parentId}`.

Este interzisa pastrarea selectiei in:

- sesiunea HTTP;
- cookies;
- local storage;
- stare globala a aplicatiei.

Copierea URL-ului intr-un tab nou trebuie sa incarce acelasi Parent. Un UUID valid inexistent sau un identificator malformat returneaza HTTP 404 cu pagina prietenoasa.

### 5.4 Interfata

- toate paginile sunt responsive;
- Bootstrap 5 este incarcat din CDN;
- se folosesc fragmente Thymeleaf reutilizabile pentru header, navigatie, alerte, erori de validare, footer si bara de progres;
- formularele afiseaza sumarul erorilor si erori langa campuri;
- editarile si stergerile trimit versiunea curenta pentru optimistic locking;
- designul si limba interfetei trebuie sa fie coerente pe toate paginile.

## 6. Import CSV

### 6.1 Fluxul de incarcare

Pagina `/imports` contine formularul de upload si istoricul importurilor, de la cel mai nou la cel mai vechi.

La `POST /imports`, aplicatia trebuie sa:

1. valideze fisierul;
2. il copieze intr-un director controlat, in afara resurselor statice;
3. creeze inregistrarea importului;
4. lanseze asincron un job Spring Batch;
5. redirectioneze imediat la `/imports/{importId}`.

Fisierul nu se proceseaza integral in request-ul HTTP si nu se incarca integral in memorie.

### 6.2 Validarea si stocarea fisierului

Se valideaza:

- fisier prezent;
- fisier nevid;
- nume original prezent;
- extensie `.csv`, case-insensitive;
- dimensiune maxima configurabila, implicit 20 MB;
- header exact, inclusiv ordinea si scrierea coloanelor.

Header obligatoriu:

```csv
recordType,parentExternalKey,parentDisplayName,childExternalKey,childDisplayName,properties
```

Securitate la stocare:

- numele original nu este folosit drept nume fizic;
- se elimina orice componenta de cale din numele original;
- numele stocat este generat folosind un UUID si extensia `.csv`;
- calea este normalizata si trebuie sa ramana sub directorul configurat;
- traversal-ul si evadarea prin symlink sunt respinse;
- daca validarea continutului esueaza dupa copiere, copia este stearsa;
- UTF-8 si BOM UTF-8 sunt acceptate.

### 6.3 Formatul datelor

Exemplu valid:

```csv
recordType,parentExternalKey,parentDisplayName,childExternalKey,childDisplayName,properties
P,PARENT-001,First parent,,,"{""country"":""RO"",""active"":true}"
C,PARENT-001,,CHILD-001,First child,"{""score"":10}"
C,PARENT-001,,CHILD-002,Second child,"{""score"":20}"
```

Reguli generale:

- separatorul este virgula, iar caracterul de quoting/escaping este ghilimeaua dubla, conform conventiilor CSV uzuale;
- sunt acceptate campuri quoted, virgule si linii noi in interiorul unui camp quoted;
- sunt acceptate terminatoare de linie uzuale (`LF` si `CRLF`);
- quoting-ul malformat produce esecul importului cu un mesaj clar;
- liniile goale sunt ignorate;
- fiecare rand trebuie sa aiba numarul asteptat de coloane;
- valorile text sunt trimise;
- `recordType` este case-sensitive si poate fi doar `P` sau `C`;
- `properties` gol devine `{}`;
- `properties` completat trebuie sa fie un obiect JSON valid;
- identificatorii UUID nu sunt preluati din fisier;
- erorile mentioneaza linia fizica din fisier la care parserul a incheiat citirea inregistrarii CSV; pentru campuri multiline aceasta poate fi ultima linie a inregistrarii.

Rand `P`:

- `parentExternalKey` este obligatoriu, maximum 255;
- `parentDisplayName` este obligatoriu, maximum 255;
- `childExternalKey` si `childDisplayName` trebuie sa fie goale.

Rand `C`:

- `parentExternalKey` este obligatoriu, maximum 255;
- `childExternalKey` este obligatoriu, maximum 255;
- `childDisplayName` este obligatoriu, maximum 255;
- `parentDisplayName` trebuie sa fie gol.

### 6.4 Reguli de import

- Parents sunt procesati complet inaintea Children, indiferent de ordinea randurilor din fisier;
- fiecare fisier este citit streaming, folosind procesare chunk-oriented;
- chunk size este configurabil, implicit 100;
- Parent este creat sau actualizat dupa `parentExternalKey`;
- Child este creat sau actualizat dupa `childExternalKey`;
- compararea cheilor externe este exacta si case-sensitive;
- la actualizare, `displayName` si intregul obiect `dynamicProperties` sunt inlocuite cu valorile din CSV; proprietatile JSON nu sunt imbinate;
- un Child existent poate fi actualizat numai sub acelasi Parent;
- un Child care refera un Parent inexistent este invalid;
- cheile externe duplicate in acelasi fisier sunt respinse;
- duplicatele Parent si Child sunt urmarite separat;
- prima aparitie a unei chei in pasul corespunzator revendica acea cheie, iar orice aparitie ulterioara este clasificata drept duplicat, chiar daca primul rand a esuat ulterior la alta validare;
- randurile valide sunt salvate chiar daca alte randuri sunt invalide;
- UUID version 7 este generat de aplicatie pentru inregistrarile noi.

Coduri de eroare obligatorii pentru raportarea randurilor:

```text
INVALID_COLUMN_COUNT
INVALID_RECORD_TYPE
MISSING_REQUIRED_VALUE
VALUE_TOO_LONG
UNEXPECTED_VALUE
INVALID_PROPERTIES
DUPLICATE_EXTERNAL_KEY
PARENT_NOT_FOUND
CHILD_PARENT_CHANGE
```

Spring Batch trebuie sa includa explicit:

- Job;
- pasi chunk-oriented pentru Parents si Children;
- ItemReader;
- ItemProcessor;
- ItemWriter;
- JobExecutionListener;
- StepExecutionListener;
- SkipListener.

Parametrii jobului includ:

- `importId`;
- `storedFilePath`;
- un parametru unic de executie.

Joburile nu pornesc automat la startup.

## 7. Evidenta importului

### 7.1 FileImport

Campuri obligatorii:

- `id`: UUID version 7;
- `originalFilename`;
- `storedFilename`: unic;
- `status`;
- `totalRows`;
- `processedRows`;
- `successfulRows`;
- `failedRows`;
- `skippedRows`;
- `errorMessage`;
- `batchJobExecutionId`;
- `createdAt`;
- `startedAt`;
- `completedAt`;
- `updatedAt`;
- `version` pentru optimistic locking.

Statusuri:

```text
UPLOADED
QUEUED
RUNNING
COMPLETED
COMPLETED_WITH_ERRORS
FAILED
```

Fluxul normal este `QUEUED -> RUNNING -> status terminal`. Un job finalizat cu randuri esuate sau sarite devine `COMPLETED_WITH_ERRORS`.

### 7.2 Erori pe rand

Erorile sunt persistate separat si contin cel putin:

- referinta la import;
- numarul randului;
- codul erorii;
- mesajul explicativ.

Pagina de detalii afiseaza erorile ordonate dupa rand. Pentru volume mari se poate limita lista initiala, dar interfata trebuie sa indice explicit limita.

### 7.3 Semantica contoarelor

- `successfulRows`: randuri salvate sau actualizate cu succes;
- `failedRows`: randuri invalide;
- `skippedRows`: randuri duplicate sarite;
- `processedRows`: suma randurilor reusite, esuate si sarite, fara a depasi `totalRows`;
- contoarele se persista la limita de chunk, nu dupa fiecare rand.

Procentul se calculeaza astfel:

```text
processedRows * 100 / totalRows
```

Rezultatul este intreg si limitat la intervalul 0-100. Un import terminat cu succes, inclusiv `COMPLETED_WITH_ERRORS`, afiseaza 100%. Pentru zero randuri, procentul este 0 in executie si 100 dupa finalizare.

## 8. Status si progres live

Pagina `/imports/{importId}` afiseaza:

- numele original al fisierului;
- statusul;
- bara Bootstrap de progres si procentul;
- total, procesate, reusite, esuate si sarite;
- mesajul de stare;
- erorile pe rand;
- mesaj final de succes, avertizare sau eroare.

Endpoint-ul:

```text
GET /api/imports/{importId}/progress
```

returneaza un DTO JSON dedicat, similar cu:

```json
{
  "importId": "0198f4be-28d7-7c42-a17f-9d2343ef8001",
  "status": "RUNNING",
  "totalRows": 10000,
  "processedRows": 4250,
  "successfulRows": 4200,
  "failedRows": 45,
  "skippedRows": 5,
  "percentage": 42,
  "finished": false,
  "message": "Processing file"
}
```

JavaScript-ul paginii:

- foloseste Fetch API;
- interogheaza aproximativ o data pe secunda pentru `QUEUED` si `RUNNING`;
- asteapta finalizarea request-ului curent inainte de urmatorul, pentru a evita suprapunerea;
- actualizeaza bara, procentul, statusul, mesajul si toate contoarele;
- opreste polling-ul la orice status terminal;
- reincearca dupa erori temporare de retea, cu intarziere limitata;
- nu simuleaza progresul pe baza timpului scurs;
- actualizeaza sau reincarca lista de erori atunci cand importul se termina.

Un import inexistent returneaza HTTP 404 atat pentru pagina HTML, cat si pentru endpoint-ul de progres.

## 9. Erori si concurenta

Se foloseste un `@ControllerAdvice` global si pagini prietenoase.

Comportament obligatoriu:

- resursa inexistenta sau relatie Child-Parent incorecta: HTTP 404;
- identificator URL malformat: HTTP 404;
- JSON invalid in formular: eroare de validare pe formular;
- fisier CSV invalid la upload: mesaj clar si revenire la formular;
- request multipart peste limita: HTTP 413 cu mesaj prietenos;
- esec batch: import `FAILED`, mesaj persistat si afisat;
- versiune depasita la editare/stergere: HTTP 409 cu mesaj de conflict;
- stergere Parent cu Children: HTTP 409 cu explicatie;
- erorile neasteptate nu expun stack trace utilizatorului.

## 10. PostgreSQL si Liquibase

PostgreSQL este singura baza de date a aplicatiei.

Configuratie obligatorie:

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

Liquibase creeaza schema completa pentru:

- `parent`;
- `child`;
- `file_import`;
- `file_import_error`;
- indecsii si constrangerile necesare;
- tabelele metadata Spring Batch, daca acestea nu sunt initializate separat si controlat.

Tipuri PostgreSQL obligatorii:

- `uuid` pentru identificatori;
- `jsonb` pentru proprietati dinamice;
- `timestamp with time zone` pentru timestamps.

Schema trebuie sa includa nume explicite pentru PK, FK, unique constraints si indexes, inclusiv:

- FK restrictiv `child.parent_id -> parent.id`;
- index pe `child.parent_id`;
- unique constraint pentru cheia externa Parent;
- unique constraint pentru cheia externa Child;
- unique constraint pentru `file_import.stored_filename`;
- index cronologic pentru imports;
- FK cu stergere cascade si index pentru erorile unui import.

Hibernate nu creeaza si nu actualizeaza schema. Aplicatia trebuie sa esueze la startup daca migrarea Liquibase sau validarea schemei esueaza.

## 11. Configurare si operare

Se ofera configurare prin proprietati si variabile de mediu pentru:

- URL, utilizator si parola PostgreSQL;
- directorul fisierelor incarcate, implicit `./uploads`;
- dimensiunea maxima, implicit 20 MB;
- chunk size, implicit 100;
- executorul asincron.

Executorul trebuie sa aiba capacitate limitata si oprire controlata. Fisierele incarcate sunt in afara resurselor statice si nu sunt versionate.

Fisierele acceptate sunt pastrate dupa terminarea sau esecul jobului pentru audit. Aplicatia nu implementeaza stergere automata; o politica de retentie ulterioara trebuie tratata ca functionalitate separata.

Proiectul include Docker Compose pentru PostgreSQL cu health check si volum persistent.

Pornire locala:

```bash
docker compose up -d
./mvnw spring-boot:run
```

## 12. Teste si criterii de acceptanta

Suita trebuie sa foloseasca JUnit 5 si Testcontainers PostgreSQL, fara H2, si sa acopere cel putin:

- creare Parent;
- creare Child si relatie obligatorie;
- nume trimise si validare maximum 255;
- UUID version 7 pentru Parent, Child si FileImport;
- persistenta si citirea JSONB;
- obiect JSON gol in loc de null;
- JSON malformat si JSON cu radacina non-obiect;
- URL direct pentru Parent;
- Parent inexistent si UUID malformat returneaza 404;
- Child accesat sub Parent gresit returneaza 404;
- actualizare si stergere cu optimistic locking;
- refuzul stergerii unui Parent cu Children;
- migrarea Liquibase, tipurile, FK-urile si indecsii;
- upload lipsa, gol, extensie gresita, fisier prea mare si header gresit;
- securitatea numelui si a caii de stocare;
- BOM, linii goale, numar gresit de coloane si numere de rand;
- regulile randurilor `P` si `C`;
- proprietati JSON invalide in CSV;
- chei externe duplicate;
- Parent inexistent pentru Child;
- interzicerea mutarii unui Child;
- import asincron, streaming/chunk si procesarea Parent inainte de Child;
- upsert dupa chei externe;
- import partial cu randuri valide si invalide;
- persistenta erorilor si semantica tuturor contoarelor;
- calculul progresului, inclusiv zero randuri si status terminal;
- endpoint-ul JSON de progres si raspunsul 404;
- trecerea prin statusurile importului si tratarea esecului batch.

Inainte de livrare se executa:

```bash
./mvnw clean verify
```

Toate testele si verificarile de compilare trebuie sa treaca.

## 13. Livrabile

Proiectul reconstruit trebuie sa contina:

- Maven Wrapper si `pom.xml`;
- codul sursa complet;
- sabloanele Thymeleaf si fragmentele comune;
- configurarea Bootstrap CDN si JavaScript-ul de polling;
- changelog-urile Liquibase;
- configurarea Spring Batch si executorul asincron;
- configurarea PostgreSQL;
- Docker Compose;
- teste unitare, web si de integrare;
- fisiere CSV exemplu;
- `.gitignore` pentru uploads si artefacte;
- README cu arhitectura, configurarea, pornirea, formatul CSV si rularea testelor.

Aplicatia este considerata finalizata numai daca porneste cu comenzile documentate, migreaza schema corect, permite CRUD complet, deschide direct URL-urile Parent, importa CSV asincron si afiseaza progresul real pana la statusul terminal.
