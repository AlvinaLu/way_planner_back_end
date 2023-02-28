package lushnalv.fel.cvut.cz.services

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.maps.model.LatLng
import lushnalv.fel.cvut.cz.models.DayPoint
import lushnalv.fel.cvut.cz.models.TypeOfDayPointActive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.OffsetDateTime

@Service
class BingService(
    @Value("\${bing.api.key}") val key: String,
    val objectMapper: ObjectMapper
) {
    val client = OkHttpClient()

    fun travelInfo(start: DayPoint, finish: LatLng): Travel {
        val walkTravel = travelInfo(start, finish, TravelMode.walking)
        if (walkTravel.travelTime > Duration.ofMinutes(10)) {
            return travelInfo(start, finish, TravelMode.driving)
        } else {
            return walkTravel
        }
    }

    private fun travelInfo(start: DayPoint, finish: LatLng, travelMode: TravelMode): Travel {
        val request = Request.Builder()
            .url("https://dev.virtualearth.net/REST/v1/Routes/DistanceMatrix?key=$key")
            .header("Content-Type", "application/json")
            .post(
                objectMapper.writeValueAsBytes(
                    BingRequest(
                        origins = listOf(
                            LatitudeLongitude(
                                start.latitude,
                                start.longitude
                            )
                        ),
                        destinations = listOf(
                            LatitudeLongitude(
                                finish.lat,
                                finish.lng
                            )
                        ),
                        travelMode = travelMode,
                        startTime = if (travelMode == TravelMode.driving) {
                            start.date.atOffset(OffsetDateTime.now().offset)
                        } else {
                            null
                        }
                    )
                ).toRequestBody("application/json".toMediaType())
            )
            .build()

        val response = client.newCall(request).execute()

        val bingResponse = objectMapper.readValue(response.body!!.bytes(), BingResponse::class.java)

        val result = bingResponse.resourceSets.flatMap { it.resources }.flatMap { it.results }.minByOrNull { it.travelDuration }!!


        return Travel(
            travelTime = Duration.ofSeconds((result.travelDuration * 60).toLong()),
            travelType = if (travelMode == TravelMode.walking) {
                TypeOfDayPointActive.PEDESTRIAN
            } else {
                TypeOfDayPointActive.AUTO
            },
            travelDistance = (result.travelDistance * 1000).toInt()
        )
    }


}

@JsonInclude(Include.NON_NULL)
data class BingRequest(
    val origins: List<LatitudeLongitude>,
    val destinations: List<LatitudeLongitude>? = null,
    val travelMode: TravelMode,
    val startTime: OffsetDateTime? = null,
    val endTime: OffsetDateTime? = null,
    val resolution: Int? = null,
    val timeUnit: TimeUnit = TimeUnit.minute,
    val distanceUnit: DistanceUnit = DistanceUnit.kilometer
)

data class LatitudeLongitude(
    val latitude: Double,
    val longitude: Double,
)

enum class TravelMode {
    driving, walking, transit
}

enum class TimeUnit {
    minute, second
}

enum class DistanceUnit {
    mile, kilometer
}

data class BingResponse(
    val resourceSets: List<ResultSet>
)

data class ResultSet(
    val resources: List<Resource>
)

data class Resource(
    val origins: List<LatitudeLongitude>,
    val destinations: List<LatitudeLongitude>?,
    val results: List<Result>
)

data class Result(
    val travelDistance: Double,
    val travelDuration: Double
)


