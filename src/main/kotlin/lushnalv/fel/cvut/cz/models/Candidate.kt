package lushnalv.fel.cvut.cz.models

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "candidate")
class Candidate(
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0,

    @Column(unique = true, name = "email")
    @JsonProperty("email")
    var email: String = "",

): Serializable{
    @OneToMany(mappedBy = "author", targetEntity = Trip::class)
    private var invitedTrips: Collection<Trip>? = null
}