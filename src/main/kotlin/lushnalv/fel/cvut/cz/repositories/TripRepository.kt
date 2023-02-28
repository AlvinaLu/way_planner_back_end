package lushnalv.fel.cvut.cz.repositories

import lushnalv.fel.cvut.cz.models.Trip
import lushnalv.fel.cvut.cz.models.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TripRepository: JpaRepository<Trip, Long> {

    override fun findAll(): List<Trip>
    fun findByAuthorId(id: Long): List<Trip>

    @Query("SELECT t FROM Trip t join t.members m where m.id =:id")
    fun findByMembersContainsUser(id: Long): List<Trip>

    override fun findById(id: Long): Optional<Trip>


}