package com.utm2maps.data

import com.utm2maps.geo.Hemisphere
import kotlin.test.Test
import kotlin.test.assertEquals

class HistoryRepositoryLogicTest {
    private fun item(id: String, title: String, ts: Long) = CoordinateHistoryItem(id, title, "raw", 625854.0, "439328", 3439328.0, 36, Hemisphere.NORTH, 31.0, 34.0, "url", ts)

    @Test fun saveOne() { assertEquals(1, upsertAndTrimHistory(emptyList(), item("1", "a", 1)).size) }
    @Test fun keepFive() {
        var list = emptyList<CoordinateHistoryItem>()
        (1..6).forEach { list = upsertAndTrimHistory(list, item(it.toString(), "t$it", it.toLong())) }
        assertEquals(5, list.size)
        assertEquals("6", list.first().id)
        assertEquals("2", list.last().id)
    }
    @Test fun duplicateUpdatesAndMovesTop() {
        val existing = listOf(item("1","old",1), item("2","b",2))
        val updated = upsertAndTrimHistory(existing, item("1","new",3))
        assertEquals("1", updated.first().id)
        assertEquals("new", updated.first().title)
        assertEquals(2, updated.size)
    }
    @Test fun deleteRemoves() {
        val list = listOf(item("1","a",1), item("2","b",2)).filterNot { it.id == "1" }
        assertEquals(1, list.size)
        assertEquals("2", list.first().id)
    }
    @Test fun updateTitleOnly() {
        val source = item("1","a",1)
        val changed = source.copy(title = "b")
        assertEquals("1", changed.id)
        assertEquals(source.easting, changed.easting)
        assertEquals("b", changed.title)
    }
}
