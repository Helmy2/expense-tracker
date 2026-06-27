package com.expense.tracker.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.expense.tracker.feature.budget.data.local.AndroidBudgetDatabaseFactory
import com.expense.tracker.feature.expense.data.local.AndroidTransactionDatabaseFactory
import com.expense.tracker.shared.app.DreamApp
import com.expense.tracker.shared.di.initKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        initKoin(
            transactionDatabaseFactory = AndroidTransactionDatabaseFactory(applicationContext),
            budgetDatabaseFactory = AndroidBudgetDatabaseFactory(applicationContext),
            appContext = applicationContext,
        )
        enableEdgeToEdge()
        setContent {
            DreamApp()
        }
    }
}
