package lushnalv.fel.cvut.cz.services

import lushnalv.fel.cvut.cz.controllers.NewCommentDto
import lushnalv.fel.cvut.cz.models.Comment
import lushnalv.fel.cvut.cz.models.User

interface CommentsService {
    fun createComment(newComment: NewCommentDto, user: User): Comment

    fun deleteComment(commentId: Long, user: User)
}