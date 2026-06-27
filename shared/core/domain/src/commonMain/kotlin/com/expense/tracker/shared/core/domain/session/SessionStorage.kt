package com.expense.tracker.shared.core.domain.session

interface SessionStorage {
    suspend fun get(): SessionInfo?

    suspend fun set(info: SessionInfo?)

    suspend fun clear()
}
