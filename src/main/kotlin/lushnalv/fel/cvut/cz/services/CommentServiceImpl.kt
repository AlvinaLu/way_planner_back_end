package lushnalv.fel.cvut.cz.services

import lushnalv.fel.cvut.cz.controllers.NewCommentDto
import lushnalv.fel.cvut.cz.exeptions.DayPointDoesNotExistException
import lushnalv.fel.cvut.cz.exeptions.InvalidCommentException
import lushnalv.fel.cvut.cz.exeptions.UserCantPerformThisActionException
import lushnalv.fel.cvut.cz.models.Comment
import lushnalv.fel.cvut.cz.models.User
import lushnalv.fel.cvut.cz.repositories.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.jvm.Throws

@Service
class CommentServiceImpl(

    private val tripRepository: TripRepository,
    private val userRepository: UserRepository,
    private val dayRepository: DayRepository,
    private val dayPointRepository: DayPointRepository,
    private val commentRepository: CommentRepository
) : CommentsService {
    @Throws(
        DayPointDoesNotExistException::class,
        InvalidCommentException::class
    )
    override fun createComment(newComment: NewCommentDto, user: User): Comment{
        val dayPoint = dayPointRepository.findById(newComment.dayPointId)
        if(dayPoint.isPresent){
            if (newComment.text.isEmpty()) {
                throw InvalidCommentException()

            }else{
                val comment = Comment(
                    date = LocalDateTime.now(),
                    message = newComment.text,
                    author = user,
                    dayPoint = dayPoint.get(),
                )
                commentRepository.save(comment)
                user.comments.add(comment)
                userRepository.save(user)
                dayPoint.get().comments.add(comment)
                dayPointRepository.save(dayPoint.get())
                return comment
            }

        }else{
            throw DayPointDoesNotExistException()
        }
    }

    @Transactional
    override fun deleteComment(commentId: Long, user: User) {
        val comment = commentRepository.findById(commentId).orElse(null) ?: return
        if (user in setOf(comment.author, comment.dayPoint.day.trip.author)) {
            comment.deleted = true
            commentRepository.save(comment)
        } else {
            throw UserCantPerformThisActionException()
        }
    }
}
