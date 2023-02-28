package lushnalv.fel.cvut.cz.services

import lushnalv.fel.cvut.cz.controllers.ChangePhotoDto
import lushnalv.fel.cvut.cz.controllers.NewDayPointDto
import lushnalv.fel.cvut.cz.controllers.NewTripDto
import lushnalv.fel.cvut.cz.controllers.ReorderDayPointsDto
import lushnalv.fel.cvut.cz.models.Trip
import lushnalv.fel.cvut.cz.models.User

interface TripService {

    fun createTrip(newTripDto: NewTripDto, user: User): Trip
    fun getAllTrips(user: User): List<Trip>

    fun getTrip(user: User, tripId: Long): Trip

    fun deleteTrip(tripId: Long, user: User): Long
    fun inviteUser(tripId: Long, userEmail: String, user: User): User

    fun changeDefaultPhoto(changePhotoDto: ChangePhotoDto, user: User): Trip
}