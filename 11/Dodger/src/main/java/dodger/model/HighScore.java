package dodger.model;

import java.time.LocalDateTime;

/**
 * Egy eredmény a ranglistán: a MODELL. Ezzel dolgozik a backend
 * (Leaderboard) és a tároló (HighScoreRepository): a tábla egy sora
 * Java-objektumként. A tábla oszlopai és a mezők egy az egyben
 * megfelelnek egymásnak:
 *
 *   name VARCHAR(50)     ->  String name
 *   score INTEGER        ->  int score
 *   created_at TIMESTAMP ->  LocalDateTime createdAt
 *
 * A felület NEM ezt kapja, hanem a belőle készített HighScoreDto-t
 * (ld. Leaderboard.top): a modell a backend belső ügye, és szabadon
 * változhat (új oszlop, más típus) anélkül, hogy a felülethez nyúlni
 * kellene.
 *
 * Rekord, mert az eredmény nem változik, miután létrejött (ld. a 8. heti
 * Position).
 *
 * @param name a játékos neve
 * @param score az elért pontszám
 * @param createdAt mikor született az eredmény
 */
public record HighScore(String name, int score, LocalDateTime createdAt) {

}
