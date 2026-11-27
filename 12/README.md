# 12. hét – Sprite-játék képekkel és pályafájlokkal; a 3. beadandó mintamegoldása

Az utolsó anyag a **3. beadandóhoz** ad fogódzót. Az `Arkanoid` egy klasszikus, kicsi példa: a sprite-ok **képek**, a billentyűzet **key binding**, a pályák **szöveges fájlok**; a kód szándékosan egyszerű (a játék állapota és a rajzolás egy osztályban), hogy a három technika jól látsszon. A `Diver` pedig egy **teljes minta beadandó**: egy saját, a feladatlap 1. feladatához hasonló szerkezetű játék (rácsos pálya fájlokból, WASD, járőröző ellenfelek látótérrel, életek, időzítő, ranglista adatbázisban), modell–nézet szerkezetben, tesztekkel, UML-lel, dokumentációval.

## Áttekintés

| Mappa      | Típus          | Téma                                                                                                                                 |
| ---------- | -------------- | ------------------------------------------------------------------------------------------------------------------------------------ |
| `Arkanoid` | Példa          | Téglatörő: **képekből** rajzolt sprite-ok (`drawImage`), ütő a billentyűzetről (**key binding**), téglák **pályafájlból**; a játékhurok egy `Timer` |
| `Diver`    | Minta beadandó | **Gyöngyhalász**: a 3. beadandó szerkezetének teljes mintamegoldása, 53 JUnit teszttel, UML-lel és dokumentációval: [`Diver/README.md`](Diver/README.md) |

> **A beadandó dokumentációját DOCX vagy PDF formátumban kell leadni.** A minta leírása csak azért Markdown (`README.md`), mert a repó a GitHubon így olvasható. A tartalma a lényeg: ugyanezeket a részeket kell a saját dokumentációtokba is beletenni.

> **A feladatlap feladatait (Maci Laci, Kígyó, Labirintus, Tron) a minta szándékosan nem oldja meg.** A `Diver` egy *hasonló* játék: ugyanazok a technikák és ugyanaz a szerkezet kell hozzá, de a szabályok mások. A mintából a felépítést, a tesztelés módját és a dokumentáció szerkezetét vegyétek át, a saját feladatotok szabályait nektek kell megírni.

---

## Arkanoid

**Projekt:** `Arkanoid`. Az ütőt a **balra / jobbra nyíl** vagy az **A / D** indítja el, a **lefelé nyíl** vagy az **S** megállítja, az **Escape** szünetel. A labda a téglákról, a falakról és az ütőről pattan; ha alul kimegy, a játék elölről kezdődik. Ha elfogytak a téglák, jön a következő pálya.

```
┌ Arkanoid ───────────────────────────────────────────────────┐
│ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓ │  ← téglák: data/levels/level00.txt,
│ ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒ │     egy karakter = egy tégla (0-9: a kép)
│ ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ │
│                                                             │
│                           ●                                 │  ← a labda: data/images/ball.png
│                                                             │
│                       ▬▬▬▬▬▬▬▬▬                             │  ← az ütő: data/images/paddle.png
└─────────────────────────────────────────────────────────────┘
```

| Osztály       | Feladat                                                                                                          |
| ------------- | ---------------------------------------------------------------------------------------------------------------- |
| `Arkanoid`    | belépési pont                                                                                                    |
| `ArkanoidGUI` | az ablak; egyetlen komponens van benne, a `GameEngine`                                                           |
| `Sprite`      | hely, méret, **kép**; `draw(Graphics)` a `drawImage`-dzsel; `collides` a téglalapok metszésével                   |
| `Ball`, `Paddle`, `Brick` | a labda (sebesség, pattanás a széleken), az ütő (sebesség, a széleken megáll), a tégla (nem mozog)   |
| `Level`       | a téglák **fájlból**: `data/levels/levelNN.txt`; ütközés a labdával (a tégla eltűnik); `isOver` (nincs több tégla) |
| `GameEngine`  | a játéktér (`JPanel`): a képek betöltése, a **key bindingek**, a `Timer` (240 képkocka / mp), a játékhurok (`newFrame`), a `paintComponent` |

### Három technika, amit innen érdemes átvenni

**1. Sprite-ok képekből.** A kép egyszer töltődik be (`new ImageIcon("data/images/ball.png").getImage()`), és a `g.drawImage(image, x, y, width, height, null)` rajzolja ki a sprite helyére, a sprite méretére nyújtva. A képek a `data/images` mappában vannak (a `Breakout Tile Set Free` csomagból, CC0 licenccel). Saját képhez bármilyen PNG jó; a méretet a kód mondja meg, nem a fájl.

**2. Key binding.** Az `InputMap` egy billentyűhöz egy nevet rendel, az `ActionMap` a névhez egy akciót (`bind("LEFT", "left", () -> paddle.setVelX(-PADDLE_MOVEMENT))`). A `WHEN_IN_FOCUSED_WINDOW` miatt a panelnek nem kell fókuszban lennie. A lenyomás *sebességet állít*, nem lépést: az ütő addig megy, amíg meg nem állítjuk. (A Diverben a lenyomás egy mezőt léptet; a Dodgerben a lenyomás és a felengedés külön van bekötve. A három a három szokásos megoldás.)

**3. Pályafájlok.** Egy szöveges rács, soronként egy téglasor; egy számjegy a tégla fajtája, szóköz az üres hely. A `Level` konstruktora soronként olvassa, és a karakter helyéből számolja a tégla koordinátáit (`col * BRICK_WIDTH`, `row * BRICK_HEIGHT`). A fájlokat a program a **munkakönyvtárhoz** képest keresi (`data/levels/...`), ezért a projekt mappájából kell indítani; a Diver ehelyett az `src/main/resources` mappából, a jar belsejéből tölti a pályákat, ami bárhonnan működik: **a beadandóhoz azt ajánljuk.**

### Mi az, amit nem érdemes átvenni?

A `GameEngine` egyszerre modell (a labda, az ütő, a téglák, a szabályok), nézet (a `paintComponent`) és eseménykezelő; a `Sprite` a képet is tartja és maga rajzol. Egy ekkora játékhoz ez elfér egy osztályban, de **teszt nem írható rá**: a "ha alul kimegy a labda, elölről kezdjük" szabályt csak játszva lehet kipróbálni, és a pálya méretét (800 × 600) több osztály is "tudja". A beadandóhoz a `Diver` szerkezetét kövessétek: a modell Swing nélkül, a kép és a rajzolás a nézetben, a szabályok tesztekkel.

---

## Diver – a minta beadandó

**Projekt:** `Diver`, leírás: [`Diver/README.md`](Diver/README.md)

Egy búvár gyöngyöket gyűjt egy sziklás barlangban, ahol cápák járőröznek vízszintesen vagy függőlegesen. Ha a búvár egy cápa látóterébe kerül (a cápa előtti mező), elveszít egy életet, és visszakerül a hajóhoz. Ha a pálya minden gyöngyét összegyűjtötte, jön a következő pálya (10 van, fájlokban); ha elfogy a 3 élet, a program bekéri a nevét, elmenti az eredményét az adatbázisba, és megmutatja a 10 legjobbat. Az eltelt időt az állapotsor mutatja.

Azt mutatja meg, hogyan néz ki egy beadásra kész játék:

- **backend** Swing és SQL nélkül (`Game`, `Level`, `Shark`; a ranglistához `Leaderboard`, a `HighScore` modell és a nézetnek szánt `HighScoreDto`), **nézet** (`DiverGUI`, `GamePanel`), ami csak a backendet hívja, és **tárolás** (`JdbcHighScoreRepository`: az egyetlen osztály, amiben SQL van; SQLite alapból, MySQL egy kommentcserével), három csomagban;
- **10 pályafájl** az `src/main/resources/levels` mappában, szigorú beolvasóval, és egy **teszt, ami mind a 10-et betölti és ellenőrzi, hogy végigjátszható** (szélességi bejárás a kezdőhelyről);
- **53 JUnit teszt**: a pálya beolvasása és ellenőrzése (12), a játék szabályai kis, a tesztben szövegként megadott pályákon (21), a ranglista backendje memóriabeli tárolóval (11), a JDBC-s tároló ideiglenes SQLite-fájlon (9);
- **dokumentáció** a feladatlap elvárásai szerint: a feladat leírása és elemzése, a program szerkezete UML osztálydiagrammal, implementációs fejezet (a pálya betöltése és a bejárhatóság ellenőrzése, a látótér, az ütemezés), esemény–eseménykezelő párosítások, szekvencia- és állapotdiagram, a tesztek, a futtatás.

---

## A 3. beadandó – ellenőrzőlista

A leadás előtt érdemes végigmenni rajta (a mintában hol van megoldva):

| Követelmény | A mintában |
| --- | --- |
| Elemi grafika; a játékos sprite-ja WASD-dal | `GamePanel.paintComponent`; key binding W/A/S/D → `Game.move(Direction)` |
| Ha nem algoritmus generálja a pályát: legalább 10 pálya fájlokban, mind végigjátszható | `src/main/resources/levels/level01..10.txt`; `Level.isPlayable`; `LevelTest.allBuiltInLevelsLoadAndArePlayable` |
| Időzítő a játék kezdetétől | `Game.getElapsedMillis` (ütemekből); az állapotsor |
| A játék végén felugró ablak, név, mentés adatbázisba | `DiverGUI.gameOver`, `askName`, `Leaderboard.save` |
| Menüpont a 10 legjobbnak | *Game ▸ High scores*: `Leaderboard.top(10)`, `HighScoresDialog` |
| Új játék egy másik menüből | *Game ▸ New game* |
| Objektumorientált; (ajánlott) többrétegű | `model` / `db` / `view` csomagok |
| Felhasználóbarát | állapotsor, gyorsbillentyűk, a cápák látótere halványan kirajzolva, az adatbázis hibája üzenet |
| Dokumentáció: leírás, elemzés, szerkezet UML osztálydiagrammal, implementációs fejezet, esemény–eseménykezelő párosítások; DOCX vagy PDF | `Diver/README.md` 1–5. szakasz (a mintában Markdown) |
| Tesztek (a bemutatón kérdezzük) | `LevelTest`, `GameTest`, `LeaderboardTest`, `JdbcHighScoreRepositoryTest` |

A bemutatón a kérdések a **saját kódotokról** szólnak: miért ott van egy szabály, ahol van; mit tesztel egy teszt; mi történik, ha nincs adatbázis; hogyan kerül egy pálya a fájlból a képernyőre. Ha a minta szerkezetét követitek, ezekre lesz válasz.

## Futtatás

A projektekhez **JDK 17 vagy újabb** kell; az első fordításhoz internet (JUnit, a `Diver`-hez a két JDBC-driver).

- **NetBeans:** *File ▸ Open Project* → a projekt mappája → **F6**. Tesztek: **Alt+F6**.
- **IntelliJ IDEA:** *File ▸ Open* → a projekt mappája → *Open as Project* → *Trust Project*; a `.run/` konfiguráció megjelenik, **Shift+F10**. Tesztek: `src/test/java` ▸ *Run 'All Tests'*.
- **Parancssorból** (a projekt mappájából):

  ```
  mvn compile exec:java        # futtatás
  mvn test                     # tesztek (Diver: 53)
  mvn package                  # target/<projekt>-1.0-SNAPSHOT.jar, a Divernél a kövér jar: -all.jar is
  java -jar target/Diver-1.0-SNAPSHOT-all.jar
  ```

  Az `Arkanoid`-ot a **projekt mappájából** kell indítani (a `data/` mappa miatt); a `Diver` jar-ja bárhonnan fut, a pályák benne vannak.

- **JDK 24+ figyelmeztetés** (`--enable-native-access`, a SQLite-driver natív könyvtára miatt): a `Diver` futtatási konfigurációi, tesztjei és kövér jar-ja már a kapcsolóval indulnak, ld. a 10. heti README-t.
- **MySQL-lel** (`Diver`): a konténer indítása a projekt mappájából `docker compose up -d` (vagy a 10. heti README `docker run` parancsa), és a `Database.java`-ban a kommentek cseréje.

## Tippek

- **Először a pálya és a modell.** Írjuk meg a pályafájl formátumát, a beolvasót és a bejárhatóság tesztjét; aztán a `Game`-et a szabályokkal és a tesztjeikkel; a felület a végén már csak rajzol.
- **A tesztben a pálya szöveg.** Egy 5 × 5-ös pálya a tesztben, `List.of("#####", "#D.P#", ...)` alakban: azonnal látszik, mit vizsgálunk, és nem kell fájl hozzá.
- **A rajzolás a modellt olvassa, mezőnként.** `getTile(position)`, `getDiver()`, `getSharks()`: a nézet ebből rajzol, és nem tárol semmit a játékról.
- **Egy lépés = egy `move`, egy ütem = egy `tick`.** A billentyű a `move`-ot hívja, a `Timer` a `tick`-et; a kettő között a modell az összekötő.
- **Az ellenfelek szabálya a modellben, tesztekkel:** merre megy, mikor fordul, mit lát. A nézet csak kirajzolja (és akár a látóteret is: a játékos így érti, miért kapták el).
- **Pályaváltáskor `frame.pack()`**, ha a pályák mérete különbözik.
- **A dokumentációban a diagram egyezzen a kóddal.** Bármilyen eszközzel készülhet (draw.io, StarUML, PlantUML, Mermaid, kézzel rajzolva és beszkennelve), képként kerül a DOCX/PDF-be.
