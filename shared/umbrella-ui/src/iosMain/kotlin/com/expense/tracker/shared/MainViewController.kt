package com.expense.tracker.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.expense.tracker.feature.expense.data.local.IosTransactionDatabaseFactory
import com.expense.tracker.shared.app.DreamApp
import com.expense.tracker.shared.di.initKoin

fun MainViewController() = ComposeUIViewController {
    initKoin(
        transactionDatabaseFactory = IosTransactionDatabaseFactory(),
    )
    DreamApp()
}
