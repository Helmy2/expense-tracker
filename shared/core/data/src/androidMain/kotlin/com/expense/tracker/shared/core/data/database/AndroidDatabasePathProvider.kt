package com.expense.tracker.shared.core.data.database

import android.content.Context

fun androidDatabaseDirectory(context: Context): String {
    val appContext = context.applicationContext
    return appContext.getDatabasePath("placeholder.db").parent!!
}
