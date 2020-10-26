package com.bartenkelaar.widgets

import java.time.OffsetDateTime
import java.util.*

data class Widget(
    val id: WidgetId,
    val coordinates: Coordinates,
    val dimensions: Dimensions,
    val zIndex: ZIndex,
    val lastModificationDate: OffsetDateTime
)

typealias WidgetId = UUID
typealias ZIndex = Int

data class Coordinates(val x: Int, val y: Int)

data class Dimensions(val height: Int, val width: Int) {
    init {
        require(height > 0 && width > 0) { "Dimensions should be larger than zero: $this" }
    }
}
