package lushnalv.fel.cvut.cz.services

import lushnalv.fel.cvut.cz.controllers.NewDutyDto
import lushnalv.fel.cvut.cz.exeptions.*
import lushnalv.fel.cvut.cz.models.*
import lushnalv.fel.cvut.cz.repositories.*
import lushnalv.fel.cvut.cz.utils.CurrencyCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.IllegalStateException
import java.math.BigDecimal
import kotlin.jvm.Throws

@Service
class DutyServiceImpl(
    private val dutyRepository: DutyRepository,
    private val dayPointRepository: DayPointRepository,
    private val userRepository: UserRepository
) : DutyService {

    companion object {
        private val LOG = LoggerFactory.getLogger(DutyServiceImpl::class.java)
    }


    @Throws(
        InvalidTripTitleException::class, DayDoesNotExist::class, DayPointDoesNotExistException::class, UserDoesNotExistException::class, AmountIsEmptyOrNegativeException::class, UserIsNotInTripException::class
    )
    @Transactional
    override fun createDuty(newDutyDto: NewDutyDto, user: User): Duty {
        if (newDutyDto.title.isEmpty() || newDutyDto.title.length <= 3) {
            throw InvalidTripTitleException()
        }
        val dayPoint =
            dayPointRepository.findById(newDutyDto.dayPointId).orElseThrow { DayPointDoesNotExistException() }

        if (dayPoint.day.trip.members.contains(user) || dayPoint.day.trip.author == user) {
            if (newDutyDto.amount > BigDecimal.ZERO) {
                val listUsers = userRepository.findByIdIn(newDutyDto.users.toSet())
                if (listUsers.map { it.id }.any { newDutyDto.users.contains(it).not() }) {
                    throw UserDoesNotExistException()
                }
                val duty = Duty(
                    title = newDutyDto.title,
                    amount = newDutyDto.amount,
                    currency = newDutyDto.currency,
                    author = user,
                    dayPoint = dayPoint,
                )
                listUsers.forEach { it.duties.add(duty) }
                dutyRepository.save(duty)
                duty.users = userRepository.saveAll(listUsers)
                duty.dayPoint.duties.add(duty)
                userRepository.save(user)
                return duty
            } else {
                throw AmountIsEmptyOrNegativeException()
            }
        } else {
            throw UserIsNotInTripException()
        }
    }

    override fun getDutyCalculations(trip: Trip): List<Transaction> {
        return trip.days
            .flatMap { it.dayPoints.filter { !it.deleted } }
            .flatMap { it.duties.filter { !it.deleted } }
            .flatMap { createTransactions(it) }
            .groupBy { it.id() }
            .map { it.value.reduce { one, two -> one + two } }
            .map { it.normalize() }
            .flatMap { toBankAccounts(it) }
            .groupBy { it.id() }
            .map { it.value.reduce { one, two -> one + two } }
            .filter { it.amount != BigDecimal.ZERO.setScale(it.amount.scale()) }
            .groupBy { it.currencyCode }
            .flatMap { it.value.toDirectTransactions() }
    }
    @Throws(
        UserCantPerformThisActionException::class
    )
    @Transactional
    override fun deleteDuty(dutyId: Long, user: User): Long {
        val duty = dutyRepository.findById(dutyId).orElse(null)
        if (duty == null) {
            return dutyId
        }
        if (user in setOf(duty.author, duty.dayPoint.day.trip.author)) {
            duty.deleted = true
            dutyRepository.save(duty)
            return dutyId
        } else {
            throw UserCantPerformThisActionException()
        }
    }

    private fun List<BankAccount>.toDirectTransactions(): List<Transaction> {
        if (isEmpty()) {
            return listOf()
        }
        val (pos, neg) = this
            .partition { it.amount > BigDecimal.ZERO }

        val positive = ArrayDeque(pos.sortedBy { it.amount })
        val negative = ArrayDeque(neg.sortedBy { it.amount })

        val result = mutableListOf<Transaction>()

        var src: BankAccount? = negative.removeLast()
        var trg: BankAccount? = positive.removeLast()
        while (src != null && trg != null) {
            val dif = src.amount + trg.amount
            if (dif == BigDecimal.ZERO.setScale(dif.scale())) {
                result.add(Transaction(src.user, trg.user, src.amount.negate(), src.currencyCode))
                if (negative.isNotEmpty() && positive.isNotEmpty()) {
                    src = negative.removeLast()
                    trg = positive.removeLast()
                } else {
                    break
                }
            } else if (dif > BigDecimal.ZERO) {
                result.add(Transaction(src.user, trg.user, src.amount.negate(), src.currencyCode))
                trg.amount = dif
                src = negative.removeLast()
            } else {
                result.add(Transaction(src.user, trg.user, trg.amount, src.currencyCode))
                src.amount = dif
                trg = positive.removeLast()
            }
        }

        return result
    }

    private fun toBankAccounts(transaction: Transaction): List<BankAccount> {
        return listOf(
            BankAccount(transaction.trg, transaction.amount, transaction.currencyCode),
            BankAccount(transaction.src, transaction.amount.negate(), transaction.currencyCode),
        )
    }

    private fun createTransactions(duty: Duty): List<Transaction> {
        val perUser = duty.amount.setScale(5) / duty.users.size.toBigDecimal()

        return duty.users.map { Transaction(it, duty.author, perUser, duty.currency) }
    }

    class Transaction(val src: User, val trg: User, val amount: BigDecimal, val currencyCode: CurrencyCode) {
        fun id() = setOf(src, trg) to currencyCode

        operator fun plus(other: Transaction): Transaction {
            if (other.id() != id()) {
                throw IllegalStateException("Can't add transactions of different users")
            }

            var otherAmount = other.amount
            if (other.src != src) {
                otherAmount = otherAmount.negate();
            }

            return Transaction(src, trg, amount.add(otherAmount), currencyCode)
        }

        fun normalize(): Transaction {
            if (amount < BigDecimal.ZERO) {
                return Transaction(trg, src, amount.negate(), currencyCode)
            } else {
                return this
            }
        }
    }

    class BankAccount(val user: User, var amount: BigDecimal, val currencyCode: CurrencyCode) {
        fun id() = user to currencyCode
        operator fun plus(other: BankAccount): BankAccount {
            if (other.id() != id()) {
                throw IllegalStateException("Can't add transactions of different users")
            }

            return BankAccount(user, amount + other.amount, currencyCode)
        }
    }
}


