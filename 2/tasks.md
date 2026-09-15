# 2. hét

Ezen a héten az UML osztálydiagram relációit ismételjük át: az asszociációt, aggregációt, kompozíciót és az öröklődést.

## Példa

Egy állatkert „szimulációja", ahol különböző állatok élnek, és egy étterem is van.

- UML osztálydiagram: `zoo_animals.png`
- NetBeans projekt: `zoo`

## Feladat

Egy szerepjátékban a szereplő lehet a fő karakter, ork vagy sárkány. Az orkok lehetnek harcosok, berzerkerek, pajzsosok. A sárkány lehet vörös vagy fekete.

Minden szereplőnek van neve, életereje és támadóereje. Minden szereplő tud támadni valakit, ekkor a megtámadott életereje a szereplő támadóértékével csökken. A berzerkerek életereje a támadóérték kétszeresével, a pajzsosoké a felével csökken. A fekete sárkányt csak akkor lehet sebezni, ha a támadóerő nagyobb, mint 20, a vörös sárkányt pedig csak akkor, ha a támadóerő nagyobb, mint 60.

A fő karakternek van egy védelem értéke is, ami valós szám. A fő karakter életerejének csökkentésénél az ellenfél támadóerejét el kell osztani ezzel az értékkel. Egy karakter pontosan akkor van életben, ha az életereje 0 vagy afölött van.

Az osztályokat teszteljük is a main programban: addig harcoljanak, amíg csak egy szereplő marad, és írjuk is ki a győztest! Véletlen számokat a `Random` osztály `nextInt` metódusával lehet generálni.

## Megoldás

- UML osztálydiagram: `rpgame.png`
- NetBeans projekt: `rpgame`