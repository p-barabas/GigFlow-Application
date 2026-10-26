# Példa AI-fejlesztési napló

## Cél

Az első heti demonstrációhoz egy gyorsan kipróbálható GigFlow prototípus készüljön React, TypeScript és Vite használatával.

## Kiinduló prompt

> Készíts egy reszponzív, magyar nyelvű React + TypeScript prototípust zenekari fellépések szervezéséhez. Lehessen fellépést létrehozni, három mintatag között váltani, valamint eseményenként Részt veszek, Bizonytalan vagy Nem érek rá választ adni. Az adat maradjon meg oldalfrissítés után localStorage használatával. Ne készíts backendet vagy valódi bejelentkezést.

## Pontosító promptok

> A csak szóközökből álló eseménycímet utasítsd el érthető magyar hibaüzenettel.

> Ugyanazon tag új válasza írja felül az előzőt; ne keletkezzen két válasz ugyanahhoz az eseményhez.

> Tedd jól láthatóvá, hogy ez első heti böngészős prototípus, és a tagválasztó nem hitelesítés.

## Emberi ellenőrzés

- az adattípusok és a tárolt JSON alakjának átnézése;
- a három válaszállapot jelentésének ellenőrzése;
- normál és hibás űrlapbevitel kipróbálása;
- frissítés utáni adatmegmaradás ellenőrzése;
- keskeny nézet vizuális és billentyűzetes ellenőrzése;
- a „tagváltás” biztonsági korlátjának egyértelmű jelzése.

## Továbbfejlesztési adósság

- a komponens túl sok felelősséget kezel;
- a tárolási adapter közvetlenül böngésző API-t használ;
- nincs alkalmazási réteg vagy use case;
- nincs szerveroldali adatforrás és felhasználóazonosítás;
- a domain szabályok még nincsenek külön tesztelhető modellben.

Ezeket a későbbi hetekben tudatos refaktorálással, majd Clean Architecture rétegekkel kell kezelni.
