package venuebot

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.temporal.WeekFields

data class Slot (val lot:String, val weekday:DayOfWeek, val localTime: LocalTime)