package lushnalv.fel.cvut.cz.models

import javax.persistence.*
import kotlin.time.Duration

@Entity
@Table(name = "`day_point_activites`")
class DayPointActive(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,
    @Column()
    val duration: Duration =  Duration.ZERO,
    val typesOfDayPointsActive: TypeOfDayPointActive = TypeOfDayPointActive.AUTO,
    val distance: Int

)

enum class TypeOfDayPointActive {
    AUTO, PEDESTRIAN
}