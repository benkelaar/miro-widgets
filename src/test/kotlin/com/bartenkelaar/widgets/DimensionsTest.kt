package com.bartenkelaar.widgets

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class DimensionsTest {
    @Test
    fun `when creating, given negative height, expect IllegalArgumentException`() {
        assertThatThrownBy { Dimensions(-5, 4) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Dimensions should be larger than zero: Dimensions(height=-5, width=4)")
    }

    @Test
    fun `when creating, given zero width, expect IllegalArgumentException`() {
        assertThatThrownBy { Dimensions(14, 0) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Dimensions should be larger than zero: Dimensions(height=14, width=0)")
    }
}