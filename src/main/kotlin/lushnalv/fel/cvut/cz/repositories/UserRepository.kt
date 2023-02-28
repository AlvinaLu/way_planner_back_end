package lushnalv.fel.cvut.cz.repositories

import lushnalv.fel.cvut.cz.models.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository: JpaRepository<User, Long> {
    fun findByEmail(email: String): Optional<User>
    fun findByEmailIn(emails:Set<String>):List<User>

    fun existsUserByEmail(@Param("email") email: String): Boolean
    override fun findById(id: Long): Optional<User>
    fun findByIdIn(ids:Set<Long>):List<User>
    fun getUserById(id: Long) : User?
}
