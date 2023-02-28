package lushnalv.fel.cvut.cz.assemblers

import lushnalv.fel.cvut.cz.models.Day
import lushnalv.fel.cvut.cz.services.WeatherContent
import lushnalv.fel.cvut.cz.utils.pairByIndex
import net.minidev.json.annotate.JsonIgnore
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class DayAssembler(val dayPointAssembler: DayPointAssembler){

    fun toDayDto(day: Day, weather: WeatherContent): DayDto{
        return DayDto(
            id = day.id,
            date = day.date,
            tripId = day.trip.id,
            dayPoints = dayPointAssembler.toListDayPointDto(day.dayPoints),
            codeWeather = weather.code,
            minTemperature = weather.minTemperature,
            maxTemperature = weather.maxTemperature
        )
    }

    fun toListDayDto(dayList: List<Day>, weather: List<WeatherContent>): List<DayDto>{
        return if(dayList.isNotEmpty()){
            dayList.pairByIndex(weather).map { (day, w)->toDayDto(day, w) }
        }else{
            listOf()
        }
    }
}

data class DayDto(
    val id: Long,
    val date: LocalDateTime,
    var tripId: Long,
    var dayPoints: List<DayPointDto>,
    val codeWeather: Int,
    val minTemperature: Double,
    val maxTemperature: Double,
)
