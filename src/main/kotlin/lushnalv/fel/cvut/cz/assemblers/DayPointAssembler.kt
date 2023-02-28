package lushnalv.fel.cvut.cz.assemblers

import com.fasterxml.jackson.databind.ObjectMapper
import lushnalv.fel.cvut.cz.models.*
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class DayPointAssembler(val objectMapper: ObjectMapper) {
    fun toDayPointDto(dayPoint: DayPoint): DayPointDto {
        return DayPointDto(
            id = dayPoint.id,
            title = dayPoint.title,
            date = dayPoint.date,
            duration = dayPoint.duration,
            latitude = dayPoint.latitude,
            longitude = dayPoint.longitude,
            typeOfDayPoint = dayPoint.typesOfDayPointsStable,
            dayId = dayPoint.day.id,
            defaultPhoto = dayPoint.defaultPhoto,
            photoListString = dayPoint.photoList,
            documentListString = dayPoint.documentList,
            travelTime = dayPoint.travelTime,
            travelType = dayPoint.travelType,
            travelDistance = dayPoint.travelDistance,
            openingMessage = openingMessage(dayPoint.date, dayPoint.openingHours),
            deleted = dayPoint.deleted
        )
    }

    private fun openingMessage(date: LocalDateTime, openingHoursString: String?): String {
        if (openingHoursString == null) {
            return ""
        }

        val openingHoursMap = objectMapper.readValue(openingHoursString, OpeningHours::class.java)

        val openingHours = openingHoursMap.byWeekDay[date.dayOfWeek]

        if (openingHours == null) {
            return "Closed at this day of week"
        }

        val localTime = date.toLocalTime()

        if (!openingHours.any { it.start <= localTime && it.end >= localTime }) {
            val nextOpen = openingHours.filter { it.start.isAfter(localTime) }.firstOrNull()
            if (nextOpen!=null) {
                return "Will open at ${nextOpen.start.format(DateTimeFormatter.ofPattern("HH:mm"))}"
            }

            val lastClosed = openingHours.lastOrNull()
            if (lastClosed!=null) {
                return "Will be closed since ${lastClosed.end.format(DateTimeFormatter.ofPattern("HH:mm"))}"
            }

            return "Closed at this day of week"
        } else {
            return ""
        }
    }

    fun toListDayPointDto(dayPointList: List<DayPoint>): List<DayPointDto> {
        return if (dayPointList.isNotEmpty()) {
            dayPointList.map { toDayPointDto(it) }
        } else {
            listOf()
        }
    }

    fun toDayPointWholeInfoDto(dayPoint: DayPoint, users: List<User>): DayPointWholeInfoDto {
        return DayPointWholeInfoDto(
            id = dayPoint.id,
            title = dayPoint.title,
            date = dayPoint.date,
            duration = dayPoint.duration,
            latitude = dayPoint.latitude,
            longitude = dayPoint.longitude,
            typeOfDayPoint = dayPoint.typesOfDayPointsStable,
            dayId = dayPoint.day.id,
            defaultPhoto = dayPoint.defaultPhoto,
            photoListString = dayPoint.photoList,
            documentListString = dayPoint.documentList,
            duties = dayPoint.duties.map { it -> DutyAssembler().toDutyDto(it) },
            travelTime = dayPoint.travelTime,
            travelType = dayPoint.travelType,
            travelDistance = dayPoint.travelDistance,
            users = users.map { UserAssembler().toUserDto(it) },
            comments = dayPoint.comments.map { CommentAssembler().toCommentDto(it) },
            deleted = dayPoint.deleted,
            openingMessage = openingMessage(dayPoint.date, dayPoint.openingHours)
        )
    }

//    fun toListDayPointWholeInfoDto(dayPointList: List<DayPoint>): List<DayPointWholeInfoDto> {
//        return if (dayPointList.isNotEmpty()) {
//            dayPointList.map { toDayPointWholeInfoDto(it) }
//        } else {
//            listOf()
//        }
//    }
}

data class DayPointDto(
    val id: Long,
    val title: String,
    val date: LocalDateTime,
    val duration: Duration,
    val latitude: Double,
    val longitude: Double,
    val typeOfDayPoint: TypeOfDayPoint,
    var dayId: Long,
    val defaultPhoto: String,
    var photoListString: List<String>,
    var documentListString: List<String>,
    val travelTime: Duration,
    val travelType: TypeOfDayPointActive,
    val travelDistance: Int,
    val openingMessage: String,
    val deleted: Boolean,
)

data class DayPointWholeInfoDto(
    val id: Long,
    val title: String,
    val date: LocalDateTime,
    val duration: Duration,
    val latitude: Double,
    val longitude: Double,
    val typeOfDayPoint: TypeOfDayPoint,
    var dayId: Long,
    val defaultPhoto: String,
    var photoListString: List<String>,
    var documentListString: List<String>,
    val duties: List<DutyDto>,
    val travelTime: Duration,
    val travelType: TypeOfDayPointActive,
    val travelDistance: Int,
    val openingMessage: String,
    val deleted: Boolean,
    val users: List<UserAssembler.UserDto>,
    val comments: List<CommentDto>,
)

