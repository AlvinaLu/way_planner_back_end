package lushnalv.fel.cvut.cz.assemblers

import lushnalv.fel.cvut.cz.models.Duty
import lushnalv.fel.cvut.cz.models.User
import lushnalv.fel.cvut.cz.services.DutyServiceImpl
import lushnalv.fel.cvut.cz.utils.CurrencyCode
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class DutyAssembler {

    fun toDutyDto(duty: Duty): DutyDto {
        val users = mutableSetOf<User>()
        duty.users.forEach{
            users.add(it)
        }
        val dutyDto =  DutyDto(
            dutyId = duty.dutyId,
            title = duty.title,
            author = duty.author.id,
            amount = duty.amount,
            currency = duty.currency,
            dayPointId = duty.dayPoint.id,
            users= users.toList().map { it.id },
            deleted = duty.deleted
        )
        return dutyDto
    }

    fun toListDutyDto(dutyList: List<Duty>): List<DutyDto> {
        return if (dutyList.isNotEmpty()) {
            dutyList.map { toDutyDto(it) }
        } else {
            listOf()
        }
    }

    fun toDutyCalculation(transaction: DutyServiceImpl.Transaction):DutyCalculationDto {
        return DutyCalculationDto(transaction.src.id, transaction.trg.id, transaction.amount.setScale(2,RoundingMode.HALF_UP), transaction.currencyCode)
    }
}

data class DutyDto(
    val dutyId: Long,
    val title: String,
    val author: Long,
    val amount: BigDecimal,
    val currency: CurrencyCode,
    var dayPointId: Long,
    var users: List<Long>,
    val deleted: Boolean,
)

data class DutyCalculationDto(
    val sourceUserId:Long,
    val targetUserId:Long,
    val amount:BigDecimal,
    val currency: CurrencyCode
)