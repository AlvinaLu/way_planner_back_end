package lushnalv.fel.cvut.cz.controllers

import lushnalv.fel.cvut.cz.assemblers.DayPointAssembler
import lushnalv.fel.cvut.cz.assemblers.DayPointWholeInfoDto
import lushnalv.fel.cvut.cz.assemblers.TripAssembler
import lushnalv.fel.cvut.cz.assemblers.TripDto
import lushnalv.fel.cvut.cz.exeptions.UserCantPerformThisActionException
import lushnalv.fel.cvut.cz.exeptions.UserDoesNotExistException
import lushnalv.fel.cvut.cz.repositories.UserRepository
import lushnalv.fel.cvut.cz.services.DayPointService
import lushnalv.fel.cvut.cz.services.WeatherService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.time.Duration
import java.time.LocalDateTime
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/day_point")
class DayPointController(
    private val userRepository: UserRepository,
    private val dayPointService: DayPointService,
    private val dayPointAssembler: DayPointAssembler,
    private val tripAssembler: TripAssembler,
    private val weatherService: WeatherService
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(DayPointController::class.java)
    }


    /**
     * Get DayPoint data
     * @param dayPointId id of DayPoint
     * @param fromUser http request with user principal
     * @return DayPoint data
     */
    @GetMapping("/{dayPointId}")
    @Transactional
    fun getDayPoint(@PathVariable dayPointId: Long, fromUser: HttpServletRequest): ResponseEntity<DayPointWholeInfoDto> {
        val user = userRepository.findByEmail(fromUser.userPrincipal.name).orElseThrow { UserDoesNotExistException() }
        val dayPoint = dayPointService.getDayPoint(dayPointId)
        if (!(dayPoint.day.trip.members + dayPoint.day.trip.author).contains(user)) {
            throw UserCantPerformThisActionException()
        }
        val response = dayPointAssembler.toDayPointWholeInfoDto(
            dayPoint,
            dayPoint.day.trip.members + dayPoint.day.trip.author
        )
        return ResponseEntity.ok(
            response
        )
    }

    /**
     * Post new photos into DayPoint
     * @param newPhotoList request to post photos
     * @param fromUser http request with user principal
     * @return DayPoint data
     */
    @PostMapping("/add_photo")
    @Transactional
    fun addPhotos(
        @RequestBody newPhotoList: NewPhotoListDto,
        fromUser: HttpServletRequest
    ): ResponseEntity<DayPointWholeInfoDto> {
        val user = userRepository.findByEmail(fromUser.userPrincipal.name).orElseThrow { UserDoesNotExistException() }
        val dayPoint = dayPointService.addPhotos(newPhotoList.dayPointId, newPhotoList.photoList)
        if (!(dayPoint.day.trip.members + dayPoint.day.trip.author).contains(user)) {
            throw UserCantPerformThisActionException()
        }
        val response =
            dayPointAssembler.toDayPointWholeInfoDto(dayPoint, dayPoint.day.trip.members + dayPoint.day.trip.author)
        return ResponseEntity.ok(response)
    }

    /**
     * Post new document into DayPoint
     * @param newDocument request to post document
     * @param fromUser http request with user principal
     * @return DayPoint data
     */
    @PostMapping("/add_document")
    @Transactional
    fun addDocument(
        @RequestBody newDocument: NewDocumentDto,
        fromUser: HttpServletRequest
    ): ResponseEntity<DayPointWholeInfoDto> {
        val user = userRepository.findByEmail(fromUser.userPrincipal.name).orElseThrow { UserDoesNotExistException() }
        val dayPoint = dayPointService.addDocument(newDocument)
        val response =
            dayPointAssembler.toDayPointWholeInfoDto(dayPoint, dayPoint.day.trip.members + dayPoint.day.trip.author)
        return ResponseEntity.ok(response)
    }

    /**
     * Delete DayPoint
     * @param dayPointId id of DayPoint
     * @param fromUser http request with user principal
     * @return id of deleted DayPoint
     */
    @DeleteMapping("/{dayPointId}")
    fun deleteDayPoint(@PathVariable("dayPointId") dayPointId: Long, fromUser: HttpServletRequest): ResponseEntity<Long> {
        val user = userRepository.findByEmail(fromUser.userPrincipal.name).orElseThrow { UserDoesNotExistException() }

        return ResponseEntity.ok(dayPointService.deleteDayPoint(dayPointId, user))
    }

    /**
     * Delete image from DayPoint
     * @param deleteImageDto request to delete image
     * @param fromUser http request with user principal
     * @return request to delete image
     */
    @DeleteMapping("/image")
    fun deleteDayPointImage(@RequestBody deleteImageDto: DeleteImageDto, fromUser: HttpServletRequest): ResponseEntity<DeleteImageDto> {
        val user = userRepository.findByEmail(fromUser.userPrincipal.name).orElseThrow { UserDoesNotExistException() }
        dayPointService.deleteImage(deleteImageDto.dayPointId, deleteImageDto.url, user)
        return ResponseEntity.ok(deleteImageDto)
    }

    /**
     * Delete document from DayPoint
     * @param deleteDocumentDto request to delete document
     * @param fromUser http request with user principal
     * @return request to delete document
     */
    @DeleteMapping("/document")
    fun deleteDayPointDocument(@RequestBody deleteDocumentDto: DeleteDocumentDto, fromUser: HttpServletRequest): ResponseEntity<DeleteDocumentDto> {
        val user = userRepository.findByEmail(fromUser.userPrincipal.name).orElseThrow { UserDoesNotExistException() }

        dayPointService.deleteDocument(deleteDocumentDto.dayPointId, deleteDocumentDto.url, user)

        return ResponseEntity.ok(deleteDocumentDto)
    }

    /**
     * Create new DayPoint
     * @param request request to create DayPoint
     * @param fromUser http request with user principal
     * @return short trip data
     */
    @PostMapping("/new")
    fun newDayPoint(@RequestBody request: NewDayPointDto, fromUser: HttpServletRequest ): ResponseEntity<TripDto> {
        val user = userRepository.findByEmail(fromUser.userPrincipal.name).orElse(null)
        val trip = dayPointService.createDayPoint(newDayPointDto = request, user)
        val response = tripAssembler.toTripDtoWithDays(trip, trip.members+ trip.author, weatherService.getWeather(trip))
        return ResponseEntity.ok(response)
    }

    /**
     * Change DayPoint start time
     * @param changeDayPointTimeDto request to change DayPoint start time
     * @param fromUser http request with user principal
     * @return short trip data
     */
    @PostMapping("/change_time")
    fun changeDayPointTime(@RequestBody changeDayPointTimeDto: ChangeDayPointTimeDto, fromUser: HttpServletRequest ): ResponseEntity<TripDto> {
        val user = userRepository.findByEmail(fromUser.userPrincipal.name).orElse(null)
        val trip = dayPointService.changeDayPointTime(changeDayPointTimeDto, user)
        return ResponseEntity.ok(tripAssembler.toTripDtoWithDays(trip, trip.members + trip.author, weatherService.getWeather(trip)))
    }

    /**
     * Reorder DayPoints
     * @param reorderDayPointsDto request to reorder DayPoints
     * @param fromUser http request with user principal
     * @return short trip data
     */
    @PostMapping("/reorder")
    fun reorderDayPoints(@RequestBody reorderDayPointsDto: ReorderDayPointsDto, fromUser: HttpServletRequest ): ResponseEntity<TripDto> {
        LOG.info(
            "REQUEST /day_point/reorder: 1 point  - ${reorderDayPointsDto.movingId} " + " 2 point - ${reorderDayPointsDto.targetBeforeId}"
        )
        val user = userRepository.findByEmail(fromUser.userPrincipal.name).orElse(null)
        val trip = dayPointService.reorderDayPoints(reorderDayPointsDto, user)
        return ResponseEntity.ok(tripAssembler.toTripDtoWithDays(trip, trip.members + trip.author, weatherService.getWeather(trip)))
    }

    /**
     * Change DayPoint duration
     * @param changeDuration request to change DayPoint duration
     * @param fromUser http request with user principal
     * @return short trip data
     */
    @PostMapping("/change")
    fun changeDurationDayPoint(@RequestBody changeDuration: ChangeDurationDto, fromUser: HttpServletRequest ): ResponseEntity<TripDto> {
        LOG.info(
            "REQUEST day_point/change: dayPoint  - ${changeDuration.dayPointId} " + " duration - ${changeDuration}"
        )
        val user = userRepository.findByEmail(fromUser.userPrincipal.name).orElse(null)
        val trip = dayPointService.changeDuration(changeDuration, user)
        return ResponseEntity.ok(tripAssembler.toTripDtoWithDays(trip, trip.members + trip.author, weatherService.getWeather(trip)))
    }
}

data class DeleteImageDto(val dayPointId: Long, val url: String)
data class DeleteDocumentDto(val dayPointId: Long, val url: String)

data class NewPhotoListDto(
    val dayPointId: Long,
    val photoList: List<String>,
)

data class NewDocumentDto(
    val dayPointId: Long,
    val document: String,
)
data class ChangeDurationDto(
    val dayPointId: Long,
    val duration: Duration,
)


data class ChangeDayPointTimeDto(
    val dayPointId: Long,
    val date: LocalDateTime
)
