# GigFlow — teljes oktatási alkalmazás

A GigFlow egy React + TypeScript kliensből és Clean Architecture szerint tagolt Spring Boot backendből álló fellépésszervező alkalmazás. A zenekari tagok a saját meghívásaikra válaszolnak, a zenekarvezető pedig új fellépéseket hoz létre. Az adatokat PostgreSQL tárolja, a sémát Flyway migrációk kezelik.

## Fő funkciók

- HTTP Basic bejelentkezés lokális demonstrációs felhasználókkal;
- szerepkör alapú felület és API-jogosultság (`MEMBER`, `LEADER`);
- fellépések listázása, létrehozása és meghívotti válaszok összesítése;
- a válaszadó azonosítása a hitelesített principalból, így más tag válasza nem módosítható;
- vezérelt, validált és akadálymentesen címkézett React űrlapok;
- egységes `ProblemDetail` hibaválaszok;
- PostgreSQL/JPA perzisztencia és Flyway migrációk;
- OpenAPI szerződés a `backend/src/main/resources/openapi.yaml` fájlban.

## Felépítés

- `frontend/`: React 19, TypeScript 7, Vite 8;
- `backend/`: Java 21, Spring Boot 4.1.1;
- `backend/src/main/resources/static/`: a frontend produkciós buildje;
- `compose.yaml`: lokális PostgreSQL.

## Gyors indítás

PostgreSQL:

```bash
docker compose up -d
```

Frontend build:

```bash
cd frontend
npm ci
npm run build
```

Backend és a beépített webes kliens:

```bash
cd ../backend
SPRING_PROFILES_ACTIVE=local \
GIGFLOW_DEMO_ANNA_PASSWORD=anna-demo \
GIGFLOW_DEMO_BENCE_PASSWORD=bence-demo \
GIGFLOW_DEMO_CSILLA_PASSWORD=csilla-demo \
GIGFLOW_DEMO_LEADER_PASSWORD=leader-demo \
./mvnw spring-boot:run
```

Ezután az alkalmazás a <http://localhost:8080/> címen érhető el.

| Felhasználó | A fenti példa jelszava | Szerepkör |
|---|---|---|
| `anna` | `anna-demo` | zenekari tag |
| `bence` | `bence-demo` | zenekari tag |
| `csilla` | `csilla-demo` | zenekari tag |
| `leader` | `leader-demo` | zenekarvezető |

A példajelszavak kizárólag helyi oktatási futtatásra valók. A kliens a jelszót csak a React memóriaállapotában tartja, `localStorage`-ba nem írja.

## Fejlesztői mód

A backend után külön terminálban:

```bash
cd frontend
npm run dev
```

A Vite az `/api` kéréseket a `http://127.0.0.1:8080` backendhez továbbítja.

## REST végpontok

| Metódus | Útvonal | Feladat | Jogosultság |
|---|---|---|---|
| `GET` | `/api/session` | Bejelentkezett felhasználó adatai | hitelesített felhasználó |
| `GET` | `/api/performances` | Fellépések listázása | `MEMBER` vagy `LEADER` |
| `POST` | `/api/performances` | Fellépés létrehozása meghívottakkal | `LEADER` |
| `PUT` | `/api/performances/{id}/response` | Saját válasz létrehozása vagy módosítása | `MEMBER` |

## Ellenőrzések

```bash
cd frontend
node --experimental-strip-types checks/api.contract-check.ts
node --experimental-strip-types checks/app-state.check.ts
node --experimental-strip-types checks/create-flow.check.ts
npm run build

cd ../backend
./mvnw test
```

A backend teljes tesztjeihez futó PostgreSQL szükséges. Más port esetén állítsd be például:

```bash
GIGFLOW_DB_URL=jdbc:postgresql://127.0.0.1:55432/gigflow ./mvnw test
```
