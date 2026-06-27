package com.expense.tracker.shared.core.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences

actual fun createDataStore(context: Any?): DataStore<Preferences> {
    val appContext = (context as Context).applicationContext
    return PreferenceDataStoreFactory.create {
        appContext.filesDir.resolve(DATA_STORE_FILE_NAME)
    }
}
