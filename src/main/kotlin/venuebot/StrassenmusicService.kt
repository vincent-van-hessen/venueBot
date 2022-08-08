package venuebot

import org.jsoup.Jsoup
import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalTime

class StrassenmusicService {

    private val loginPage = "https://www.dresden.de/apps_ext/StrassenmusikApp_en/login?2-1.IFormSubmitListener-signInPanel-signInForm"
    private val bookingPage = "https://www.dresden.de/apps_ext/StrassenmusikApp_en/applicant?2"
    private val addBookingPage = "https://www.dresden.de/apps_ext/StrassenmusikApp_en/applicant?3-1.IBehaviorListener.0-bookingContainer-listBookings-responsiveTable-bottomLinksContainer-bottomLinks-0-bottomLinkButton&_="

    fun login(username:String, password:String) = HttpHelper.getPage(
        loginPage,
        rawData = "id2_hf_0=&username=${URLEncoder.encode(username, Charsets.UTF_8)}&password=${URLEncoder.encode(password, Charsets.UTF_8)}&submit=Login"
    )

    fun loadBookings(cookies: String): List<String> {
        val page = HttpHelper.getPage(
            url = bookingPage,
            cookies = cookies,
        )
        val document = Jsoup.parse(page)
        return emptyList()
    }

    fun addBooking(slot:String, day:LocalDate, time: LocalTime) {
        val page = HttpHelper.getPage(addBookingPage + "${System.currentTimeMillis()}")
    }

}