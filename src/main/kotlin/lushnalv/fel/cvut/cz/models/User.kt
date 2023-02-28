package lushnalv.fel.cvut.cz.models

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.format.annotation.DateTimeFormat
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0,

    @Column(unique = true, name = "email")
    @JsonProperty("email")
    var email: String = "",

    @Column
    var name: String = "",

    @Column
    @JsonProperty("password")
    var password: String = "",

    @Column(name="code_activated")
    var codeActivated: Boolean = false,

    @Column(name="code")
    var code: Int = -1,

    @Column(name = "img_url")
    var imgUrl: String = "",

    @Column(name = "created_at")
    @DateTimeFormat
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "last_online")
    @DateTimeFormat
    var lastOnline: LocalDateTime = LocalDateTime.now(),

    @Column(name="signed_up")
    var signedUp: Boolean = false

    ): Serializable {
    @OneToMany(mappedBy = "author", targetEntity = Trip::class)
    private var ownTrips: Collection<Trip>? = null

    @ManyToMany(cascade = [CascadeType.PERSIST], fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_trips",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "trip_id")]
    )
    var trips: MutableList<Trip> = mutableListOf()

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_duty",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "duty_id")]
    )
    var duties: MutableList<Duty> = mutableListOf()

    @ManyToMany(cascade = [CascadeType.PERSIST], fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_comments",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "comment_id")]
    )
    var comments: MutableList<Comment> = mutableListOf()


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "User(id=$id, name='$name')"
    }


}