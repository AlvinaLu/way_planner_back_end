package lushnalv.fel.cvut.cz.services

import org.springframework.core.env.Environment
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service


@Service
class MailService(val emailSender: JavaMailSender) {
    fun send(to: String, subject: String, text: String) {
        emailSender.send(
            SimpleMailMessage().apply {
                setFrom("wayplanner1@seznam.cz")
                setTo(to)
                setSubject(subject)
                setText(text)
            }
        )
    }
}