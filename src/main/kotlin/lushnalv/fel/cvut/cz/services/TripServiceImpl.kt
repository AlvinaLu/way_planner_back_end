package lushnalv.fel.cvut.cz.services

import com.fasterxml.jackson.databind.ObjectMapper
import lushnalv.fel.cvut.cz.controllers.*
import lushnalv.fel.cvut.cz.exeptions.*
import lushnalv.fel.cvut.cz.models.*
import lushnalv.fel.cvut.cz.repositories.TripRepository
import lushnalv.fel.cvut.cz.repositories.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import kotlin.jvm.Throws

@Service
class TripServiceImpl(
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository,
    private val mailService: MailService,
    private val dayPointService: DayPointService,
    private val objectMapper: ObjectMapper
) : TripService {

    companion object {
        private val LOG = LoggerFactory.getLogger(TripServiceImpl::class.java)
    }

    @Throws(
        InvalidTripTitleException::class, InvalidEmailException::class
    )
    @Transactional
    override fun createTrip(newTripDto: NewTripDto, author: User): Trip {
        if (newTripDto.title.isEmpty() || newTripDto.title.length <= 3) {
            throw InvalidTripTitleException()
        }

        newTripDto.newMemberEmails.firstOrNull { !isEmailValid(it) }?.let {
            throw InvalidEmailException(it)
        }

        val trip = Trip(
            title = newTripDto.title,
            startDay = newTripDto.startDay,
            endDay = newTripDto.endDay,
            author = author
        )

        var days = Duration.between(newTripDto.startDay, newTripDto.endDay).toDays()
        var i = 0
        while (i <= days) {
            if (i == 0) {
                val day = Day(date = newTripDto.startDay, trip = trip)
                val openingHours = dayPointService.getOpeningHours(newTripDto.openingHours)
                val dayPoint = DayPoint(
                    title = "Start point",
                    date = newTripDto.startDay.plusHours(7),
                    typesOfDayPointsStable = TypeOfDayPoint.START,
                    duration = Duration.ZERO, day = day,
                    latitude = newTripDto.startLocation.latitude,
                    longitude = newTripDto.startLocation.longitude,
                    openingHours = if (openingHours != null) {
                        objectMapper.writeValueAsString(openingHours)
                    } else {
                        null
                    }
                )
                day.dayPoints.add(dayPoint)
                trip.days.add(day)
            } else {
                val day = Day(date = newTripDto.startDay.plusDays(i.toLong()), trip = trip)
                trip.days.add(day)
            }
            i++
        }

        var users = userRepository.findAllById(newTripDto.membersIdExist) +
                userRepository.findByEmailIn(newTripDto.newMemberEmails)

        val existingEmails = users.map { it.email }.toSet()

        users = users + newTripDto.newMemberEmails
            .filter { existingEmails.contains(it).not() }
            .map {
                User(
                    email = it,
                    name = it.substringBefore("@"),
                    password = "NOT SET",
                    imgUrl = listAvatars[kotlin.random.Random.nextInt(listAvatars.size - 1)],
                    signedUp = false
                )
            }

        trip.members = users
            .filter { it != author }
            .toMutableList()

        trip.members.forEach {
            it.trips.add(trip)
        }
        users.filter { it != author }.forEach {
            mailService.send(
                it.email,
                "You've been invited to a trip  \uD83D\uDE09",
                "You've been invited to a trip \"${trip.title}\" in app wayPlaner by ${author.name}"
            )

        }

        return tripRepository.save(trip)
    }

    @Throws(
        TripDoesNotExistException::class, UserCantPerformThisActionException::class
    )
    @Transactional
    override fun inviteUser(tripId: Long, userEmail: String, caller: User): User {
        val trip = tripRepository.findById(tripId).orElseThrow { TripDoesNotExistException() }
        if (isEmailValid(userEmail)) {
            if ((trip.members + trip.author).contains(caller)) {
                val user = userRepository.findByEmail(userEmail).orElse(
                    User(
                        email = userEmail,
                        name = userEmail.substringBefore("@"),
                        password = "NOT SET",
                        imgUrl = listAvatars[kotlin.random.Random.nextInt(listAvatars.size - 1)],
                        signedUp = false
                    )
                )
                user.trips.add(trip)
                userRepository.save(user)
                mailService.send(
                    userEmail,
                    "You've been invited to a trip  \uD83D\uDE09",
                    "You've been invited to a trip \"${trip.title}\" in app wayPlaner by ${caller.name}"
                )
                return user
            } else {
                throw UserCantPerformThisActionException()
            }
        } else {
            throw InvalidEmailException(userEmail)
        }
    }
    @Throws(
        UserCantPerformThisActionException::class, TripDoesNotExistException::class
    )
    override fun changeDefaultPhoto(changePhotoDto: ChangePhotoDto, user: User): Trip {
        val trip = tripRepository.findById(changePhotoDto.tripId).orElseThrow { TripDoesNotExistException() }
        if (trip.author == user || trip.members.contains(user)) {
            trip.defaultPhoto = changePhotoDto.photoUrl
            tripRepository.save(trip)
            return trip
        } else {
            throw UserCantPerformThisActionException()
        }
    }

    override fun getAllTrips(user: User): List<Trip> {
        val tripsAuthor = tripRepository.findByAuthorId(user.id)
        val tripsMembers = tripRepository.findByMembersContainsUser(user.id)
        return tripsAuthor + tripsMembers
    }

    @Throws(
        TripDoesNotExistException::class, TripHasNotThisMember::class
    )
    override fun getTrip(user: User, tripId: Long): Trip {
        val trip = tripRepository.findById(tripId)
        if (trip.isPresent) {
            if (trip.get().author == user || trip.get().members.contains(user)) {
                return trip.get()
            } else {
                throw TripHasNotThisMember()
            }
        } else {
            throw TripDoesNotExistException()
        }
    }

    @Throws(
        UserCantPerformThisActionException::class
    )
    @Transactional
    override fun deleteTrip(tripId: Long, user: User): Long {
        val trip = tripRepository.findById(tripId).orElse(null)
        if (trip == null) {
            return tripId
        }

        if (trip.author == user) {
            trip.deleted = true
            tripRepository.save(trip)
            trip.days.flatMap { it.dayPoints }.forEach { dayPointService.deleteDayPoint(it.id, user) }
            return tripId
        } else {
            throw UserCantPerformThisActionException()
        }
    }


//    private fun travelInfo(start: DayPoint, finish: LatLng): Travel {
//        try {
//            val walkDurationInfo = DistanceMatrixApi.newRequest(geoApiContext)
//                .origins(LatLng(start.latitude, start.longitude))
//                //TODO: handle timezone
//                .departureTime((start.date + start.duration).toInstant(ZoneOffset.UTC))
//                .mode(TravelMode.WALKING)
//                .destinations(finish)
//                .await()
//                .rows
//                .first()
//                .elements
//                .first()
//            val walkTravel = Travel(
//                walkDurationInfo.duration.javaDuration(),
//                TypeOfDayPointActive.PEDESTRIAN,
//                walkDurationInfo.distance.inMeters.toInt()
//            )
//
//            if (walkTravel.travelTime > Duration.ofMinutes(10)) {
//                val travelInfo = DistanceMatrixApi.newRequest(geoApiContext)
//                    .origins(LatLng(start.latitude, start.longitude))
//                    //TODO: handle timezone
//                    .departureTime((start.date + start.duration).toInstant(ZoneOffset.UTC))
//                    .destinations(finish)
//                    .await()
//                    .rows
//                    .first()
//                    .elements
//                    .first()
//                return Travel(
//                    travelInfo.duration.javaDuration(),
//                    TypeOfDayPointActive.AUTO,
//                    travelInfo.distance.inMeters.toInt()
//                )
//            } else {
//                return walkTravel;
//            }
//        } catch (ex: ApiException) {
//            LOG.error(ex.message,ex)
//            return Travel(Duration.ZERO, TypeOfDayPointActive.PEDESTRIAN)
//        }
//        return Travel(Duration.ofMinutes(15), TypeOfDayPointActive.PEDESTRIAN, travelDistance = 1050)
//    }


}

private fun com.google.maps.model.Duration.javaDuration(): Duration {
    return Duration.ofSeconds(inSeconds)
}

data class Travel(
    var travelTime: Duration = Duration.ZERO,
    var travelType: TypeOfDayPointActive = TypeOfDayPointActive.AUTO,
    var travelDistance: Int = 0
)
