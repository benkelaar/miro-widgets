package com.bartenkelaar.widgets

import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap

/**
 * ConcurrentSkipListMap backed implementation of Widget Storage.
 *
 * Concurrent updates over the same index range should not lose any widgets.
 * Resulting order of concurrent updates is undefined, but completeness is guaranteed.
 *
 * Gets an ID generator and supplier of NOW injected. Use ConcurrentWidgetStorage#default
 * to retrieve an instance with these fields injected with fairly sane static defaults.
 */
class ConcurrentWidgetStorage(
    private val generateId: () -> WidgetId,
    private val now: () -> OffsetDateTime
) : WidgetStorage {
    private val orderedStorage = ConcurrentSkipListMap<ZIndex, Widget>()
    private val indexedStorage = ConcurrentHashMap<WidgetId, Widget>()

    override fun createWidget(coordinates: Coordinates, dimensions: Dimensions, zIndex: ZIndex): Widget {
        val id = generateId()
        check(id !in indexedStorage.keys) { "ID collision, please provide better ID generator" }

        val widget = Widget(id, coordinates, dimensions, zIndex, now())
        indexedStorage[id] = widget
        orderedStorage.shiftIn(widget)

        return widget
    }

    override fun updateWidget(
        widgetId: WidgetId,
        coordinates: Coordinates?,
        dimensions: Dimensions?,
        zIndex: ZIndex?
    ): Widget = indexedStorage.compute(widgetId) { _, oldWidget ->
        requireNotNull(oldWidget) { "Unknown WidgetId" }

        val widget = oldWidget.updatedCopy(coordinates, dimensions, zIndex)
        if (zIndex != null) {
            orderedStorage.remove(oldWidget.zIndex)
            orderedStorage.shiftIn(widget)
        }

        widget
    }!!

    override fun getAllSorted() = orderedStorage.values.toList()

    private fun ConcurrentSkipListMap<ZIndex, Widget>.shiftIn(widget: Widget): Widget? =
        put(widget.zIndex, widget)?.let { shiftIn(it.updatedCopy(newZIndex = it.zIndex + 1)) }

    private fun Widget.updatedCopy(newCoords: Coordinates? = null, newDims: Dimensions? = null, newZIndex: ZIndex?) =
        copy(
            coordinates = newCoords ?: coordinates,
            dimensions = newDims ?: dimensions,
            zIndex = newZIndex ?: zIndex,
            lastModificationDate = now(),
        )

    companion object {
        fun default() = ConcurrentWidgetStorage(UUID::randomUUID, OffsetDateTime::now)
    }
}

