# 3. hét – Java gyűjtemények

Ezen a héten a Java gyűjteményeket nézzük át. Az `ArrayList`, a `HashMap` és a `HashSet` talán a leggyakrabban használt és leghasznosabb eszközök a programozásban.

## Áttekintés

| Mappa                | Típus   | Téma                                        |
| -------------------- | ------- | ------------------------------------------- |
| `1_BagArrayListIntro` | Példa   | Zsák `ArrayList`-tel                        |
| `2_BagHashMapIntro`   | Példa   | Zsák `HashMap`-pel                          |
| `3_NumBoth`           | Példa   | Közös elemek száma ciklus nélkül            |
| `4_Sort1`             | Példa   | Rendezés osztási maradék szerint            |
| `5_BagArrayList`      | Feladat | Metszet és különbség (`ArrayList`-es zsák)  |
| `6_BagHashMap`        | Feladat | Metszet és különbség (`HashMap`-es zsák)    |
| `7_Sort2`             | Feladat | Részlista rendezése `Comparator`-ral        |

## Generikusok röviden

A gyűjtemények mind **generikusak**: a `< >` közé írt **típusparaméterrel** mondjuk meg, hogy milyen elemekkel használjuk őket. Az alábbi jelölések a heti kódokban mind előfordulnak, és a JDK dokumentációja is tele van velük, ezért érdemes megtanulni olvasni őket.

### Típusparaméter megadása: `List<String>`

```java
List<String> names = new ArrayList<>();
names.add("Anna");             // rendben
names.add(42);                 // fordítási hiba: ebbe a listába csak String kerülhet
String first = names.get(0);   // nem kell cast, a fordító tudja, hogy String jön ki
```

A lényeg: a hibás használat már **fordításkor** kiderül, nem futás közben.

| Jelölés                      | Jelentés                                                                                                                                                  |
| ---------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `List<String>`               | `String`-eket tartalmazó lista                                                                                                                            |
| `Map<String, Integer>`       | két típusparaméter, sorrendben a kulcs és az érték típusa                                                                                                 |
| `Map.Entry<String, Integer>` | a map egy kulcs–érték párja; az `Entry` a `Map` interfészen belül van deklarálva, ezért a pont a nevében                                                  |
| `new ArrayList<>()`          | „gyémánt”: az üres `< >` helyére a fordító a bal oldal alapján kitalálja a típust                                                                          |
| `List<Integer>`              | típusparaméter nem lehet primitív, ezért nincs `List<int>`; helyette a csomagolóosztály kell (`Integer`, `Double`, …), az átalakítást a fordító elvégzi   |
| `List` (`< >` nélkül)        | „nyers típus”, a generikusok előtti időkből maradt meg; kerülendő, mert ilyenkor a fordító semmit nem ellenőriz                                            |

### Saját generikus metódus: `<T>`

A `3_NumBoth` projektből:

```java
public static <T> int numBoth(Collection<T> c1, Collection<T> c2)
```

| Rész                   | Jelentés                                                                                                                                                                     |
| ---------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `<T>`                  | bevezet egy **típusváltozót**: „legyen `T` egy tetszőleges típus”. E nélkül a fordító egy `T` nevű osztályt keresne. A név szabadon választható, egy nagybetű a szokás: `T` (type), `E` (element), `K` (key), `V` (value) |
| `int`                  | a visszatérési típus – a `<T>` mindig közvetlenül ez elé kerül                                                                                                               |
| `Collection<T> c1, …`  | mindkét gyűjtemény ugyanolyan típusú (`T`) elemeket tartalmaz                                                                                                                |

Híváskor a `T`-t nem kell kiírni: a `numBoth(c1, c2)` hívásnál a fordító látja, hogy `c1` egy `List<Integer>`, tehát `T = Integer`.

**Miért kell a `<T>`, és miért pont ott?** A `<T>` nem az `int`-hez tartozik: ez a `T` *deklarációja*. Ahogy egy változót, úgy a típusváltozót is deklarálni kell, mielőtt használjuk – a `Collection<T>` már csak *használja*. Ha kihagyjuk, a fordító a `T`-t egy létező osztály nevének hiszi, és nem találja:

```java
public static int numBoth(Collection<T> c1, Collection<T> c2)
// error: cannot find symbol
//   symbol: class T
```

A helye pedig azért a visszatérési típus előtt van, mert a visszatérési típus is lehet maga a `T`, és addigra már ismertnek kell lennie:

```java
public static <T> T first(List<T> list)
```

Generikus **osztálynál** a deklaráció az osztály neve után áll (`class ReverseComparator<T>`), és azt a `T`-t az osztály példánymetódusai mind látják – ezért nem kell a `compare(T, T)` elé újra kiírni. Statikus metódus viszont az osztály `T`-jét nem látja (az a példányhoz tartozik), a sajátját kell deklarálnia.

### Generikus interfész megvalósítása, saját generikus osztály

```java
public class RemainderComparator implements Comparator<Integer>   // 4_Sort1
public class ReverseComparator<T> implements Comparator<T>        // 7_Sort2
```

- Az első osztály **nem generikus**: rögzíti, hogy `Integer`-eket hasonlít össze, ezért a metódusa `compare(Integer, Integer)`.
- A második **maga is generikus**: az osztály neve utáni `<T>` vezeti be a típusváltozót, amelyet aztán továbbad a `Comparator`-nak. A `T` a példányosításkor dől el: `new ReverseComparator<String>(…)`, vagy gyémánttal `new ReverseComparator<>(…)`.

### Helyettesítő karakter: `Comparator<? super T>`

A `7_Sort2` projektből:

```java
public static <T> void sort(List<T> list, int i, int j, Comparator<? super T> comparator)
```

Az utolsó paraméter típusa darabonként:

| Rész              | Jelentés                                                                                   |
| ----------------- | ------------------------------------------------------------------------------------------ |
| `Comparator<...>` | egy összehasonlító; a `< >` között az áll, hogy milyen típusú elemeket tud összehasonlítani |
| `?`               | „valamilyen típus, amelyet nem nevezünk meg” (angolul *wildcard*)                           |
| `super T`         | megkötés a `?`-re: csak `T` maga, vagy `T` valamelyik **őse** (ősosztálya, interfésze) lehet |
| együtt            | olyan összehasonlító, amely `T`-ket **vagy annál általánosabb** dolgokat tud összehasonlítani |

Miért jó ez? Rendezzünk egy `List<Integer>`-t, ekkor `T = Integer`. Az `Integer` ősei a `Number` és az `Object`.

| Átadott összehasonlító | Elfogadja? | Miért                                                                                          |
| ---------------------- | ---------- | ---------------------------------------------------------------------------------------------- |
| `Comparator<Integer>`  | igen       | pontosan `T`                                                                                   |
| `Comparator<Number>`   | igen       | aki bármilyen két számot össze tud hasonlítani, az két `Integer`-t is                          |
| `Comparator<Object>`   | igen       | aki bármilyen két objektumot össze tud hasonlítani, az két `Integer`-t is (lásd `TextLengthComparator`) |
| `Comparator<String>`   | nem        | a `String` nem őse az `Integer`-nek, ez `Integer`-ekkel nem tud mit kezdeni                     |

Ha a paraméter típusa egyszerűen `Comparator<T>` lenne, a fordító **csak** a `Comparator<Integer>`-t fogadná el, pedig a másik kettő is hibátlanul működne. Ennek az az oka, hogy a típusparaméterek között az öröklődés nem „öröklődik tovább”: hiába őse a `Number` az `Integer`-nek, a `Comparator<Number>` és a `Comparator<Integer>` két egymástól független típus. Ez elsőre meglepő, de szükséges:

```java
List<Integer> ints = new ArrayList<>();
List<Number> nums = ints;   // fordítási hiba - szerencsére, mert különben...
nums.add(3.14);             // ...egy tört szám kerülne az egészek listájába
```

A `? super T` ezt a merevséget oldja fel ott, ahol ez biztonságos.

A párja a `? extends T`: „`T` vagy `T` valamelyik **leszármazottja**”. Ilyen például az `ArrayList` másoló konstruktora, `ArrayList(Collection<? extends E> c)`: egy `ArrayList<Number>`-t egy `List<Integer>` elemeivel is fel lehet tölteni.

Ökölszabály, hogy melyik mikor kell:

- amiből `T`-ket **veszünk ki** (forrás) → `? extends T`,
- aminek `T`-ket **adunk át** (fogyasztó) → `? super T`. A `Comparator` ilyen: a `compare` két `T`-t *kap*.

Példa szignatúra (`Collections.sort`):

```java
static <T extends Comparable<? super T>> void sort(List<T> list)
```

Itt a `<T extends …>` a típusváltozó megkötése: `T` nem akármi lehet, hanem csak olyan típus, amely `Comparable`, vagyis össze tudja hasonlítani magát egy másik `T`-vel (vagy `T` egy ősével). Ezért nem kell ennek a változatnak `Comparator`-t átadni.

## Példák

### 1. Zsák `ArrayList`-tel

**Projekt:** `1_BagArrayListIntro`

Implementáljunk egy zsák típust `ArrayList`-tel! A zsák hasonló egy halmazhoz, viszont egy elem többször is szerepelhet benne, így külön nyilván fogjuk tartani, hogy melyik elemből hány darab van.

Műveletek:

- új elem betétele
- elem kivétele
- kiürítés
- lekérdezés: egy elemből hány darab van
- **unió**: két zsák unióját állítja elő

A halmazműveletek értelmezése zsákokra (elemenként, a darabszámokkal):

| Művelet   | Darabszám az eredményben | Megjegyzés                                    |
| --------- | ------------------------ | --------------------------------------------- |
| unió      | `a + b`                  | a két zsák „összeöntése”                      |
| metszet   | `min(a, b)`              | ami mindkettőben megvan                       |
| különbség | `a - b`, ha pozitív      | ami elfogyna, kimarad; nem szimmetrikus       |

### 2. Zsák `HashMap`-pel

**Projekt:** `2_BagHashMapIntro`

Adjunk egy hatékonyabb implementációt a zsákra `HashMap`-pel!

> Az `ArrayList`-es változatban egy elem megkeresése lineáris idejű, a `HashMap`-nél ez átlagosan konstans – érdemes a két megoldást összevetni.

### 3. Közös elemek száma

**Projekt:** `3_NumBoth`

Írjunk egy függvényt **ciklus használata nélkül**, amely két azonos típusú elemeket tartalmazó gyűjteményről megállapítja, hogy hány közös elemük van.

A multiplicitás nem számít: ha egy elemből mindkét gyűjteményben 2-2 darab van, az is csak egynek számít.

### 4. Rendezés osztási maradék szerint

**Projekt:** `4_Sort1`

Írjunk egy függvényt a következő szignatúrával:

```java
List<Integer> getSortedByNRemainder(List<Integer> list, final int n, final boolean ascending)
```

- visszaadja a `list` **másolatát** (az eredeti lista nem módosul),
- az `n` szerinti osztási maradék alapján rendezve (a maradék negatív számra is `0` és `n-1` közé esik, pl. `-1` maradéka `3`-mal osztva `2`),
- az `ascending` értékétől függően növekvően vagy csökkenően.

Legyen a függvénynek egy kétparaméteres változata is, amely csak a `list`-et és az `n`-t várja, és növekvően rendez.

## Feladatok

### 1. Metszet és különbség – `ArrayList`

**Projekt:** `5_BagArrayList`

Implementáljuk az `ArrayList`-tel megvalósított `Bag`-hez a **metszet** és a **különbség** műveleteket.

### 2. Metszet és különbség – `HashMap`

**Projekt:** `6_BagHashMap`

Implementáljuk a `HashMap`-pel megvalósított `Bag`-hez a **metszet** és a **különbség** műveleteket.

### 3. Részlista rendezése `Comparator`-ral

**Projekt:** `7_Sort2`

Írjunk egy függvényt, amely egy adott lista, annak `i` és `j` indexei, valamint egy összehasonlító objektum (`Comparator`) alapján **helyben módosítja** a listát:

- az `i` és `j` közötti részt a `Comparator` által meghatározott módon rendezi,
- a kezdőszeletet és a listavéget pedig **ellentétes** irányban.

Példa természetes rendezéssel, `i = 2`, `j = 4` (mindkét határ beleértve):

```text
előtte:  [5, 2 | 8, 1, 9 | 3, 7]
utána:   [5, 2 | 1, 8, 9 | 7, 3]
```