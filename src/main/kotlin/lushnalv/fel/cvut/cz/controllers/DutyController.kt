package lushnalv.fel.cvut.cz.controllers

import lushnalv.fel.cvut.cz.assemblers.*
import lushnalv.fel.cvut.cz.exeptions.TripDoesNotExistException
import lushnalv.fel.cvut.cz.exeptions.UserDoesNotExistException
import lushnalv.fel.cvut.cz.repositories.DayPointRepository
import lushnalv.fel.cvut.cz.repositories.TripRepository
import lushnalv.fel.cvut.cz.repositories.UserRepository
import lushnalv.fel.cvut.cz.services.DutyService
import lushnalv.fel.cvut.cz.utils.CurrencyCode
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("duty")
class DutyController(val dayPointAssembler: DayPointAssembler, val dutyService: DutyService, val userRepository: UserRepository, val tripRepository: TripRepository, val dayPointRepository: DayPointRepository) {
    companion object {
        private val LOG = LoggerFactory.getLogger(TripController::class.java)
    }


    /**
     * Create new Duty at DayPoint
     * @param request: request to create new Duty at DayPoint
     * @param fromUser: http request with user principal
     * @return full DayPoint data
     */
    @Transactional
    @PostMapping("/new")
    fun createDuty(@RequestBody request: NewDutyDto, fromUser: HttpServletRequest): ResponseEntity<DayPointWholeInfoDto> {
        val user = userRepository.findByEmail(fromUser.userPrincipal.name).orElse(null)
        val duty = dutyService.createDuty(request, user)
        val dayPoint = dayPointRepository.findById(duty.dayPoint.id).get()
        val response =  dayPointAssembler.toDayPointWholeInfoDto(
            dayPoint,
            duty.dayPoint.day.trip.members + duty.dayPoint.day.trip.author
        )
        return ResponseEntity.ok(response)
    }

    /**
     * Calculate all trip user debts
     * @param tripId: id of trip
     * @return debts data
     */
    @Transactional
    @GetMapping("/calculation/{tripId}")
    fun getDutyCalculations(@PathVariable tripId: Long): ResponseEntity<List<DutyCalculationDto>> {
        val trip = tripRepository.findById(tripId).orElseThrow { TripDoesNotExistException() }
        return ResponseEntity.ok(dutyService.getDutyCalculations(trip).map { DutyAssembler().toDutyCalculation(it) })
    }


    /**
     * Delete Duty at DayPoint
     * @param dutyId: id of Duty
     * @param fromUser: http request with user principal
     * @return id of deleted Duty
     */
    @DeleteMapping("/{dutyId}")
    fun deleteDuty(@PathVariable("dutyId") dutyId : Long, fromUser: HttpServletRequest): ResponseEntity<Long> {
        val user = userRepository.findByEmail(fromUser.userPrincipal.name).orElseThrow{ UserDoesNotExistException() }

        return ResponseEntity.ok(dutyService.deleteDuty(dutyId, user))
    }
}

data class NewDutyDto(
    val title: String,
    val amount: BigDecimal,
    val currency: CurrencyCode,
    var dayPointId: Long,
    var users: List<Long>,
)