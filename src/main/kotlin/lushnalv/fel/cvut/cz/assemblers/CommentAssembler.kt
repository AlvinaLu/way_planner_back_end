package lushnalv.fel.cvut.cz.assemblers

import lushnalv.fel.cvut.cz.models.Comment
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class CommentAssembler() {
    fun toCommentDto(comment: Comment): CommentDto {
        return CommentDto(
            id = comment.commentId,
            date = comment.date,
            message = comment.message,
            author = comment.author.id,
            dayPointId = comment.dayPoint.id,
            deleted = comment.deleted
        )
    }

    fun toListCommentDto(commentList: List<Comment>): List<CommentDto> {
        return if (commentList.isNotEmpty()) {
            commentList.map { toCommentDto(it) }
        } else {
            listOf()
        }
    }
}

data class CommentDto(
    val id: Long,
    val date: LocalDateTime,
    val message: String,
    val author: Long,
    val dayPointId: Long,
    val deleted: Boolean
)
