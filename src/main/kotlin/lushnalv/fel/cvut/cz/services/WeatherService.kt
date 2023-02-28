package lushnalv.fel.cvut.cz.services

import lushnalv.fel.cvut.cz.models.Trip

interface WeatherService {
    fun getWeather(trip: Trip): List<WeatherContent>
}