package lushnalv.fel.cvut.cz.services


import com.fasterxml.jackson.databind.ObjectMapper
import lushnalv.fel.cvut.cz.assemblers.LatLng
import lushnalv.fel.cvut.cz.models.Trip
import net.minidev.json.annotate.JsonIgnore
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.TimeZone
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

@Service
class OpenMeteoService(val objectMapper: ObjectMapper) : WeatherService {
    private val DEFAULT_WEATHER = WeatherContent(-1, -273.0, 6000000.0, LocalDateTime.MIN)
    private val cache = ConcurrentHashMap<WeatherKey, WeatherContent>()
    private val client = OkHttpClient()

    override fun getWeather(trip: Trip): List<WeatherContent> {
        val tasks: List<CompletableFuture<WeatherContent>> = trip.days
            .map {
                if (it.dayPoints.isEmpty()) {
                    return@map CompletableFuture.completedFuture(DEFAULT_WEATHER)
                }
                val dayPoint = it.dayPoints[0]
                getWeather(WeatherKey(LatLng(dayPoint.latitude, dayPoint.longitude), it.date.toLocalDate()))
            }

        CompletableFuture.allOf(
            *tasks.toTypedArray()
        ).join()

        return tasks.map { it.get() }
    }

    private fun getWeather(weatherKey: WeatherKey): CompletableFuture<WeatherContent> {
        if (weatherKey.date > LocalDate.now().plusDays(15)) {
            return CompletableFuture.completedFuture(DEFAULT_WEATHER);
        }

        val cached = cache.get(weatherKey)
        if (cached != null && cached.expirationTime >= LocalDateTime.now()) {
            return CompletableFuture.completedFuture(cached)
        } else {
            return CompletableFuture.supplyAsync {
                val request = Request.Builder()
                    .url("https://api.open-meteo.com/v1/forecast?latitude=${weatherKey.latLng.latitude}&longitude=${weatherKey.latLng.longitude}&daily=weathercode,temperature_2m_max,temperature_2m_min&timezone=${TimeZone.getDefault().id}&start_date=${weatherKey.date}&end_date=${weatherKey.date}")
                    .get()
                    .build()

                val response = client.newCall(request).execute()

                val weatherResponse = objectMapper.readValue(response.body!!.bytes(), WeatherResponse::class.java)

                val wmoCode = weatherResponse.daily.weathercode.firstOrNull()
                val weatherCode = translate(wmoCode)

                val weatherContent = WeatherContent(weatherCode ?: -1, weatherResponse.daily.temperature_2m_min.first(), weatherResponse.daily.temperature_2m_max.first())
                cache.put(weatherKey, weatherContent)

                return@supplyAsync weatherContent
            }
        }
    }

    private fun translate(wmoCode: Int?): Int? {
        if (wmoCode == null) {
            return null
        }

        if (wmoCode in 0..2) {
            return 41
        }

        if (wmoCode == 3) {
            return 1
        }

        if (wmoCode in 4..9) {
            return 6
        }

        if (wmoCode in 10..19) {
            return 6
        }

        if (wmoCode in 20..21) {
            return 4
        }

        if (wmoCode in 22..23) {
            return 12
        }

        if (wmoCode == 24) {
            return 36
        }

        if (wmoCode == 25) {
            return 15
        }

        if (wmoCode == 26) {
            return 40
        }

        if (wmoCode == 27) {
            return 35
        }

        if (wmoCode == 28) {
            return 6
        }

        if (wmoCode == 29) {
            return 29
        }

        if (wmoCode in 40..49) {
            return 6
        }

        if (wmoCode in 50..52) {
            return 4
        }

        if (wmoCode == 53) {
            return 5
        }

        if (wmoCode in 54..55) {
            return 15
        }

        if (wmoCode in 56..57) {
            return 13
        }

        if (wmoCode in 58..59) {
            return 15
        }

        if (wmoCode == 60) {
            return 18
        }

        if (wmoCode in 62..63) {
            return 15
        }

        if (wmoCode in 64..65) {
            return 30
        }

        if (wmoCode == 66) {
            return 11
        }

        if (wmoCode == 67) {
            return 13
        }

        if (wmoCode == 68) {
            return 11
        }

        if (wmoCode == 69) {
            return 13
        }

        if (wmoCode in 70..71) {
            return 25
        }

        if (wmoCode in 72..73) {
            return 39
        }

        if (wmoCode in 74..79) {
            return 14
        }

        if (wmoCode == 80) {
            return 2
        }

        if (wmoCode == 81) {
            return 17
        }

        if (wmoCode == 82) {
            return 28
        }

        if (wmoCode == 83) {
            return 20
        }

        if (wmoCode == 84) {
            return 35
        }

        if (wmoCode == 85) {
            return 37
        }

        if (wmoCode == 86) {
            return 38
        }

        if (wmoCode == 87) {
            return 37
        }

        if (wmoCode == 88) {
            return 38
        }

        if (wmoCode == 89) {
            return 37
        }

        if (wmoCode == 90) {
            return 38
        }

        if (wmoCode == 91) {
            return 16
        }

        if (wmoCode == 92) {
            return 29
        }

        if (wmoCode == 93) {
            return 22
        }

        if (wmoCode == 94) {
            return 34
        }

        if (wmoCode in 95..99) {
            return 9
        }
        return null
    }
}

data class WeatherKey(val latLng: LatLng, val date: LocalDate)

class WeatherContent(val code: Int, val minTemperature: Double, val maxTemperature: Double, @JsonIgnore val expirationTime: LocalDateTime = LocalDateTime.now().plusHours(1))

data class WeatherResponse(val daily: DailyContent)

data class DailyContent(val time: List<LocalDate>, val weathercode: List<Int>, val temperature_2m_min: List<Double>, val temperature_2m_max: List<Double>)