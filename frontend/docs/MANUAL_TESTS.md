# GigFlow — kézi elfogadási tesztek

Az alábbi próbákhoz a backend `local` profillal és a README-ben megadott demó felhasználókkal fusson.

## 1. Hibás és sikeres bejelentkezés

1. Adj meg hibás jelszót Anna felhasználóhoz.
2. Ellenőrizd az érthető hibaüzenetet.
3. Jelentkezz be az `anna` fiókkal.

Elvárt eredmény: hibás jelszóval nem jelennek meg adatok; siker után Anna neve, a fellépéslista és a kijelentkezés látszik.

## 2. Vezetői eseménylétrehozás

1. Jelentkezz be `leader` felhasználóként.
2. Hozz létre egy jövőbeli „Téli kamarazene” eseményt a „Bartók Terem” helyszínnel.
3. Próbálj csak szóközökből álló címet is menteni.

Elvárt eredmény: a helyes esemény egyszer megjelenik három meghívottal; a hibás cím nem jut el az API-hoz.

## 3. Szerepkörök szétválasztása

1. Vezetőként ellenőrizd, hogy létrehozhatsz eseményt, de nincs válaszgomb.
2. Jelentkezz ki, majd lépj be Annaként.
3. Ellenőrizd, hogy a létrehozó űrlap helyett tagsági tájékoztató látszik.

Elvárt eredmény: a felület csak a szerepkörhöz tartozó műveleteket kínálja; az API külön 403-mal is védi a tiltott műveleteket.

## 4. Saját válasz létrehozása és módosítása

1. Annaként egy meghívásnál válaszd a **Részt veszek** lehetőséget.
2. Ugyanott válaszd a **Nem érek rá** lehetőséget.

Elvárt eredmény: Anna egyetlen válasza módosul, az összesítő nem számolja kétszer, és más tag válasza változatlan.

## 5. Tartósság és elkülönített felhasználók

1. Frissítsd az oldalt, és jelentkezz be újra Annaként.
2. Ellenőrizd a korábbi válaszát.
3. Jelentkezz át Bence vagy Csilla fiókjába, és adj eltérő választ.

Elvárt eredmény: az adat PostgreSQL-ből visszatöltődik, és minden tag kizárólag a saját válaszát módosítja.

## 6. Üres, hiba- és reszponzív állapot

1. Üres adatbázissal ellenőrizd a „Még nincs fellépés” állapotot.
2. Állítsd le a backendet, majd próbálj adatot tölteni.
3. Keskenyítsd az ablakot telefonméretre, és billentyűzettel járd be a bejelentkezést, űrlapot és válaszgombokat.

Elvárt eredmény: az üres és hibaállapot különbözik, minden mezőnek van címkéje, a fókusz látható, és nincs szükség vízszintes görgetésre.
