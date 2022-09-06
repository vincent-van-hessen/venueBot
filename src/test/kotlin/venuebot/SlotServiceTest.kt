package venuebot

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SlotServiceTest {

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun getSlotsToBeBooked() {
        // Given
        // When
        val slotsToBeBooked = SlotService().getSlotsToBeBooked()
        // Then
        assertThat(slotsToBeBooked).isNotEmpty
    }
}