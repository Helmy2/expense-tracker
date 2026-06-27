package com.expense.tracker.shared.core.testing

import com.expense.tracker.shared.core.domain.TimeProvider
import kotlinx.datetime.TimeZone

class FakeTimeProvider(
    private var current: Long = 0L,
    private val zone: TimeZone = TimeZone.UTC,
) : TimeProvider {
    override fun nowMillis(): Long = current

    override fun timeZone(): TimeZone = zone

    fun setNowMillis(millis: Long) {
        current = millis
    }

    fun advanceBy(deltaMillis: Long) {
        current += deltaMillis
    }
}
