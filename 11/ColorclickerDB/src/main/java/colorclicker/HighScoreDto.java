package colorclicker;

import java.time.LocalDateTime;

/**
 * Egy sor a ranglistán, ahogy a NÉZET kapja: DTO (adatátviteli objektum).
 * A backend (Leaderboard) a modellből (HighScore) állítja össze, a
 * helyezéssel kiegészítve; a nézet csak megjeleníti. A modell a backend
 * belső ügye, a DTO a "szerződés" a nézet felé (ld. a 10. heti
 * HighScoreDto).
 *
 * @param rank a helyezés, 1-től
 * @param name a játékos neve
 * @param size a tábla mérete
 * @param millis az idő ezredmásodpercben
 * @param createdAt mikor született az eredmény
 */
public record HighScoreDto(int rank, String name, int size, long millis, LocalDateTime createdAt) {

}
