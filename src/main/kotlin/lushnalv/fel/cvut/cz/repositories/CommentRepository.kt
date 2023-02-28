package lushnalv.fel.cvut.cz.repositories

import lushnalv.fel.cvut.cz.models.Comment
import lushnalv.fel.cvut.cz.models.DayPoint
import lushnalv.fel.cvut.cz.models.Duty
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*
@Repository
interface CommentRepository: JpaRepository<Comment, Long> {
    fun findByDayPointId(dayPoint: DayPoint): List<Comment>

    fun findByAuthorId(id: Long): List<Comment>

    fun getByCommentId(id: Long): Optional<Comment>

}