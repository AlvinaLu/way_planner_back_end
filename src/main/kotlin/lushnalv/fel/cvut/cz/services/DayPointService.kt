package lushnalv.fel.cvut.cz.services

import lushnalv.fel.cvut.cz.controllers.*
import lushnalv.fel.cvut.cz.models.DayPoint
import lushnalv.fel.cvut.cz.models.OpeningHours
import lushnalv.fel.cvut.cz.models.Trip
import lushnalv.fel.cvut.cz.models.User


interface DayPointService {
    fun getDayPoint(dayPointId: Long): DayPoint

    fun addPhotos(dayPointId: Long, photoList: List<String>): DayPoint

    fun addDocument(newDocument: NewDocumentDto): DayPoint
    fun deleteDayPoint(dayPointId: Long, user: User): Long
    fun deleteImage(dayPointId: Long, url: String, user: User)
    fun deleteDocument(dayPointId: Long, url: String, user: User)

    fun createDayPoint(newDayPointDto: NewDayPointDto, user: User): Trip

    fun reorderDayPoints(reorderDayPointsDto: ReorderDayPointsDto, user: User): Trip
    fun getOpeningHours(openingHoursString: String): OpeningHours?

   fun changeDuration(changeDuration: ChangeDurationDto, user: User): Trip
    fun changeDayPointTime(changeDayPointTimeDto: ChangeDayPointTimeDto, user: User): Trip
}