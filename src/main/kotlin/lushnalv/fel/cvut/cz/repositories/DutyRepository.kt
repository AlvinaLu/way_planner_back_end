package lushnalv.fel.cvut.cz.repositories

import lushnalv.fel.cvut.cz.models.DayPoint
import lushnalv.fel.cvut.cz.models.Duty
import lushnalv.fel.cvut.cz.models.Trip
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DutyRepository: JpaRepository<Duty, Long> {

    override fun findAll(): List<Duty>

    fun findByAuthorId(id: Long): List<Duty>

    @Query("SELECT t FROM Duty t join t.users u where u.id =:id")
    fun findByUsersContainsUser(id: Long): List<Duty>

    fun findByDutyId(id: Long): Optional<Duty>

    fun findByDayPointId(dayPoint: DayPoint): Optional<Duty>
}