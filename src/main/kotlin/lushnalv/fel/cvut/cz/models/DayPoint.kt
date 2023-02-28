package lushnalv.fel.cvut.cz.models

import java.io.Serializable
import java.time.Duration
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "`day_points`")
class DayPoint(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,
    @Column(nullable = false)
    val title: String = "",
    @Column()
    var date: LocalDateTime = LocalDateTime.now(),
    @Column()
    var duration: Duration = Duration.ZERO,
    @Column()
    val typesOfDayPointsStable: TypeOfDayPoint = TypeOfDayPoint.UNKNOWN,
    @Column(length = 65000)
    val defaultPhoto: String = "",
    @Column()
    val latitude: Double = 0.0,
    @Column()
    val longitude: Double = 0.0,
    @Column()
    var travelTime: Duration = Duration.ZERO,
    @Column()
    var travelType: TypeOfDayPointActive = TypeOfDayPointActive.AUTO,
    @Column()
    var travelDistance: Int = 0,
    @ManyToOne
    @JoinColumn(name = "day_id", referencedColumnName = "id")
    var day: Day,
    @Column(length = 65000)
    var photoListString: String = "",
    @Column(length = 65000)
    var documentListString: String = "",
    @Column
    var deleted: Boolean = false,
    @Column(length = 65000)
    var openingHours: String?
): Serializable {

    @OneToMany(mappedBy = "dayPoint", targetEntity = Duty::class, cascade = [CascadeType.PERSIST])
    var duties: MutableList<Duty> = mutableListOf()

    @OneToMany(mappedBy = "dayPoint", targetEntity = Comment::class, cascade = [CascadeType.PERSIST])
    var comments: MutableList<Comment> = mutableListOf()

    var photoList: List<String>
        get() {
            return photoListString?.split(",") ?: listOf()
        }
        set(value: List<String>) {
            photoListString = value.joinToString(",");
        }

    var documentList: List<String>
        get() {
            return documentListString?.split(",") ?: listOf()
        }
        set(value: List<String>) {
            documentListString = value.joinToString(",");
        }

    override fun toString(): String {
        return "DayPoint(id=$id, title='$title', date=$date)"
    }


}

enum class TypeOfDayPoint {
    START, FOOD, HOTEL, GAS, SIGHTS, CUSTOM, UNKNOWN
}

val type = TypeOfDayPointActive.AUTO

