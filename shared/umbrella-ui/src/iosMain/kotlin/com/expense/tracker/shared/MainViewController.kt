package com.expense.tracker.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.expense.tracker.shared.app.DreamApp
import com.expense.tracker.shared.core.data.database.IosAppDatabaseFactory
import com.expense.tracker.shared.di.initKoin

fun MainViewController() = ComposeUIViewController {
    initKoin(databaseFactory = IosAppDatabaseFactory())
    DreamApp()
}
