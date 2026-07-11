package com.okhamzina.routinetaskmanager.core.error

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AppResultTest {

    @Test
    fun map_transformsSuccessData() {
        val result = AppResult.Success(2).map { it * 3 }

        assertEquals(AppResult.Success(6), result)
    }

    @Test
    fun map_keepsErrorUntouched() {
        val result = AppResult.Error(AppError.Storage).map { value: Int -> value * 3 }

        assertEquals(AppResult.Error(AppError.Storage), result)
    }

    @Test
    fun callbacks_runOnlyForMatchingResultType() {
        var successCalled = false
        var errorCalled = false

        val success = AppResult.Success("ok")
            .onSuccess { successCalled = true }
            .onError { errorCalled = true }

        assertEquals(AppResult.Success("ok"), success)
        assertTrue(successCalled)
        assertFalse(errorCalled)
    }

    @Test
    fun asEmptyDataResult_replacesSuccessDataWithUnit() {
        val result = AppResult.Success("payload").asEmptyDataResult()

        assertSame(Unit, (result as AppResult.Success).data)
    }
}
