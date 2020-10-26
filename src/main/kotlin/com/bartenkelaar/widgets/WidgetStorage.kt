package com.bartenkelaar.widgets

/**
 * *A Widget* is an object on a plane in a [Cartesian coordinate system](https://en.wikipedia.org/wiki/Cartesian_coordinate_system)
 * that has coordinates (X, Y), Z-index, width, height, last modification date, and a **unique identifier**.
 * X, Y, and Z-index are integers (may be negative). Width and height are integers > 0.
 * Widget attributes should be not null.
 *
 * A Z-index* is a unique sequence common to all widgets that determines the order of widgets
 * (regardless of their coordinates). **Gaps are allowed.** The higher the value, the higher
 * the widget lies on the plane.
 *
 * The storage maintains the widgets and guards uniqueness of Z-index
 */
interface WidgetStorage {
    /**
     * Having a set of coordinates, dimensions and Z-index, we get a complete widget description
     * result. The ID is generated automatically by the storage. If a Z-index is not specified,
     * the widget moves to the foreground (becomes maximum, minimum 0). If the existing Z-index
     * is specified, then the new widget shifts widget with the same (and greater if needed) upwards.
     *
     * Examples:
     * - Given - 1,2,3; New - **2**; Result - 1,**2**,3,4; Explanation: 2 and 3 were shifted.
     * - Given - 1,5,6; New - **2**; Result - 1,**2**,5,6; Explanation: No one shifted;
     */
    fun createWidget(coordinates: Coordinates, dimensions: Dimensions, zIndex: ZIndex = 0): Widget

    /**
     * Returns an updated full description of the widget. We cannot change the widget id.
     * All changes to widgets must occur atomically. That is, if we change the *XY* coordinates of
     * the widget, then we should not get an intermediate state during concurrent reading.
     * The rules related to the Z-index are the same as when creating a widget.
     */
    fun updateWidget(
        widgetId: WidgetId,
        coordinates: Coordinates? = null,
        dimensions: Dimensions? = null,
        zIndex: ZIndex? = null
    ): Widget

    /**
     * Returns a list of all widgets sorted by Z-index, from smallest to largest.
     */
    fun getAllSorted(): List<Widget>
}