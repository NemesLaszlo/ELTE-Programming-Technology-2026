package dodger.model;

import java.time.LocalDateTime;

/**
 * Egy sor a ranglistán, ahogy a FELÜLET kapja: DTO (Data Transfer Object,
 * adatátviteli objektum). A backend (Leaderboard) a modellből (HighScore)
 * állítja össze, és pontosan azt tartalmazza, amire a megjelenítéshez
 * szükség van: a helyezést is, amit a modell nem tárol (a tábla nem tudja,
 * hányadik egy sor, az a lekérdezés eredménye).
 *
 * Miért nem a modellt adjuk a felületnek? Mert a modell a backend belső
 * ügye: ha változik (új mező, más tárolás), a felületnek nem kell tudnia
 * róla; a DTO a "szerződés" a két réteg között. Egy nagyobb rendszerben a
 * modellben olyan mezők is vannak, amiket a felület nem kaphat meg (belső
 * azonosítók, jelszó-hash, naplóadatok): a DTO csak azt viszi ki, ami
 * kell. A felület a DTO-t csak olvassa, nem küldi vissza: a mentéshez a
 * Leaderboard.save(name, score) egyszerű értékeket kap, és a backend
 * készíti belőlük a modellt.
 *
 * @param rank a helyezés, 1-től
 * @param name a játékos neve
 * @param score az elért pontszám
 * @param createdAt mikor született az eredmény
 */
public record HighScoreDto(int rank, String name, int score, LocalDateTime createdAt) {

}
