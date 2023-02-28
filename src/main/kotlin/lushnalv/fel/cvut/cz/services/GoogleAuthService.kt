package lushnalv.fel.cvut.cz.services

import lushnalv.fel.cvut.cz.auth.JwtProvider
import lushnalv.fel.cvut.cz.auth.JwtResponse
import lushnalv.fel.cvut.cz.controllers.listAvatars
import lushnalv.fel.cvut.cz.exeptions.GoogleTokenInvalid
import lushnalv.fel.cvut.cz.exeptions.InvalidEmailException
import lushnalv.fel.cvut.cz.models.GoogleResponse
import lushnalv.fel.cvut.cz.models.User
import lushnalv.fel.cvut.cz.repositories.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.Random

@Service
class GoogleAuthService(val userRepository: UserRepository) {
    @Autowired
    lateinit var encoder: PasswordEncoder

    @Autowired
    lateinit var authenticationManager: AuthenticationManager

    @Autowired
    lateinit var jwtProvider: JwtProvider

    val restTemplate = RestTemplate()

    @Throws(GoogleTokenInvalid::class)
    fun verifyGoogleToken(token: String): GoogleResponse {
        try {
            val response = restTemplate.exchange(
                "https://oauth2.googleapis.com/tokeninfo?id_token=$token",
                HttpMethod.GET,
                null,
                GoogleResponse::class.java
            )
            return response.body!!
        } catch (e: Exception) {
            throw GoogleTokenInvalid()
        }
    }
    @Throws(InvalidEmailException::class)
    fun attempt(token: String): JwtResponse {
        val verification = verifyGoogleToken(token)
        var user: User? = userRepository.findByEmail(verification.email).orElse(null)
        if (user == null) {
            if (isEmailValid(verification.email)) {
                user = User(
                    email = verification.email!!,
                    name = verification.name,
                    password = encoder.encode(token),
                    imgUrl = listAvatars[kotlin.random.Random.nextInt(listAvatars.size-1)],
                    signedUp = true
                )
                userRepository.save(user)
            } else {
                throw InvalidEmailException(verification.email)
            }
            val jwt: String = jwtProvider.generateJwtToken(user.email)
            return JwtResponse(id = user.id, email = user.email, name = user.name, accessToken = jwt, imgUrl = user.imgUrl)
        } else {
            if (!user.signedUp) {
                user.signedUp = true
                user.password = encoder.encode(token)
                user.name = verification.name
                userRepository.save(user)
            }
            val jwt: String = jwtProvider.generateJwtToken(user.email)
            return JwtResponse(id = user.id, email = user.email, name = user.name, accessToken = jwt, imgUrl = user.imgUrl)
        }
    }
}