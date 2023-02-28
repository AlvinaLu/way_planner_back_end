package lushnalv.fel.cvut.cz.models

import java.time.DayOfWeek
import java.time.LocalTime

data class OpeningHours(
    val byWeekDay: Map<DayOfWeek, List<TimeInterval>>
) {
    data class TimeInterval(
        val start: LocalTime,
        val end: LocalTime
    )
}