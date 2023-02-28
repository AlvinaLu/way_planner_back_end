package lushnalv.fel.cvut.cz.assemblers

import lushnalv.fel.cvut.cz.models.Day
import lushnalv.fel.cvut.cz.models.User
import org.springframework.stereotype.Component

@Component
class UserAssembler() {

    fun toUserDto(user: User): UserDto{
        return UserDto(user.id, user.email, user.name, user.imgUrl)
    }

    fun toListUserDto(userList: List<User>): List<UserDto>{
        return if(userList.isNotEmpty()){
            userList.map { toUserDto(it) }
        }else{
            listOf()
        }
    }

    fun toUserRegistrationDto(user: User): UserRegistrationDto{
        return UserRegistrationDto(user.name, user.id)
    }

    data class UserRegistrationDto(val name: String, val id: Long)

    data class UserDto(val id: Long, val email: String, val name: String, val imgUrl: String)
}