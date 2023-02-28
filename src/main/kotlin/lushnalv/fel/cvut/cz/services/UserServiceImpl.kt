package lushnalv.fel.cvut.cz.services

import lushnalv.fel.cvut.cz.auth.JwtProvider
import lushnalv.fel.cvut.cz.auth.JwtResponse
import lushnalv.fel.cvut.cz.controllers.UserLoginRequest
import lushnalv.fel.cvut.cz.controllers.UpdatePasswordRequest
import lushnalv.fel.cvut.cz.controllers.UserRegistrationRequest
import lushnalv.fel.cvut.cz.controllers.listAvatars
import lushnalv.fel.cvut.cz.exeptions.*
import lushnalv.fel.cvut.cz.models.User
import lushnalv.fel.cvut.cz.repositories.TripRepository
import lushnalv.fel.cvut.cz.repositories.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.regex.Pattern
import kotlin.jvm.Throws
import kotlin.random.Random

@Service
class UserServiceImpl(
    val userRepository: UserRepository,
    val tripRepository: TripRepository,
    val mailService: MailService
) : UserService {

    @Autowired
    lateinit var encoder: PasswordEncoder

    @Autowired
    lateinit var authenticationManager: AuthenticationManager


    @Autowired
    lateinit var jwtProvider: JwtProvider


    @Throws(EmailAlreadyExistException::class, InvalidEmailException::class)
    override fun attemptRegistration(userDetails: UserRegistrationRequest): User {
        if (userRepository.existsUserByEmail(userDetails.email)) {
            val user = userRepository.findByEmail(userDetails.email).get()
            if (user.signedUp) {
                throw EmailAlreadyExistException(userDetails.email)
            }
            user.password = encoder.encode(userDetails.password)
            user.name = userDetails.name
            user.signedUp = true
            userRepository.save(user)
            return user
        } else {
            if (isEmailValid(userDetails.email)) {
                val user = User(
                    email = userDetails.email!!,
                    name = userDetails.name,
                    password = encoder.encode(userDetails.password),
                    imgUrl = listAvatars[kotlin.random.Random.nextInt(listAvatars.size - 1)],
                    signedUp = true
                )
                userRepository.save(user)
                return user

            } else {
                throw InvalidEmailException(userDetails.email)
            }
        }
    }

    @Throws(SingInUnsuccessfulException::class)
    override fun attemptSignIn(loginRequest: UserLoginRequest): JwtResponse {
        val userCandidate: User =
            userRepository.findByEmail(loginRequest.email!!).orElseThrow { SingInUnsuccessfulException() }
        try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(loginRequest.email, loginRequest.password)
            )
            SecurityContextHolder.getContext().authentication = authentication
            val jwt: String = jwtProvider.generateJwtToken(userCandidate.email!!)
            return JwtResponse(
                id = userCandidate.id,
                email = userCandidate.email,
                name = userCandidate.name,
                imgUrl = userCandidate.imgUrl,
                accessToken = jwt
            )
        } catch (e: Exception) {
            throw SingInUnsuccessfulException()
        }


    }

    @Throws(UserDoesNotExistException::class)
    override fun getUserData(id: Long): User {
        return userRepository.findById(id).orElseThrow { UserDoesNotExistException() }
    }

    override fun getAllFriends(id: Long): List<User> {
        val listTripWhereUserIs = tripRepository.findByMembersContainsUser(id)
        val listWhereAuthorIs = tripRepository.findByAuthorId(id)
        val result =
            (listTripWhereUserIs + listWhereAuthorIs).flatMap { it.members + it.author }.toSet().filter { it.id != id }
                .toList()
        return result
    }

    override fun resetPassword(email: String) {
        val user = userRepository.findByEmail(email).orElseThrow { UserDoesNotExistException() }

        user.code = Random.nextInt(1000, 9999)
        userRepository.save(user)

        mailService.send(email, "Password reset code", user.code.toString())
    }

    override fun updatePassword(updatePasswordRequest: UpdatePasswordRequest): User {
        val user = userRepository.findByEmail(updatePasswordRequest.email).orElseThrow { UserDoesNotExistException() }
        if (updatePasswordRequest.code != -1 && updatePasswordRequest.code != user.code) {
            throw IncorrectResetCodeException()
        }
        user.password = encoder.encode(updatePasswordRequest.password)
        user.code = -1
        userRepository.save(user)
        return user
    }
}

fun isEmailValid(email: String): Boolean {
    return Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    ).matcher(email).matches()
}
