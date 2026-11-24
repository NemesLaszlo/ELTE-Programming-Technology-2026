package colorclicker;

import java.time.LocalDateTime;

/**
 * Egy eredmény a ranglistán: a MODELL, a "results" tábla egy sora. Ezzel
 * dolgozik a backend (Leaderboard) és a tároló (HighScoreRepository); a
 * nézet a belőle készített HighScoreDto-t kapja.
 *
 * A Color clickerben az eredmény az idő (ezredmásodpercben), amennyi alatt
 * a játékos kiszínezte a táblát, ezért a KISEBB a jobb; és csak az azonos
 * méretű táblák eredményei hasonlíthatók össze, ezért a méret is a sor
 * része.
 *
 * @param name a játékos neve
 * @param size a tábla mérete (5, 10, 15 vagy 20)
 * @param millis az idő ezredmásodpercben
 * @param createdAt mikor született az eredmény
 */
public record HighScore(String name, int size, long millis, LocalDateTime createdAt) {

}
