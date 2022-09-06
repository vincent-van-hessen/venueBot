package venuebot

import com.google.gson.JsonParser
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class SlotService {

    private fun getAllSlots(): List<Slot> {
        val file = File("times.json")
        val jsonElement = JsonParser.parseString(file.readText())
        val listOfSlots = mutableListOf<Slot>()
        jsonElement.asJsonArray.forEach { entry ->
            if (entry.isJsonObject) {
                val jsonObject = entry.asJsonObject
                val dayOfWeek = when (jsonObject.get("day").asString.lowercase()) {
                    "monday" -> DayOfWeek.MONDAY
                    "tuesday" -> DayOfWeek.TUESDAY
                    "wednesday" -> DayOfWeek.WEDNESDAY
                    "thursday" -> DayOfWeek.THURSDAY
                    "friday" -> DayOfWeek.FRIDAY
                    "saturday" -> DayOfWeek.SATURDAY
                    "sunday" -> DayOfWeek.SUNDAY
                    else -> null
                }
                jsonObject.keySet().filter { it != "day" }.map { lot: String ->
                    try {
                        val timeOfDayString = jsonObject.get(lot).asString
                        val localTime = LocalTime.parse(timeOfDayString)
                        if (dayOfWeek != null) {
                            listOfSlots.add(Slot(lot, dayOfWeek, localTime))
                        } else null
                    } catch (exception: Exception) {
                        println("bad value in config: $lot")
                    }
                }
            }
        }
        return listOfSlots
    }

    fun getSlotsToBeBooked(): List<Slot> {
        val dayOfWeek = LocalDate.now().dayOfWeek
        return getAllSlots().filter { slot ->
            when (dayOfWeek) {
                DayOfWeek.MONDAY -> slot.weekday == DayOfWeek.TUESDAY
                DayOfWeek.TUESDAY -> slot.weekday == DayOfWeek.WEDNESDAY
                DayOfWeek.WEDNESDAY -> slot.weekday == DayOfWeek.THURSDAY
                DayOfWeek.THURSDAY -> slot.weekday == DayOfWeek.FRIDAY
                DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> (slot.weekday == DayOfWeek.SATURDAY || slot.weekday == DayOfWeek.SUNDAY || slot.weekday == DayOfWeek.MONDAY)
                else -> false
            }
        }
    }

}