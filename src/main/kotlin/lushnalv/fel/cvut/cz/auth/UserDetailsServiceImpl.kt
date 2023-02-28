package lushnalv.fel.cvut.cz.auth

import lushnalv.fel.cvut.cz.repositories.UserRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class UserDetailsServiceImpl(val userRepository: UserRepository): UserDetailsService {


    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByEmail(username).orElseThrow { UsernameNotFoundException("User '$username' not found") }


        return org.springframework.security.core.userdetails.User
            .withUsername(username)
            .password(user.password)
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(false)
            .authorities("user")
            .build()
    }
}