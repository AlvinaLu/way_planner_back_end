package lushnalv.fel.cvut.cz.models

import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*
@Entity
@Table(name = "comment")
class Comment(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.AUTO)
    val commentId: Long = 0,
    @Column()
    var date: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false)
    val message: String = "",
    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id", referencedColumnName = "id")
    var author: User,
    @ManyToOne(optional = false)
    @JoinColumn(name = "day_point_id", referencedColumnName = "id")
    var dayPoint: DayPoint,
    @Column
    var deleted: Boolean = false,

    ): Serializable{

}


