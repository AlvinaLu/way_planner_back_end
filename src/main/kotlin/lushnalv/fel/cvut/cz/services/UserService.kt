package lushnalv.fel.cvut.cz.services

import lushnalv.fel.cvut.cz.auth.JwtResponse
import lushnalv.fel.cvut.cz.controllers.UserLoginRequest
import lushnalv.fel.cvut.cz.controllers.UpdatePasswordRequest
import lushnalv.fel.cvut.cz.controllers.UserRegistrationRequest
import lushnalv.fel.cvut.cz.models.User

interface UserService {

    fun attemptRegistration(userDetails: UserRegistrationRequest): User

    fun attemptSignIn(loginRequest: UserLoginRequest): JwtResponse

    fun getUserData(id: Long) : User

    fun getAllFriends(id: Long) : List<User>
    fun resetPassword(email: String)
    fun updatePassword(updatePasswordRequest: UpdatePasswordRequest): User
}