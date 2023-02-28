package lushnalv.fel.cvut.cz.models

import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "`days`")
class Day(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,
    @Column(nullable = false)
    val date: LocalDateTime = LocalDateTime.now(),
    @ManyToOne(optional = false)
    @JoinColumn(name = "trip_id", referencedColumnName = "id")
    var trip: Trip,

    ): Serializable {
    @OneToMany(mappedBy = "day", targetEntity = DayPoint::class, cascade = arrayOf(CascadeType.PERSIST))
    var dayPoints: MutableList<DayPoint> = mutableListOf()
}
