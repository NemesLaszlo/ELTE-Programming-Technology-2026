# 4. hét – Fájlkezelés és kivételkezelés

Ezen a héten a fájlkezeléssel foglalkozunk. Az első beadandóban fájlból kell olvasni, és a kivételkezelést is használni kell, hogy a közben felmerülő problémákat (hiányzó fájl, hibás formátum, rossz típusú adat) kezelni tudjuk.

## Áttekintés

| Mappa        | Típus           | Téma                                                        |
| ------------ | --------------- | ----------------------------------------------------------- |
| `Vehicles`   | Példa           | Gépjárművek tankolásai – beolvasás, statisztikák            |
| `AnimalFarm` | Feladat         | Állatfarm – beolvasás, sovány állatok, nagy fogyasztók      |
| `Race`       | Minta beadandó  | Lények versenye – teljes megoldás tesztekkel, dokumentációval |

Mindegyik feladatban ugyanaz a három építőkő jelenik meg:

- **Fájl beolvasása** soronként, a bemenet szerkezetének megfelelően
- **Kivétel dobása**, ha a fájl nem nyitható meg vagy az adatok hibásak
- **Absztrakt ősosztály**, amiből a konkrét típusok származnak, de közvetlenül nem példányosítható

## Példa – Gépjárművek tankolásai

**Projekt:** `Vehicles`

Egy szövegfájlban gépjárművek tankolásait tartjuk nyilván. Minden gépjárművet a **rendszáma** azonosít. A gépjármű típusa lehet személygépkocsi, teherautó vagy busz.

### A fájl szerkezete

- Az első sor a gépjárművek száma: `n`
- A következő `n` sor mindegyike egy gépjárművet ír le, ebben a sorrendben:
  1. típus: `S` – személygépkocsi, `B` – busz, `T` – teherautó
  2. rendszám
  3. tankolások száma
  4. a tankolt mennyiségek (annyi szám, ahány tankolás)

Példafájl:

```
3
S AAA-111 4 6 7 8 9
B ABC-123 2 3 13
T FFF-888 7 1 2 3 4 5 6 7
```

Például a `B ABC-123 2 3 13` sor: egy busz, rendszáma `ABC-123`, kétszer tankolt, 3 és 13 egységnyit.

### Követelmények

- **Kivételkezelés:** dobjunk kivételt, ha hiba van a fájl megnyitásánál vagy az adatok beolvasásánál.
- **Gépjárművenként:** adjuk meg a tankolások átlagos mennyiségét.
- **Kategóriánként (S / B / T):** melyik gépjárműbe tankoltak
  - a legtöbbet,
  - a legkevesebbet,
  - a legtöbbször,
  - a legkevesebbszer.
- **Öröklődés:** az egyes gépjárműtípusokat egy közös gépjármű ősosztályból származtassuk, amiből közvetlenül **ne** lehessen példányt létrehozni.

> Oldjuk meg a feladatot úgy is, hogy **nem használjuk fel** a fájl első sorában lévő `n`-t: addig olvasunk, amíg van következő sor, és minden sort önállóan dolgozunk fel.

## Feladat – Állatfarm

**Projekt:** `AnimalFarm`

Egy szövegfájlban egy állatfarm állatait tartjuk nyilván. Minden állatot a **neve** azonosít. Egy állat lehet tehén, ló, kecske vagy emu.

### A fájl szerkezete

- Az első sor az állatok száma: `n`
- A következő `n` sor mindegyike egy állatot ír le, ebben a sorrendben:
  1. fajta: `T` – tehén, `L` – ló, `K` – kecske, `E` – emu
  2. név
  3. súly (kg)
  4. aznapi étkezések száma
  5. az egyes étkezéseken elfogyasztott élelem **dekagrammban** (annyi szám, ahány étkezés)

Példafájl:

```
3
E Borisz 32 4 20 30 30 20
K Kelemen 21 3 30 40 50
T Nárcisz 134 8 20 40 20 30 10 20 40 50
```

Például az `E Borisz 32 4 20 30 30 20` sor: egy emu, neve Borisz, 32 kg, aznap négyszer evett, összesen 100 dkg-ot.

### Követelmények

- **Kivételkezelés:** dobjunk kivételt, ha hiba van a fájl megnyitásánál vagy az adatok beolvasásánál.
- **Kórosan sovány állatok:** egy állat kórosan sovány, ha a súlya kevesebb, mint a fajtájához tartozó `k` küszöb:

  | Fajta  | `k` (kg) |
  | ------ | -------- |
  | tehén  | 100      |
  | ló     | 60       |
  | kecske | 12       |
  | emu    | 20       |

- **Nagy fogyasztók:** adjuk meg azokat az állatokat, amelyek aznap **több, mint 1 kg** (azaz több, mint 100 dkg) élelmet fogyasztottak.
- **Öröklődés:** az egyes állatfajtákat egy közös `Állat` ősosztályból származtassuk, amiből közvetlenül **ne** lehessen példányt létrehozni.

> Itt is oldjuk meg a feladatot az `n` felhasználása nélkül is.

## A megoldások működése

A `Vehicles` és az `AnimalFarm` projekt ugyanarra a mintára épül, a kódban magyar nyelvű megjegyzések magyarázzák a lépéseket.

### Futtatás

A projektekhez **JDK 17 vagy újabb** szükséges.

- **NetBeans:** nyissuk meg a projektet, majd **F6** (Run Project). A program a projekt mappájában lévő `data.txt` fájlt olvassa be.
- **Más fájl beolvasása:** a fájl neve parancssori argumentumként adható meg (NetBeansben: *Project Properties → Run → Arguments*), például:

  ```
  java -cp build/classes vehicles.Main sajat.txt
  java -cp build/classes animalfarm.Main sajat.txt
  ```

### Hibakezelés

A beolvasás soronként történik, és minden sort önállóan dolgozunk fel: a sor mezőit egy `Scanner` adja vissza egymás után. Az első sorban lévő `n` ezért **elhagyható**; ha szerepel, csak ellenőrzésre használjuk.

A program `InvalidInputException` kivételt dob – a hibás sor számával együtt –, ha

- ismeretlen a típus / fajta betűjele,
- hiányzik egy mező, vagy nem szám áll ott, ahol számnak kellene,
- nem annyi mennyiség szerepel a sorban, ahány tankolást / étkezést a sor megad,
- egy mennyiség vagy a súly nem pozitív,
- ugyanaz a rendszám / név kétszer szerepel,
- az első sorban megadott `n` nem egyezik a beolvasott sorok számával.

Ha a fájl nem található vagy nem olvasható, `IOException` keletkezik. A `Main` mindkét kivételt elkapja, hibaüzenetet ír ki, és `1`-es kilépési kóddal áll le. Hibás fájl esetén a nyilvántartásba semmi nem kerül be.

## Minta beadandó – Lények versenye

**Projekt:** `Race`

Az első beadandó feladatsor 1. feladatának teljes megoldása: többnapos verseny, amelyen homokjárók, szivacsok és lépegetők vesznek részt, és az nyer, aki életben maradva a legmesszebbre jut.

Ez a projekt azt mutatja meg, hogyan néz ki egy beadásra kész megoldás:

- a bemenet minden hibáját ellenőrzi, és érthető hibaüzenetet ad,
- minden osztály és metódus Javadoc megjegyzéssel van ellátva (angolul),
- egységtesztek (JUnit 4) tartoznak hozzá – NetBeansben **Alt+F6**,
- saját dokumentációja van a feladat leírásával, az osztálydiagrammal, a használattal és a teszteléssel: [`Race/README.md`](Race/README.md).

## Tippek

- Az ősosztály legyen `abstract`; a fajtánként eltérő érték (pl. a soványsági küszöb) jó jelölt egy absztrakt metódusra, amit minden leszármazott maga ad meg.
- A típusbetű alapján az ősosztály egy statikus "gyártó" metódusa (vagy egy külön beolvasó osztály) hozhatja létre a megfelelő leszármazottat.
- Az egyes sorokat `Scanner`-rel vagy szóköz mentén darabolva dolgozzuk fel; ha nem szám jön ott, ahol számnak kéne lennie, vagy hiányzik egy mező, dobjunk kivételt.
- Használjuk a `try-with-resources` szerkezetet a fájl megnyitásához, így a fájl hiba esetén is bezáródik.
- A példában szereplő fajlok a projektek mellett találhatók, azokkal érdemes tesztelni – de egy szándékosan elrontott fájllal is próbáljuk ki a kivételkezelést.
