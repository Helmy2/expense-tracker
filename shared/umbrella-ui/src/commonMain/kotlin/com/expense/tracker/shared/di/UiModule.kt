package com.expense.tracker.shared.di

import com.expense.tracker.feature.sample.api.navigation.SampleRoute
import com.expense.tracker.feature.sample.impl.di.sampleUiModule
import com.expense.tracker.shared.core.data.local.SampleDatabaseFactory
import com.expense.tracker.shared.navigation.Navigator
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module


val navigatorModule = module {
    single { Navigator(startDestination = SampleRoute) }
}

private var hasStartedKoin = false

fun initKoin(
    sampleDatabaseFactory: SampleDatabaseFactory,
    appContext: Any? = null,
    appDeclaration: KoinAppDeclaration = {}
) {
    if (hasStartedKoin) return

    startKoin {
        appDeclaration()
        modules(sampleDataModule(sampleDatabaseFactory, appContext), sampleUiModule, navigatorModule)
    }
    hasStartedKoin = true
}
