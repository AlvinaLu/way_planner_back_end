package lushnalv.fel.cvut.cz.repositories

import lushnalv.fel.cvut.cz.models.DayPoint
import lushnalv.fel.cvut.cz.models.Trip
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DayPointRepository: JpaRepository<DayPoint, Long>{
    override fun findById(id: Long): Optional<DayPoint>
}