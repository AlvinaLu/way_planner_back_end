package lushnalv.fel.cvut.cz.controllers

import lushnalv.fel.cvut.cz.assemblers.CommentAssembler
import lushnalv.fel.cvut.cz.assemblers.CommentDto
import lushnalv.fel.cvut.cz.assemblers.DayPointAssembler
import lushnalv.fel.cvut.cz.exeptions.UserDoesNotExistException
import lushnalv.fel.cvut.cz.repositories.UserRepository
import lushnalv.fel.cvut.cz.services.CommentsService
import lushnalv.fel.cvut.cz.services.DayPointService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/comment")
class CommentController(
    val userRepository: UserRepository,
    val dayPointService: DayPointService,
    val dayPointAssembler: DayPointAssembler,
    val commentsService: CommentsService,
    val commentAssembler: CommentAssembler
) {

    /**
     * Post new comment into DayPoint
     * @param newComment new comment request
     * @param fromUser http request with user principal
     * @return response entity with comment
     */
    @PostMapping("/new")
    fun newComment(@RequestBody newComment: NewCommentDto, fromUser: HttpServletRequest): ResponseEntity<CommentDto> {
        val user = userRepository.findByEmail(fromUser.userPrincipal.name).orElse(null)
        val comment = commentsService.createComment(newComment = newComment, user)
        return ResponseEntity.ok(commentAssembler.toCommentDto(comment))
    }

    /**
     * Delete comment from DayPoint
     * @param commentId id of comment
     * @param fromUser http request with user principal
     * @return id of deleted comment
     */
    @DeleteMapping("/{commentId}")
    fun deleteComment(@PathVariable("commentId") commentId : Long, fromUser: HttpServletRequest): ResponseEntity<Long> {
        val user = userRepository.findByEmail(fromUser.userPrincipal.name).orElseThrow { UserDoesNotExistException() }

        commentsService.deleteComment(commentId, user)
        return ResponseEntity.ok(commentId)
    }

}

data class NewCommentDto(
    val dayPointId: Long,
    val text: String,
)