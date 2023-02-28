package lushnalv.fel.cvut.cz.exeptions

import lushnalv.fel.cvut.cz.constants.ResponseConstants
import org.springframework.http.HttpStatus

open class ControllerException(message: String, val code: String, val status: HttpStatus = HttpStatus.UNPROCESSABLE_ENTITY) : RuntimeException(message)

class InvalidTripTitleException(message: String = "Trip title can't less then three letter ") : ControllerException(message, ResponseConstants.INVALID_TRIP_TITLE.value)
class DenyAccessException(message: String = "User credential invalid") : ControllerException(message, ResponseConstants.INVALID_CREDENTIAL.value)
class EmailAlreadyExistException(email: String) : ControllerException("Email ${email} already exist!", ResponseConstants.EMAIL_ALREADY_EXIST.value)
class InvalidEmailException(email: String) : ControllerException("Email ${email} is invalid!", ResponseConstants.INVALID_EMAIL.value)
class SingInUnsuccessfulException(message: String = "Email or password is invalid") : ControllerException(message, ResponseConstants.INVALID_USER_DATA.value)
class UserDoesNotExistException(message: String = "User doesn't exist!") : ControllerException(message, ResponseConstants.USER_DOES_NOT_EXIST.value)
class UserCantPerformThisActionException(message: String = "User can't perform this action") : ControllerException(message, ResponseConstants.USER_CANT_PERFORM_THIS_ACTION.value)
class GoogleTokenInvalid(message: String = "Invalid google token!") : ControllerException(message, ResponseConstants.INVALID_GOOGLE_TOKEN.value)
class TripDoesNotExistException(message: String = "Trip does not exist") : ControllerException(message, ResponseConstants.INVALID_TRIP_ID.value)

class DayPointDoesNotExistException(message: String = "DayPoint does not exist") : ControllerException(message, ResponseConstants.INVALID_DAY_POINT_ID.value)
class TripHasNotThisMember(message: String = "Trip has not this member!") : ControllerException(message, ResponseConstants.INVALID_TRIP_OWNER.value)

class DayDoesNotExist(message: String = "Day does not exist") : ControllerException(message, ResponseConstants.INVALID_DAY_POINT_ID.value)

class AmountIsEmptyOrNegativeException(message: String = "Amount is empty or negative") : ControllerException(message, ResponseConstants.AMOUNT_IS_EMPTY_OR_NEGATIVE.value)

class UserIsNotInTripException(message: String = "User is not in trip") : ControllerException(message, ResponseConstants.USER_IS_NOT_IN_TRIP.value)

class PointsAreInDifferentDaysException(message: String = "Points are in different days") : ControllerException(message, ResponseConstants.POINTS_ARE_IN_DIFFERENT_DAYS.value)

class DayPointIsNotStartPointException(message: String = "DayPoint is not a start point") : ControllerException(message, ResponseConstants.DAY_POINT_IS_NOT_START_POINT.value)

class InvalidCommentException(message: String = "Comment can't be empty ") : ControllerException(message, ResponseConstants.INVALID_COMMENT.value)

class IncorrectResetCodeException() : ControllerException("Incorrect reset code", ResponseConstants.INCORRECT_RESET_CODE.value)

