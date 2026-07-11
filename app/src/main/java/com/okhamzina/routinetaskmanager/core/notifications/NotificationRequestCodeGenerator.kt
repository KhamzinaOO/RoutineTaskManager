package com.okhamzina.routinetaskmanager.core.notifications

object NotificationRequestCodeGenerator {

    fun next(
        key: String,
        usedCodes: MutableSet<Int>
    ): Int {
        var candidate = key.hashCode().normalize()
        var attempt = 1

        while (!usedCodes.add(candidate)) {
            candidate = (candidate * HASH_STEP + attempt).normalize()
            attempt += 1
        }

        return candidate
    }

    private fun Int.normalize(): Int {
        return if (this == Int.MIN_VALUE) 0 else this
    }

    private const val HASH_STEP = 31
}
