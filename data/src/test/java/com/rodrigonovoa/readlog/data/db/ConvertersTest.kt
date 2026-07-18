package com.rodrigonovoa.readlog.data.db

import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `fromStringList and toStringList roundtrip an empty list`() {
        val result = converters.toStringList(converters.fromStringList(emptyList()))

        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `fromStringList and toStringList roundtrip titles with commas and spaces`() {
        val titles = listOf(
            "Cien años de soledad",
            "Foundation, Book 1",
            "El nombre del viento",
        )

        val result = converters.toStringList(converters.fromStringList(titles))

        assertEquals(titles, result)
    }
}
