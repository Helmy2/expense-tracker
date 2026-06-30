package com.expense.tracker.feature.recurring.api.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object RecurringListRoute : NavKey

@Serializable
data class RecurringFormRoute(val templateId: String? = null) : NavKey
