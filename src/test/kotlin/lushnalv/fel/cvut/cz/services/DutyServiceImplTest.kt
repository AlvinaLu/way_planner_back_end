package lushnalv.fel.cvut.cz.services

import lushnalv.fel.cvut.cz.models.*
import lushnalv.fel.cvut.cz.utils.CurrencyCode
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.util.concurrent.atomic.AtomicLong

class DutyServiceImplTest {

    @Test
    fun getDutyCalculationsBasic() {
        val duties = mutableListOf(
            Duty(
                amount = 100.0.toBigDecimal(),
                currency = CurrencyCode.AUD,
                author = User(name = "Vasya", id = 0),
                dayPoint = mockDayPoint(),
            ).apply {
                users = mutableListOf(
                    User(name = "Vasya", id = 0),
                    User(name = "Lena", id = 1)
                )
            }
        )

        val dutyCalculations = DutyServiceImpl(mock(), mock(), mock()).getDutyCalculations(toTrip(duties))
        Assertions.assertEquals(1, dutyCalculations.size)
        Assertions.assertEquals(50.toBigDecimal().setScale(5), dutyCalculations[0].amount)
    }

    @Test
    fun getDutyCalculationsCircle() {
        val userRepo = UserRepo()
        val duties = mutableListOf(
            Duty(
                amount = 100.0.toBigDecimal(),
                currency = CurrencyCode.AUD,
                author = userRepo.user("Vasya"),
                dayPoint = mockDayPoint(),
            ).apply {
                users = mutableListOf(
                    userRepo.user("Vasya"),
                    userRepo.user("Lena")
                )
            },
            Duty(
                amount = 100.0.toBigDecimal(),
                currency = CurrencyCode.AUD,
                author = userRepo.user("Lena"),
                dayPoint = mockDayPoint(),
            ).apply {
                users = mutableListOf(
                    userRepo.user("Lena"),
                    userRepo.user("Petya")
                )
            },
            Duty(
                amount = 100.0.toBigDecimal(),
                currency = CurrencyCode.AUD,
                author = userRepo.user("Petya"),
                dayPoint = mockDayPoint(),
            ).apply {
                users = mutableListOf(
                    userRepo.user("Petya"),
                    userRepo.user("Vasya")
                )
            }
        )

        val dutyCalculations = DutyServiceImpl(mock(), mock(), mock()).getDutyCalculations(toTrip(duties))
        Assertions.assertEquals(0, dutyCalculations.size)
    }

    @Test
    fun getDutyCalculationsNonZero() {
        val userRepo = UserRepo()
        val duties = mutableListOf(
            Duty(
                amount = 150.0.toBigDecimal(),
                currency = CurrencyCode.AUD,
                author = userRepo.user("Vasya"),
                dayPoint = mockDayPoint(),
            ).apply {
                users = mutableListOf(
                    userRepo.user("Vasya"),
                    userRepo.user("Lena"),
                    userRepo.user("Ilya")
                )
            },
            Duty(
                amount = 200.0.toBigDecimal(),
                currency = CurrencyCode.AUD,
                author = userRepo.user("Lena"),
                dayPoint = mockDayPoint(),
            ).apply {
                users = mutableListOf(
                    userRepo.user("Lena"),
                    userRepo.user("Petya")
                )
            },
            Duty(
                amount = 109.0.toBigDecimal(),
                currency = CurrencyCode.AUD,
                author = userRepo.user("Petya"),
                dayPoint = mockDayPoint(),
            ).apply {
                users = mutableListOf(
                    userRepo.user("Petya"),
                    userRepo.user("Vasya")
                )
            }
        )

        val dutyCalculations = DutyServiceImpl(mock(), mock(), mock()).getDutyCalculations(toTrip(duties))
        Assertions.assertEquals(3, dutyCalculations.size)
        Assertions.assertEquals((45.5).toBigDecimal().setScale(5), dutyCalculations[0].amount)
        Assertions.assertEquals("Lena", dutyCalculations[0].trg.name)
        Assertions.assertEquals("Petya", dutyCalculations[0].src.name)
        Assertions.assertEquals((4.5).toBigDecimal().setScale(5), dutyCalculations[1].amount)
        Assertions.assertEquals("Lena", dutyCalculations[1].trg.name)
        Assertions.assertEquals("Ilya", dutyCalculations[1].src.name)
    }

    @Test
    fun getDutyCalculations1() {
        val userRepo = UserRepo()
        val duties = mutableListOf(
            Duty(
                amount = 1000.0.toBigDecimal(),
                currency = CurrencyCode.AUD,
                author = userRepo.user("Vasya"),
                dayPoint = mockDayPoint(),
            ).apply {
                users = mutableListOf(
                    userRepo.user("Vasya"),
                    userRepo.user("Lena"),
                    userRepo.user("Ilya"),
                    userRepo.user("Anna")
                )
            }
        )

        val dutyCalculations = DutyServiceImpl(mock(), mock(), mock()).getDutyCalculations(toTrip(duties))
        Assertions.assertEquals(250.toBigDecimal().setScale(5), dutyCalculations[0].amount)
        Assertions.assertEquals(250.toBigDecimal().setScale(5), dutyCalculations[0].amount)
        Assertions.assertEquals(250.toBigDecimal().setScale(5), dutyCalculations[0].amount)
    }

    private class UserRepo {
        val counter = AtomicLong()
        val map = mutableMapOf<String, User>()

        fun user(name: String): User {
            return map.computeIfAbsent(name) { User(name = name, id = counter.getAndIncrement()) }
        }
    }

    private fun toTrip(duties: MutableList<Duty>): Trip {
        val trip = Trip(author = User())
        val day = Day(trip = trip)
        trip.days = mutableListOf(day)
        val dayPoint = DayPoint(day = day, openingHours = null)
        day.dayPoints = mutableListOf(dayPoint)

        dayPoint.duties = duties
        return trip
    }

    private fun mockDayPoint(): DayPoint {
        val trip = Trip(author = User())
        val day = Day(trip = trip)
        trip.days = mutableListOf(day)
        return DayPoint(day = day, openingHours = null)
    }
}