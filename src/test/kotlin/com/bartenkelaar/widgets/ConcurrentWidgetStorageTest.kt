package com.bartenkelaar.widgets

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.lang.IllegalStateException
import java.time.OffsetDateTime
import java.util.*
import java.util.UUID.randomUUID

class ConcurrentWidgetStorageTest {
    private val widgetStorage = ConcurrentWidgetStorage.default()
    private val staticTimeStorage = ConcurrentWidgetStorage(UUID::randomUUID) { NOW }

    @Nested
    @DisplayName("When creating widget")
    inner class CreateWidget {
        private val staticWidgetStorage = ConcurrentWidgetStorage({ TEST_ID }) { NOW }

        @Test
        fun `given coordinates and dimensions, expect it created`() {
            val coords = Coordinates(5, 7)
            val dims = Dimensions(100, 200)

            val widget = staticWidgetStorage.createWidget(coords, dims)

            assertThat(widget).isEqualTo(
                Widget(
                    id = TEST_ID,
                    coordinates = coords,
                    dimensions = dims,
                    zIndex = 0,
                    lastModificationDate = NOW,
                )
            )
        }

        @Test
        fun `given duplicate ID, expect IllegalStateException`() {
            val coords = Coordinates(1, 2)
            val dims = Dimensions(10, 10)
            staticWidgetStorage.createWidget(coords, dims)

            assertThatThrownBy { staticWidgetStorage.createWidget(coords, dims) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("ID collision, please provide better ID generator")
        }

        @Test
        fun `given z-index already present, expect existing one shifted up`() {
            val coords = Coordinates(1, 2)
            val dims = Dimensions(10, 10)
            val zIndex = 5
            val widget1 = staticTimeStorage.createWidget(coords, dims, zIndex)
            val widget2 = staticTimeStorage.createWidget(coords, dims, zIndex)

            val list = staticTimeStorage.getAllSorted()

            assertThat(list).containsExactly(
                widget2, widget1.copy(zIndex = 6)
            )
        }

        @Test
        fun `given z-index already present, expect existing and following shifted up, but higher up same`() {
            val coords = Coordinates(1, 2)
            val dims = Dimensions(10, 10)
            val zIndex = -4
            val widget1 = staticTimeStorage.createWidget(coords, dims, zIndex)
            val widget2 = staticTimeStorage.createWidget(coords, dims, zIndex)
            val widget3 = staticTimeStorage.createWidget(coords, dims, zIndex)
            val widgetAt0 = staticTimeStorage.createWidget(coords, dims)

            val list = staticTimeStorage.getAllSorted()

            assertThat(list).containsExactly(
                widget3, widget2.copy(zIndex = -3), widget1.copy(zIndex = -2), widgetAt0
            )
        }

        @Test
        fun `given existing widget that's being shifted, expect it's modification date updated`() {
            val coords = Coordinates(10, 20)
            val dims = Dimensions(1010, 2020)
            val zIndex = 42
            val oldWidget1 = widgetStorage.createWidget(coords, dims, zIndex)

            widgetStorage.createWidget(coords, dims, zIndex)

            val newWidget1 = widgetStorage.getAllSorted().last()
            assertThat(newWidget1.id).isEqualTo(oldWidget1.id)
            assertThat(newWidget1.zIndex).isEqualTo(43)
            assertThat(oldWidget1.lastModificationDate).isBefore(newWidget1.lastModificationDate)
        }
    }

    @Nested
    @DisplayName("When updating widget")
    inner class UpdateWidget {
        @Test
        fun `given unknown ID, expect exception`() {
            assertThatThrownBy { widgetStorage.updateWidget(randomUUID()) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Unknown WidgetId")
        }

        @Test
        fun `given changed zIndex, expect order changed`() {
            val coords = Coordinates(14, 3)
            val dims = Dimensions(100, 300)
            val zIndex = 27
            val widget1 = staticTimeStorage.createWidget(coords, dims, zIndex)
            val widget2 = staticTimeStorage.createWidget(coords, dims, zIndex)
            val widget3 = staticTimeStorage.createWidget(coords, dims, zIndex)

            val updatedWidget3 = staticTimeStorage.updateWidget(widget3.id, zIndex = 29)

            assertThat(staticTimeStorage.getAllSorted()).containsExactly(
                widget2.copy(zIndex = 28), updatedWidget3, widget1.copy(zIndex = 30)
            )
        }

        @Test
        fun `expect modification date changed`() {
            val widget = widgetStorage.createWidget(Coordinates(18, 22), Dimensions(4, 4))

            val updateWidget = widgetStorage.updateWidget(widget.id)

            assertThat(widget.lastModificationDate).isBefore(updateWidget.lastModificationDate)
        }
    }

    companion object {
        private val TEST_ID = randomUUID()
        private val NOW = OffsetDateTime.now()
    }
}