package lushnalv.fel.cvut.cz.controllers

import com.fasterxml.jackson.annotation.JsonProperty
import lushnalv.fel.cvut.cz.assemblers.*
import lushnalv.fel.cvut.cz.exeptions.UserDoesNotExistException
import lushnalv.fel.cvut.cz.models.TypeOfDayPoint
import lushnalv.fel.cvut.cz.repositories.UserRepository
import lushnalv.fel.cvut.cz.services.DutyService
import lushnalv.fel.cvut.cz.services.TripService
import lushnalv.fel.cvut.cz.services.WeatherService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("trip")
class TripController(val tripAssembler: TripAssembler, val tripService: TripService, val userRepository: UserRepository, val dutyService: DutyService, val weatherService: WeatherService) {
    companion object {
        private val LOG = LoggerFactory.getLogger(TripController::class.java)
    }

    /**
     * Get all trips of current user
     * @param fromUser http request with user principal
     * @return short trip data list
     */
    @GetMapping("/all")
    fun getAllTrips(fromUser: HttpServletRequest): ResponseEntity<List<TripDto>> {
        val user = userRepository.findByEmail(fromUser.userPrincipal.name).orElse(null)
        val trips = tripService.getAllTrips(user)
        val response = tripAssembler.toListTripDto(trips)
        return ResponseEntity.ok(response)
    }

    /**
     * Create a new trip
     * @param request new trip request
     * @param fromUser http request with user principal
     * @return short trip data
     */
    @PostMapping("/new")
    @Transactional
    fun createTrip(@RequestBody request: NewTripDto, fromUser: HttpServletRequest): ResponseEntity<TripDto> {
        val user = userRepository.findByEmail(fromUser.userPrincipal.name).orElse(null)
        val trip = tripService.createTrip(request, user)
        return ResponseEntity.ok(tripAssembler.toTripDto(trip, trip.members + trip.author))
    }

    /**
     * Get short trip data
     * @param tripId id of trip
     * @param fromUser http request with user principal
     * @return short trip data
     */
    @GetMapping("/{tripId}")
    @Transactional
    fun getTrip(@PathVariable tripId: Long,  fromUser: HttpServletRequest, ): ResponseEntity<TripDto> {
        val user = userRepository.findByEmail(fromUser.userPrincipal.name).orElse(null)
        val trip = tripService.getTrip(user, tripId)
        val response = tripAssembler.toTripDtoWithDays(trip, trip.members + trip.author, weatherService.getWeather(trip))
        return ResponseEntity.ok(response)
    }

    /**
     * Get full trip data
     * @param tripId id of trip
     * @param fromUser http request with user principal
     * @return full trip data
     */
    @GetMapping("/{tripId}/info")
    @Transactional
    fun getTripWithInfo(@PathVariable tripId: Long,  fromUser: HttpServletRequest, ): ResponseEntity<TripInfoDto> {
        val user = userRepository.findByEmail(fromUser.userPrincipal.name).orElse(null)
        val trip = tripService.getTrip(user, tripId)
        val duties = dutyService.getDutyCalculations(trip)
        val response = tripAssembler.toTripDtoWithDuties(trip, trip.members + trip.author,  weather = weatherService.getWeather(trip),  duties = duties)
        return ResponseEntity.ok(response)
    }

    /**
     * Delete a trip
     * @param tripId id of trip
     * @param fromUser http request with user principal
     * @return id of deleted trip
     */
    @DeleteMapping("/{tripId}")
    fun deleteTrip(@PathVariable("tripId") tripId : Long, fromUser: HttpServletRequest): ResponseEntity<Long> {
        val user = userRepository.findByEmail(fromUser.userPrincipal.name).orElseThrow{UserDoesNotExistException()}

        return ResponseEntity.ok(tripService.deleteTrip(tripId, user))
    }

    /**
     * Invite a new user to a trip
     * @param inviteUserDto invite user request
     * @param fromUser http request with user principal
     * @return invited user data
     */
    @PostMapping("user/invite")
    fun inviteUser(@RequestBody inviteUserDto: InviteUserDto, fromUser: HttpServletRequest): ResponseEntity<UserAssembler.UserDto> {
        val user = userRepository.findByEmail(fromUser.userPrincipal.name).orElseThrow{ UserDoesNotExistException() }

        return ResponseEntity.ok(UserAssembler().toUserDto(tripService.inviteUser(inviteUserDto.tripId, inviteUserDto.userEmail, user)))
    }

    /**
     * Change default photo of a trip
     * @param changePhotoDto request to change photo
     * @param fromUser http request with user principal
     * @return short trip data
     */
    @PostMapping("default_photo")
    fun changeDefaultPhoto(@RequestBody changePhotoDto: ChangePhotoDto, fromUser: HttpServletRequest): ResponseEntity<TripDto> {
        val user = userRepository.findByEmail(fromUser.userPrincipal.name).orElseThrow{ UserDoesNotExistException() }
        val trip = tripService.changeDefaultPhoto(changePhotoDto, user)
        return ResponseEntity.ok(tripAssembler.toTripDto(trip, trip.members + trip.author))
    }
}

data class ChangePhotoDto(
    val tripId: Long,
    val photoUrl: String
)

data class InviteUserDto(
    val tripId: Long,
    val userEmail: String
)

data class NewTripDto(
    val title: String,
    val startDay: LocalDateTime,
    val endDay: LocalDateTime,
    val startLocation: LatLng,
    val membersIdExist: List<Long>,
    val newMemberEmails: Set<String>,
    val openingHours: String = "",
)

data class NewDayPointDto(
    val title: String,
    val duration: Duration,
    val typeOfDayPoint: TypeOfDayPoint,
    var dayId: Long,
    val lat: Double,
    val lng: Double,
    val defaultPhoto: String = "",
    val openingHours: String = "",
)

data class ReorderDayPointsDto(
    val movingId: Long,
    val targetBeforeId: Long
)

data class OpeningHoursDto (
    @JsonProperty("open_now") val openNow:Boolean,
    val periods: List<PeriodDto>
) {
    data class PeriodDto(
        val open: TimeMarkerDto,
        val close: TimeMarkerDto?
    )
    data class TimeMarkerDto(
        val day:Int,
        val time: String
    )
}


//val img = mutableListOf<String>(
//    "https://source.unsplash.com/Y4YR9OjdIMk",
//    "https://source.unsplash.com/bELvIg_KZGU",
//    "https://source.unsplash.com/YgYJsFDd4AU",
//    "https://source.unsplash.com/Yc5sL-ejk6U"
//)


//val trips = mutableListOf<Trip>(
//    Trip(
//        0L,
//        "Berlin",
//        LocalDateTime.of(2022, 10, 31, 0, 0),
//        LocalDateTime.of(2022, 10, 15, 0, 0),
//        "https://source.unsplash.com/YgYJsFDd4AU",
//    ),
//    Trip(
//        1L,
//        "Kuala-Lumpur",
//        LocalDateTime.of(2022, 10, 25, 0, 0),
//        LocalDateTime.of(2022, 10, 15, 0, 0),
//        "https://source.unsplash.com/YgYJsFDd4AU",
//    ),
//    Trip(
//        2L,
//        "Prague",
//        LocalDateTime.of(2022, 10, 25, 0, 0),
//        LocalDateTime.of(2022, 10, 15, 0, 0),
//        "https://source.unsplash.com/YgYJsFDd4AU",
//    ),
//    Trip(
//        3L,
//        "Dresden",
//        LocalDateTime.of(2022, 10, 25, 0, 0),
//        LocalDateTime.of(2022, 10, 15, 0, 0),
//        "https://source.unsplash.com/YgYJsFDd4AU",
//    ),
//    Trip(
//        4L,
//        "Moscow",
//        LocalDateTime.of(2022, 10, 25, 0, 0),
//        LocalDateTime.of(2022, 10, 15, 0, 0),
//    ),
//    Trip(
//        5L,
//        "London",
//        LocalDateTime.of(2022, 5, 11, 0, 0),
//        LocalDateTime.of(2022, 5, 22, 0, 0),
//    )
//)

//val days = mutableListOf<Day>(
//    Day(
//        0L,
//        LocalDateTime.of(2022, 10, 25, 0, 0),
//        trips[0],
//    ),
//    Day(
//        1L,
//        LocalDateTime.of(2022, 10, 25, 0, 0),
//        trips[0],
//    ),
//    Day(
//        2L,
//        LocalDateTime.of(2022, 10, 25, 0, 0),
//        trips[0],
//    ),
//    Day(3L, LocalDateTime.of(2022, 10, 25, 0, 0), trips[0]),
//    Day(4L, LocalDateTime.of(2022, 10, 25, 0, 0), trips[0]),
//)
//
//val daysPoints = mutableListOf<DayPoint>(
//    DayPoint(
//        0L,
//        "Start",
//        LocalDateTime.of(2022, 10, 25, 7, 0),
//        Duration.ofMinutes(60 * 60 * 1000),
//        TypeOfDayPoint.START,
//        "https://source.unsplash.com/YgYJsFDd4AU",
//        latitude = 0.0,
//        longitude = 0.0,
//        days[0]
//    ),
//    DayPoint(
//        1L,
//        "Gänsedieb",
//        LocalDateTime.of(2022, 10, 25, 9, 0),
//        Duration.ofMinutes(60 * 60 * 1000),
//        TypeOfDayPoint.FOOD,
//        "https://source.unsplash.com/YgYJsFDd4AU",
//        latitude = 0.0,
//        longitude = 0.0,
//        days[0]
//    ),
//    DayPoint(
//        2L,
//        "Dresden Transport Museum",
//        LocalDateTime.of(2022, 10, 25, 10, 5),
//        Duration.ofMinutes(60 * 60 * 1000),
//        TypeOfDayPoint.SIGHTS,
//        "https://source.unsplash.com/YgYJsFDd4AU",
//        latitude = 0.0,
//        longitude = 0.0,
//        days[0]
//    ),
//    DayPoint(
//        3L,
//        "Biosphärenreservat",
//        LocalDateTime.of(2022, 10, 25, 15, 5),
//        Duration.ofMinutes(60 * 60 * 1000),
//        TypeOfDayPoint.SIGHTS,
//        "https://source.unsplash.com/YgYJsFDd4AU",
//        latitude = 0.0,
//        longitude = 0.0,
//        days[0]
//    ),
//    DayPoint(
//        4L,
//        "Biosphärenreservat",
//        LocalDateTime.of(2022, 10, 25, 15, 5),
//        Duration.ofMinutes(60 * 60 * 1000),
//        TypeOfDayPoint.SIGHTS,
//        "https://source.unsplash.com/YgYJsFDd4AU",
//        latitude = 0.0,
//        longitude = 0.0,
//        days[0]
//    ),
//    DayPoint(
//        5L,
//        "Biosphärenreservat",
//        LocalDateTime.of(2022, 10, 25, 15, 5),
//        Duration.ofMinutes(60 * 60 * 1000),
//        TypeOfDayPoint.SIGHTS,
//        "https://source.unsplash.com/YgYJsFDd4AU",
//        latitude = 0.0,
//        longitude = 0.0,
//        days[0]
//    ),
//    DayPoint(
//        6L,
//        "Biosphärenreservat",
//        LocalDateTime.of(2022, 10, 25, 15, 5),
//        Duration.ofMinutes(60 * 60 * 1000),
//        TypeOfDayPoint.SIGHTS,
//        "https://source.unsplash.com/YgYJsFDd4AU",
//        latitude = 0.0,
//        longitude = 0.0,
//        days[0]
//    ),
//)

