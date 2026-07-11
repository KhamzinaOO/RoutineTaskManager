package com.okhamzina.routinetaskmanager.core.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationRequestCodeGeneratorTest {

    @Test
    fun next_returnsHashWhenItIsUnused() {
        val usedCodes = mutableSetOf<Int>()
        val key = "REMINDER-1-123"

        val code = NotificationRequestCodeGenerator.next(
            key = key,
            usedCodes = usedCodes
        )

        assertEquals(key.hashCode(), code)
        assertTrue(code in usedCodes)
    }

    @Test
    fun next_resolvesCollisionAndStoresResolvedCode() {
        val key = "REMINDER-1-123"
        val usedCodes = mutableSetOf(key.hashCode())

        val code = NotificationRequestCodeGenerator.next(
            key = key,
            usedCodes = usedCodes
        )

        assertNotEquals(key.hashCode(), code)
        assertTrue(code in usedCodes)
        assertEquals(2, usedCodes.size)
    }

    @Test
    fun next_neverReturnsIntMinValue() {
        val usedCodes = mutableSetOf<Int>()

        val code = NotificationRequestCodeGenerator.next(
            key = "polygenelubricants",
            usedCodes = usedCodes
        )

        assertNotEquals(Int.MIN_VALUE, code)
    }
}
