# GigFlow frontend

A React/TypeScript kliens aktuális full-stack indítási leírása a projekt gyökerében található: [`../README.md`](../README.md).

## Indítás

Előfeltétel: Node.js 20.19 vagy újabb, valamint a `127.0.0.1:8080` címen futó Spring backend.

```bash
npm install
npm run dev
```

A terminálban megjelenő helyi címet kell megnyitni, alapértelmezetten: `http://localhost:5173`.
Fejlesztéskor a Vite kizárólag az `/api` prefixű kéréseket proxyzza a backendhez; a kliens relatív API URL-t használ, ezért külön CORS workaround nem szükséges.

Ellenőrzött kiadás készítése:

```bash
npm run build
npm run preview
```

A production build a Spring Boot `src/main/resources/static` könyvtárába kerül. Az `index.html` és az `assets/` buildkimenet; a Vite a könyvtár más fájljait nem törli.
