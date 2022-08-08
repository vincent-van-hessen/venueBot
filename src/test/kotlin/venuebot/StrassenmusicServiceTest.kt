package venuebot

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class StrassenmusicServiceTest {

    @Test
    fun login() {
        // Given
        // When
        val cookies = StrassenmusicService().login("rarspace07@gmail.com", "Start1234567890!")
        val bookings = StrassenmusicService().loadBookings(cookies)
        // Then
        assertNotNull(cookies)
        assertNotNull(bookings)
    }
}