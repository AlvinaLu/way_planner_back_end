package lushnalv.fel.cvut.cz.models

import lushnalv.fel.cvut.cz.utils.CurrencyCode
import java.io.Serializable
import java.math.BigDecimal
import javax.persistence.*

@Entity
@Table(name = "duty")
class Duty(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.AUTO)
    val dutyId: Long = 0,
    @Column(nullable = false)
    val title: String = "",
    @Column()
    val amount: BigDecimal = (0.00).toBigDecimal(),
    @Column()
    val currency: CurrencyCode = CurrencyCode.EUR,
    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id", referencedColumnName = "id")
    var author: User,
    @ManyToOne(optional = false)
    @JoinColumn(name = "day_point_id", referencedColumnName = "id")
    var dayPoint: DayPoint,
    @Column
    var deleted: Boolean = false,
) : Serializable {

    @ManyToMany(mappedBy = "duties", fetch = FetchType.LAZY)
    var users: List<User> = mutableListOf()
}

