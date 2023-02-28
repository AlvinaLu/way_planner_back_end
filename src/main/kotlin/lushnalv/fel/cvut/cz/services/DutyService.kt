package lushnalv.fel.cvut.cz.services

import lushnalv.fel.cvut.cz.controllers.NewDutyDto
import lushnalv.fel.cvut.cz.models.Duty
import lushnalv.fel.cvut.cz.models.Trip
import lushnalv.fel.cvut.cz.models.User

interface DutyService {

    fun createDuty(newDutyDto: NewDutyDto, user: User): Duty
    fun getDutyCalculations(trip: Trip): List<DutyServiceImpl.Transaction>
    fun deleteDuty(dutyId: Long, user: User): Long

//    fun getAllDutiesAuthor(user: User): List<Duty>
//
//    fun getAllDuties(user: User): List<Duty>

}