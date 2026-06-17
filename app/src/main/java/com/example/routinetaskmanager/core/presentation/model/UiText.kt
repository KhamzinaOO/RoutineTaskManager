package com.example.routinetaskmanager.core.presentation.model

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

sealed interface UiText {
    data class StringResource(
        @StringRes val resId: Int,
        val args: List<Any> = emptyList()
    ) : UiText

    data class PluralResource(
        @PluralsRes val resId: Int,
        val quantity: Int,
        val args: List<Any> = listOf(quantity)
    ) : UiText

    data class DynamicString(val value: String) : UiText
}

fun UiText.asString(context: Context): String {
    return when (this) {
        is UiText.DynamicString -> value
        is UiText.PluralResource -> context.resources.getQuantityString(
            resId,
            quantity,
            *args.toTypedArray()
        )
        is UiText.StringResource -> context.getString(
            resId,
            *args.toTypedArray()
        )
    }
}
