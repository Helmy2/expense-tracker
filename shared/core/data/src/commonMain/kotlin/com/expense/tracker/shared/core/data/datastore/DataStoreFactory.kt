package com.expense.tracker.shared.core.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

const val DATA_STORE_FILE_NAME = "settings.preferences_pb"

expect fun createDataStore(context: Any? = null): DataStore<Preferences>
