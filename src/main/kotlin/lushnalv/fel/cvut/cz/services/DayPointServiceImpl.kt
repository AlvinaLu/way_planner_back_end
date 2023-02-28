package lushnalv.fel.cvut.cz.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.maps.model.LatLng
import lushnalv.fel.cvut.cz.controllers.*
import lushnalv.fel.cvut.cz.exeptions.*
import lushnalv.fel.cvut.cz.models.*
import lushnalv.fel.cvut.cz.repositories.DayPointRepository
import lushnalv.fel.cvut.cz.repositories.DayRepository
import lushnalv.fel.cvut.cz.repositories.TripRepository
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.jvm.Throws

@Service
class DayPointServiceImpl(
    private val dayPointRepository: DayPointRepository,
    private val dayRepository: DayRepository,
    private val bingService: BingService,
    private val tripRepository: TripRepository,
    private val objectMapper: ObjectMapper
) : DayPointService {
    companion object {
        private val LOG = LoggerFactory.getLogger(DayPointServiceImpl::class.java)
    }

    private val httpClient = OkHttpClient()
    @Throws(
        DayPointDoesNotExistException::class
    )
    override fun getDayPoint(dayPointId: Long): DayPoint {
        val dayPoint = dayPointRepository.findById(dayPointId).orElseThrow { DayPointDoesNotExistException() }
        return dayPoint
    }

    @Throws(
        DayPointDoesNotExistException::class
    )
    override fun addPhotos(dayPointId: Long, photoList: List<String>): DayPoint {
        val newPhotos = photoList
            .filter { !it.isEmpty() }
            .map { fixPhotoUrl(it) }

        val dayPoint = dayPointRepository.findById(dayPointId).orElseThrow { DayPointDoesNotExistException() }
        if (dayPoint.photoList.isEmpty() || (dayPoint.photoList.size == 1 && dayPoint.photoList[0].isEmpty())) {
            dayPoint.photoList = newPhotos.toList()
        } else {
            dayPoint.photoList = newPhotos.toList() + dayPoint.photoList
        }
        dayPointRepository.save(dayPoint)
        return dayPoint
    }

    private fun fixPhotoUrl(photoUrl: String): String {
        if (photoUrl.startsWith("https://maps.googleapis.com")) {
            val request = Request.Builder()
                .url(photoUrl)
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            return response.request.url.toString()
        } else {
            return photoUrl
        }
    }
    @Throws(
        DayPointDoesNotExistException::class
    )
    override fun addDocument(newDocument: NewDocumentDto): DayPoint {
        val dayPoint =
            dayPointRepository.findById(newDocument.dayPointId).orElseThrow { DayPointDoesNotExistException() }
        if (dayPoint.documentList.isEmpty() || (dayPoint.documentList.size == 1 && dayPoint.documentList[0].isEmpty())) {
            dayPoint.documentList = listOf(newDocument.document)
        } else {
            dayPoint.documentList = listOf(newDocument.document) + dayPoint.documentList
        }
        dayPointRepository.save(dayPoint)
        return dayPoint
    }

    @Throws(
        UserCantPerformThisActionException::class
    )
    @Transactional
    override fun deleteDayPoint(dayPointId: Long, user: User): Long {
        val dayPoint = dayPointRepository.findById(dayPointId).orElse(null)
        if (dayPoint == null) {
            return dayPointId
        }
        if (user == dayPoint.day.trip.author || dayPoint.day.trip.members.contains(user)) {
            dayPoint.deleted = true
            val dayPoints = dayPoint.day.dayPoints.filter { !it.deleted }.sortedBy { it.date }.toMutableList()
            if (dayPoints.isNotEmpty()) {
                recalculateTimeLine(dayPoints, dayPoints[0].date)
            }
            dayPointRepository.saveAll(dayPoint.day.dayPoints)
            return dayPointId
        } else {
            throw UserCantPerformThisActionException()
        }
    }

    @Throws(
        UserCantPerformThisActionException::class, DayPointDoesNotExistException::class
    )
    @Transactional
    override fun deleteImage(dayPointId: Long, url: String, user: User) {
        val dayPoint = dayPointRepository.findById(dayPointId).orElseThrow { DayPointDoesNotExistException() }
        if (user == dayPoint.day.trip.author) {
            dayPoint.photoList = dayPoint.photoList.filter { it != url }
            dayPointRepository.save(dayPoint)
        } else {
            throw UserCantPerformThisActionException()
        }
    }

    @Throws(
        DayPointDoesNotExistException::class, UserCantPerformThisActionException::class
    )
    @Transactional
    override fun deleteDocument(dayPointId: Long, url: String, user: User) {
        val dayPoint = dayPointRepository.findById(dayPointId).orElseThrow { DayPointDoesNotExistException() }

        if (user == dayPoint.day.trip.author) {
            dayPoint.documentList = dayPoint.documentList.filter { it != url }
            dayPointRepository.save(dayPoint)
        } else {
            throw UserCantPerformThisActionException()
        }
    }

    @Throws(
        DayDoesNotExist::class,
    )
    override fun createDayPoint(newDayPointDto: NewDayPointDto, user: User): Trip {
        val day = dayRepository.findById(newDayPointDto.dayId)
        if (day.isPresent) {
            val date: LocalDateTime
            val listOfDayPoints = day.get().dayPoints.sortedBy { it.date }
            val travelTime: Duration
            var travelType: TypeOfDayPointActive
            var travelDistance: Int
            if (listOfDayPoints.isNotEmpty()) {
                val lastPoint = listOfDayPoints.last()
                val travelInfo = bingService.travelInfo(lastPoint, LatLng(newDayPointDto.lat, newDayPointDto.lng))
                travelTime = travelInfo.travelTime
                date = lastPoint.date + lastPoint.duration + travelTime
                travelType = travelInfo.travelType
                travelDistance = travelInfo.travelDistance
            } else {
                travelTime = Duration.ZERO
                date = day.get().date.plusHours(7)
                travelType = TypeOfDayPointActive.AUTO
                travelDistance = 0
            }
            val openingHours = getOpeningHours(newDayPointDto.openingHours)
            val dayPoint = DayPoint(
                title = newDayPointDto.title,
                date = date,
                duration = newDayPointDto.duration,
                typesOfDayPointsStable = newDayPointDto.typeOfDayPoint,
                defaultPhoto = fixPhotoUrl(newDayPointDto.defaultPhoto),
                latitude = newDayPointDto.lat,
                longitude = newDayPointDto.lng,
                day = day.get(),
                travelTime = travelTime,
                travelType = travelType,
                travelDistance = travelDistance,
                openingHours = if (openingHours != null) {
                    objectMapper.writeValueAsString(openingHours)
                } else {
                    null
                }
            )
            day.get().dayPoints.add(dayPoint)

            dayPointRepository.save(dayPoint)

            return tripRepository.findById(day.get().trip.id).get()
        } else {
            throw DayDoesNotExist()
        }
    }

    @Throws(
        DayPointDoesNotExistException::class, PointsAreInDifferentDaysException::class
    )
    override fun reorderDayPoints(reorderDayPointsDto: ReorderDayPointsDto, user: User): Trip {
        val movingItem = dayPointRepository.findById(reorderDayPointsDto.movingId)
            .orElseThrow { DayPointDoesNotExistException() }
        val targetItem = dayPointRepository.findById(reorderDayPointsDto.targetBeforeId)
            .orElseThrow { DayPointDoesNotExistException() }


        if (movingItem.day.id == targetItem.day.id) {
            val day = movingItem.day
            val dayPoints = day.dayPoints.filter { !it.deleted }.sortedBy { it.date }.toMutableList()
            val startDate = dayPoints[0].date
            if (dayPoints.indexOf(targetItem) == 0) {
                movingItem.date = targetItem.date
            }
            val indexTarget = dayPoints.indexOf(targetItem)
            val indexMoving = dayPoints.indexOf(movingItem)
            val element = dayPoints.removeAt(indexMoving)

            dayPoints.add(indexTarget, element)

            recalculateTimeLine(dayPoints, startDate)
            day.dayPoints = dayPoints
            dayPointRepository.saveAll(dayPoints)
            dayRepository.save(day)
            return day.trip
        } else {
            throw PointsAreInDifferentDaysException()
        }
    }

    @Throws(
        DayPointDoesNotExistException::class, DayPointIsNotStartPointException::class
    )
    override fun changeDayPointTime(changeDayPointTimeDto: ChangeDayPointTimeDto, user: User): Trip {
        val dayPoint = dayPointRepository.findById(changeDayPointTimeDto.dayPointId)
            .orElseThrow { DayPointDoesNotExistException() }
        val dayPoints = dayPoint.day.dayPoints.filter { !it.deleted }.sortedBy { it.date }.toMutableList()

        if (dayPoints.indexOf(dayPoint) != 0) {
            throw DayPointIsNotStartPointException()
        }

        recalculateTimeLine(dayPoints, changeDayPointTimeDto.date)

        dayPoint.day.dayPoints = dayPoints
        dayRepository.save(dayPoint.day)
        return dayPoint.day.trip
    }

    @Throws(
        DayPointDoesNotExistException::class,
    )
    override fun changeDuration(changeDuration: ChangeDurationDto, user: User): Trip {
        val dayPoint = dayPointRepository.findById(changeDuration.dayPointId)
            .orElseThrow { DayPointDoesNotExistException() }

        dayPoint.duration = changeDuration.duration

        val day = dayPoint.day
        val dayPoints = day.dayPoints.filter { !it.deleted }.sortedBy { it.date }.toMutableList()
        val startDate = dayPoints[0].date

        recalculateTimeLine(dayPoints, startDate)
        day.dayPoints = dayPoints
        dayPointRepository.saveAll(dayPoints)
        dayRepository.save(day)
        return day.trip

    }

    fun recalculateTimeLine(dayPoints: MutableList<DayPoint>, date: LocalDateTime) {
        if (dayPoints.isEmpty() || dayPoints.size == 1) {
            return
        }

        dayPoints[0].travelTime = Duration.ZERO
        dayPoints[0].date = date

        var firstIndex = 0
        var secondIndex = 1

        while (secondIndex < dayPoints.size) {
            val first = dayPoints[firstIndex]
            val second = dayPoints[secondIndex]
            firstIndex++
            secondIndex++


            val travelInfo = bingService.travelInfo(first, LatLng(second.latitude, second.longitude))
            second.date = first.date + first.duration + travelInfo.travelTime
            second.travelType = travelInfo.travelType
            second.travelDistance = travelInfo.travelDistance
            second.travelTime = travelInfo.travelTime
        }
    }

    override fun getOpeningHours(openingHoursString: String): OpeningHours? {
        if (openingHoursString.trim().isEmpty() || openingHoursString == "\"\"") {
            return null
        }

        val openingHoursDto = objectMapper.readValue(openingHoursString, OpeningHoursDto::class.java)

        if (openingHoursDto.periods.any { it.close == null }) {
            return OpeningHours(
                mapOf(
                    DayOfWeek.MONDAY to listOf(OpeningHours.TimeInterval(LocalTime.of(0, 0), LocalTime.of(23, 59, 59))),
                    DayOfWeek.TUESDAY to listOf(
                        OpeningHours.TimeInterval(
                            LocalTime.of(0, 0),
                            LocalTime.of(23, 59, 59)
                        )
                    ),
                    DayOfWeek.WEDNESDAY to listOf(
                        OpeningHours.TimeInterval(
                            LocalTime.of(0, 0),
                            LocalTime.of(23, 59, 59)
                        )
                    ),
                    DayOfWeek.THURSDAY to listOf(
                        OpeningHours.TimeInterval(
                            LocalTime.of(0, 0),
                            LocalTime.of(23, 59, 59)
                        )
                    ),
                    DayOfWeek.FRIDAY to listOf(OpeningHours.TimeInterval(LocalTime.of(0, 0), LocalTime.of(23, 59, 59))),
                    DayOfWeek.SATURDAY to listOf(
                        OpeningHours.TimeInterval(
                            LocalTime.of(0, 0),
                            LocalTime.of(23, 59, 59)
                        )
                    ),
                    DayOfWeek.SUNDAY to listOf(OpeningHours.TimeInterval(LocalTime.of(0, 0), LocalTime.of(23, 59, 59))),
                ).toMap()
            )
        }

        return openingHoursDto.periods.flatMap {
            listOf(
                PeriodTime(
                    getDayOfWeek(it.open.day),
                    OpenStatus.OPEN,
                    LocalTime.of(it.open.time.substring(0, 2).toInt(), it.open.time.substring(2).toInt())
                ),
                PeriodTime(
                    getDayOfWeek(it.open.day),
                    OpenStatus.CLOSE,
                    LocalTime.of(it.close!!.time.substring(0, 2).toInt(), it.close.time.substring(2).toInt())
                ),
            )
        }
            .sortedWith(compareBy({ it.dayOfWeek }, { it.time }))
            .toOpenPeriods()
    }

    private fun getDayOfWeek(day: Int): DayOfWeek {
        if (day == 0) {
            return DayOfWeek.SUNDAY
        } else {
            return DayOfWeek.values()[day - 1]
        }
    }

    private data class PeriodTime(
        val dayOfWeek: DayOfWeek,
        val openStatus: OpenStatus,
        val time: LocalTime
    )

    private enum class OpenStatus {
        OPEN, CLOSE
    }

    private fun List<PeriodTime>.toOpenPeriods(): OpeningHours {
        val result = mutableMapOf<DayOfWeek, MutableList<OpeningHours.TimeInterval>>()
        var openTime: LocalTime? = LocalTime.of(0, 0)
        var dayOfWeek = DayOfWeek.MONDAY
        for (event: PeriodTime in this) {
            if (event.dayOfWeek == dayOfWeek) {
                if (event.openStatus == OpenStatus.CLOSE) {
                    result.computeIfAbsent(dayOfWeek) { mutableListOf() }
                        .add(OpeningHours.TimeInterval(openTime!!, event.time))
                    openTime = null
                } else {
                    openTime = event.time
                }
            } else {
                if (event.openStatus == OpenStatus.CLOSE) {
                    result.computeIfAbsent(dayOfWeek) { mutableListOf() }
                        .add(OpeningHours.TimeInterval(openTime!!, LocalTime.of(23, 59, 59)))
                    dayOfWeek = event.dayOfWeek
                    diffInBetween(dayOfWeek, event.dayOfWeek).forEach {
                        result.computeIfAbsent(dayOfWeek) { mutableListOf() }
                            .add(OpeningHours.TimeInterval(LocalTime.of(0, 0), LocalTime.of(23, 59, 59)))
                    }
                    result.computeIfAbsent(dayOfWeek) { mutableListOf() }
                        .add(OpeningHours.TimeInterval(LocalTime.of(0, 0), event.time))
                    openTime = null
                } else {
                    dayOfWeek = event.dayOfWeek
                    openTime = event.time
                }
            }
        }
        if (openTime != null) {
            result.computeIfAbsent(dayOfWeek) { mutableListOf() }
                .add(OpeningHours.TimeInterval(openTime, LocalTime.of(23, 59, 59)))
        }

        return OpeningHours(result)
    }

    private fun diffInBetween(start: DayOfWeek, end: DayOfWeek): List<DayOfWeek> {
        if (end.ordinal > start.ordinal) {
            return IntRange(start.ordinal, end.ordinal)
                .map { DayOfWeek.values()[it] }
                .filter { it != start && it != end }
        } else {
            return (IntRange(end.ordinal, 6) + IntRange(0, start.ordinal))
                .map { DayOfWeek.values()[it] }
                .filter { it != start && it != end }
        }
    }
}


