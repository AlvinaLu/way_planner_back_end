package lushnalv.fel.cvut.cz.assemblers

import lushnalv.fel.cvut.cz.models.Trip
import lushnalv.fel.cvut.cz.models.User
import lushnalv.fel.cvut.cz.services.DutyServiceImpl
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import lushnalv.fel.cvut.cz.services.WeatherContent

@Component
class TripAssembler(val dayAssembler: DayAssembler) {

    fun toTripDto(trip: Trip, users: List<User>?): TripDto {
        return TripDto(
            id = trip.id,
            title = trip.title,
            ownerId = trip.author.id,
            startDay = trip.startDay,
            endDay = trip.endDay,
            defaultPhoto = trip.defaultPhoto,
            days = listOf(),
            members = users?.map { it-> UserAssembler().toUserDto(it)}?: listOf(),
            deleted = trip.deleted
        )

    }

    fun toTripDtoWithDays(trip: Trip, users: List<User>?, weather:List<WeatherContent>): TripDto {
        return TripDto(
            id = trip.id,
            title = trip.title,
            ownerId = trip.author.id,
            startDay = trip.startDay,
            endDay = trip.endDay,
            defaultPhoto = trip.defaultPhoto,
            days = dayAssembler.toListDayDto(trip.days.sortedBy { it.date }, weather),
            members = users?.map { it-> UserAssembler().toUserDto(it)}?: listOf(),
            deleted = trip.deleted
        )

    }

    fun toTripDtoWithDuties(trip: Trip, users: List<User>, duties: List<DutyServiceImpl.Transaction>, weather:List<WeatherContent>): TripInfoDto {
        return TripInfoDto(
            id = trip.id,
            title = trip.title,
            ownerId = trip.author.id,
            startDay = trip.startDay,
            endDay = trip.endDay,
            defaultPhoto = trip.defaultPhoto,
            days = dayAssembler.toListDayDto(trip.days.sortedBy { it.date }, weather),
            members = users?.map { it-> UserAssembler().toUserDto(it)}?: listOf(),
            dutyCalculation = duties.map { DutyAssembler().toDutyCalculation(it) },
            deleted = trip.deleted,
        )

    }
    fun toListTripDto(tripList: List<Trip>): List<TripDto>{
        return if(tripList.isNotEmpty()){
            tripList.map { toTripDto(it, null) }
        }else{
            listOf()
        }
    }
}

data class TripDto(
    val id: Long,
    val title: String,
    val ownerId: Long,
    val startDay: LocalDateTime,
    val endDay: LocalDateTime,
    val defaultPhoto: String,
    val days: List<DayDto>,
    val members: List<UserAssembler.UserDto>,
    val deleted: Boolean,
)

data class TripInfoDto(
    val id: Long,
    val title: String,
    val ownerId: Long,
    val startDay: LocalDateTime,
    val endDay: LocalDateTime,
    val defaultPhoto: String,
    val days: List<DayDto>,
    val members: List<UserAssembler.UserDto>,
    val dutyCalculation: List<DutyCalculationDto>,
    val deleted: Boolean,
)



data class LatLng(val latitude:Double, val longitude: Double)