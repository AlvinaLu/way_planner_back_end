package lushnalv.fel.cvut.cz.models

import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "trip")
class Trip(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,
    @Column(nullable = false)
    val title: String = "",
    @Column()
    val startDay: LocalDateTime = LocalDateTime.now(),
    @Column()
    val endDay: LocalDateTime = LocalDateTime.now(),
    @Column(length = 65000)
    var defaultPhoto: String = "",

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    var author: User,

    ): Serializable{

    @Column
    var deleted: Boolean = false

    @ManyToMany(mappedBy = "trips", cascade = [CascadeType.PERSIST], fetch = FetchType.LAZY)
    var members: MutableList<User> = mutableListOf()

    @OneToMany(mappedBy = "trip", cascade = [CascadeType.PERSIST])
    var days: MutableList<Day> = mutableListOf()
}