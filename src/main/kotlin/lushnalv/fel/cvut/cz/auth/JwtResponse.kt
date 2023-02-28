package lushnalv.fel.cvut.cz.auth

import org.springframework.security.core.GrantedAuthority

class JwtResponse(
    val id: Long,
    val email: String,
    val name: String,
    var imgUrl: String = "",
    var accessToken: String,
) {
    var type = "Bearer"
}