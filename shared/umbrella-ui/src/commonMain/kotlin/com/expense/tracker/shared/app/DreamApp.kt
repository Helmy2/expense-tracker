package com.expense.tracker.shared.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.expense.tracker.feature.recurring.domain.repository.RecurringTemplateRepository
import com.expense.tracker.shared.navigation.AppShell
import org.koin.compose.koinInject

@Composable
fun DreamApp() {
    val recurringRepository = koinInject<RecurringTemplateRepository>()
    LaunchedEffect(Unit) {
        recurringRepository.processDueRecurring()
    }
    AppShell()
}
