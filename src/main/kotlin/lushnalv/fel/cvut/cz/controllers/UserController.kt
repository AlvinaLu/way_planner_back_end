package lushnalv.fel.cvut.cz.controllers

import lushnalv.fel.cvut.cz.assemblers.UserAssembler
import lushnalv.fel.cvut.cz.auth.JwtResponse
import lushnalv.fel.cvut.cz.exeptions.UserDoesNotExistException
import lushnalv.fel.cvut.cz.models.User
import lushnalv.fel.cvut.cz.repositories.UserRepository
import lushnalv.fel.cvut.cz.services.GoogleAuthService
import lushnalv.fel.cvut.cz.services.UserService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/user")
class UserController(
    val userService: UserService,
    val userRepository: UserRepository,
    val userAssembler: UserAssembler,
    val googleAuthService: GoogleAuthService
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(UserController::class.java)
    }

    /**
     * Register a new user
     * @param userRegistrationRequest user registration request
     * @return new user data
     */
    @PostMapping("/signup")
    fun registerUser(@RequestBody userRegistrationRequest: UserRegistrationRequest): ResponseEntity<UserAssembler.UserDto> {
        LOG.info(
            "REQUEST /user/signup: username - ${userRegistrationRequest.email} " +
                    " password - ${userRegistrationRequest.password}"
        )
        val user = userService.attemptRegistration(userRegistrationRequest)
        val response = userAssembler.toUserDto(user)
        LOG.info(
            "RESPONSE /user/signup: id - ${response.id} " +
                    " email - ${response.email}" +
                    " name - ${response.name}"
        )
        return ResponseEntity.ok(response)
    }

    /**
     * Login a user into system
     * @param loginRequest user login request
     * @return jwt token to be used with authorized requests
     */
    @PostMapping("/signin")
    fun authenticateUser(@RequestBody loginRequest: UserLoginRequest): ResponseEntity<JwtResponse> {
        LOG.info(
            "REQUEST /user/signin: username - ${loginRequest.email} " +
                    " password - ${loginRequest.password}"
        )
        val response = userService.attemptSignIn(loginRequest)
        LOG.info("RESPONSE /user/signin: id - ${response.name} OK")
        return ResponseEntity.ok(response)
    }

    /**
     * Login a user into system using google account
     * @param token token provided by google auth
     * @return jwt token to be used with authorized requests
     */
    @PostMapping("/sign-google")
    fun authenticateUserWithGoogle(@RequestBody token: UserAuthenticationGoogleRequest): ResponseEntity<JwtResponse> {
        LOG.info(
            "REQUEST /user/sign-google: token - ${token.idToken} "
        )
        val response = googleAuthService.attempt(token.idToken)
        LOG.info("RESPONSE /user/sign-google: id  OK")
        return ResponseEntity.ok(response)
    }

    /**
     * Get user data
     * @param userId id of user
     * @return user data
     */
    @GetMapping("/data/{user-id}")
    fun getUserData(@PathVariable(name = "user-id") userId: Long): ResponseEntity<UserAssembler.UserDto> {
        LOG.info(
            "REQUEST /user/data/${userId}"
        )
        val user: User = userService.getUserData(userId)
        LOG.info(
            "RESPONSE /user/data/${userId}" +
                    "email - ${user.email}" +
                    "name - ${user.name}"
        )
        return ResponseEntity.ok(userAssembler.toUserDto(user))

    }

    /**
     * Get friends user data
     * @param fromUser http request with user principal
     * @return user data list
     */
    @GetMapping("/friends")
    fun getUserData(fromUser: HttpServletRequest): ResponseEntity<List<UserAssembler.UserDto>> {
        LOG.info(
            "REQUEST /user/friends"
        )
        val user = userRepository.findByEmail(fromUser.userPrincipal.name).orElse(null)
        val response = userAssembler.toListUserDto(userService.getAllFriends(user.id))
        LOG.info(
            "RESPONSE /user/friends$response"
        )
        return ResponseEntity.ok(response)

    }

    /**
     * Request password reset
     * @param resetPasswordRequest password reset request
     */
    @PostMapping("/reset-password")
    fun resetPassword(@RequestBody resetPasswordRequest: ResetPasswordRequest) {
        return userService.resetPassword(resetPasswordRequest.email)
    }

    /**
     * Update password
     * @param updatePasswordRequest password update request
     * * @return user data
     */
    @PostMapping("/update-password")
    fun updatePassword(@RequestBody updatePasswordRequest: UpdatePasswordRequest): ResponseEntity<UserAssembler.UserDto> {
        return ResponseEntity.ok(userAssembler.toUserDto(userService.updatePassword(updatePasswordRequest)))
    }

}
val listAvatars = listOf<String>("https://firebasestorage.googleapis.com/v0/b/wayplanner.appspot.com/o/avatars%2Ftortoise.png?alt=media&token=30c75321-0095-4983-a4b5-bc21a4548335",
    "https://firebasestorage.googleapis.com/v0/b/wayplanner.appspot.com/o/avatars%2Fhamster.png?alt=media&token=71353469-02da-4d5e-bcf2-44292573121f",
    "https://firebasestorage.googleapis.com/v0/b/wayplanner.appspot.com/o/avatars%2Fbear.png?alt=media&token=11245377-82cc-4bf7-9084-27dfdcfe3a0d",
    "https://firebasestorage.googleapis.com/v0/b/wayplanner.appspot.com/o/avatars%2Fcat.png?alt=media&token=d2a4038c-0fe7-4a30-867c-4e11a22943fb",
    "https://firebasestorage.googleapis.com/v0/b/wayplanner.appspot.com/o/avatars%2Fchicken.png?alt=media&token=c868fabb-5435-423f-852a-c734d557c155",
    "https://firebasestorage.googleapis.com/v0/b/wayplanner.appspot.com/o/avatars%2Fdog.png?alt=media&token=2b85f366-5685-46ab-8443-7f59cf840d8f",
    "https://firebasestorage.googleapis.com/v0/b/wayplanner.appspot.com/o/avatars%2Ffrog.png?alt=media&token=fd0abda0-7c6e-411e-b7a2-ac2ff4f5f041",
    "https://firebasestorage.googleapis.com/v0/b/wayplanner.appspot.com/o/avatars%2Fhedgehog.png?alt=media&token=adaa378a-9a6e-4c95-b6cd-9f17c9b5b3e2",
    "https://firebasestorage.googleapis.com/v0/b/wayplanner.appspot.com/o/avatars%2Fhippo.png?alt=media&token=2b1238f5-7e62-4ca4-85b1-46f011e40631",
    "https://firebasestorage.googleapis.com/v0/b/wayplanner.appspot.com/o/avatars%2Fkangaroo.png?alt=media&token=283b92c3-615d-445d-8677-61313cf2ddb2",
    "https://firebasestorage.googleapis.com/v0/b/wayplanner.appspot.com/o/avatars%2Fmonkey.png?alt=media&token=d86178dc-a19c-4be7-8e53-058430785416",
    "https://firebasestorage.googleapis.com/v0/b/wayplanner.appspot.com/o/avatars%2Fmoose.png?alt=media&token=a83fe35b-703f-4d33-a499-3986842cc408",
    "https://firebasestorage.googleapis.com/v0/b/wayplanner.appspot.com/o/avatars%2Fpanda.png?alt=media&token=7a7208ba-6d1c-4c3a-ab9a-cccbc3c92e7d",
    "https://firebasestorage.googleapis.com/v0/b/wayplanner.appspot.com/o/avatars%2Fparrot.png?alt=media&token=c272cb9e-69b5-4d55-862f-a072234efde4",
    "https://firebasestorage.googleapis.com/v0/b/wayplanner.appspot.com/o/avatars%2Fpenguin.png?alt=media&token=b3ef5ed8-3db7-4d01-83ab-58d84eb60579",
    "https://firebasestorage.googleapis.com/v0/b/wayplanner.appspot.com/o/avatars%2Fplatypus.png?alt=media&token=58b9bf8e-eb9a-423c-9d29-34d6e3ebbcbe",
    "https://firebasestorage.googleapis.com/v0/b/wayplanner.appspot.com/o/avatars%2Fpolar_bear.png?alt=media&token=a78e6512-5d25-48d0-9fad-f1e3f41d4684",
    "https://firebasestorage.googleapis.com/v0/b/wayplanner.appspot.com/o/avatars%2Frhinoceros.png?alt=media&token=f95ece71-da73-4194-b9b1-ee804c7c699e",
    "https://firebasestorage.googleapis.com/v0/b/wayplanner.appspot.com/o/avatars%2Fseal.png?alt=media&token=eba28573-8c5e-4e4c-aff2-d053d4fbdc1f",
    "https://firebasestorage.googleapis.com/v0/b/wayplanner.appspot.com/o/avatars%2Fsquirrel.png?alt=media&token=f4c923ee-4915-4b37-9432-b118affa2710",
    "https://firebasestorage.googleapis.com/v0/b/wayplanner.appspot.com/o/avatars%2Ftiger.png?alt=media&token=43ca3a29-3e10-46ce-b9bc-4cffd2f631ac",
    "https://firebasestorage.googleapis.com/v0/b/wayplanner.appspot.com/o/avatars%2Fwalrus.png?alt=media&token=4a012203-a828-4aba-bc98-73ffef3fafc5",
    "https://firebasestorage.googleapis.com/v0/b/wayplanner.appspot.com/o/avatars%2Ffish.png?alt=media&token=fa6a80e3-503f-4ab8-a24a-c922796c7068")
class UserLoginRequest(val email: String, val password: String)
data class UserRegistrationRequest(val name: String, val email: String, val password: String)
data class UpdatePasswordRequest(val email: String, val password: String, val code: Int)
data class ResetPasswordRequest(val email: String)
data class UserAuthenticationGoogleRequest(val idToken: String)
