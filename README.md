# Mobil alkalmazásfejlesztés 2024 - Autó alkatrész bolt

A projekt brutálisan a követelmények teljesítésére van optimalizálva.

## Pici infók a pontozáshoz

- Hibák *tán* nincsenek.

- Van email/password és guest login

- Van két adatmodell (Cart, ShopItem)

- 4 activity: Main (login), register, shop és cart

- ahol van beviteli mező (login és register) ott meg van adva a megfelelő hint is

- constraint layout mellé van pl. az `activity_cart.xml`-ban linear layout

- van két animáció, egy a gyakvideós card beúszás, a másik akkor látszik, ha valamit kosárba raksz

- minden activity elérhető intentekkel

- van +lifecycle hook, a mainben az onPause pl.

- most a notification nemtom erőforrásnak számít-e de kell hozzá permission :D

- van notification rendelés leadásnál

CRUD műveletekből szerencsére meg van mindegyik
- Create: cartokat is hoz létre, meg ha nem lenne véletlen itemlist, azt is megcsinálja
- Read: kind of sok olvasás történik, pl. az user cartjának lekérdezése
- Update: a cart mindig updatelődik, ha belepakolsz / törölsz belőle
- Delete: A cart ugye mentve van firebaseben userenként, a vendég felhasználók is rendelhetnek, **de "kijelentkezés" után törlődik a kosaruk az adatbázisból**

- 2 komplex firestore lekérdezés: egy talán komplex?

Köszönöm, hogy végigolvastad és kitartást!