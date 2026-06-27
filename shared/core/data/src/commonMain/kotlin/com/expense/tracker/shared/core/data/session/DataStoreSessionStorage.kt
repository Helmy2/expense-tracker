package com.expense.tracker.shared.core.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.expense.tracker.shared.core.domain.session.SessionInfo
import com.expense.tracker.shared.core.domain.session.SessionStorage
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DataStoreSessionStorage(
    private val dataStore: DataStore<Preferences>,
) : SessionStorage {

    private val key = stringPreferencesKey("session_info")
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun get(): SessionInfo? {
        val prefs = dataStore.data.firstOrNull() ?: return null
        val encoded = prefs[key] ?: return null
        return try {
            json.decodeFromString<SessionInfoDto>(encoded).toSessionInfo()
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun set(info: SessionInfo?) {
        dataStore.edit { prefs ->
            if (info == null) {
                prefs.remove(key)
            } else {
                val dto = SessionInfoDto.from(info)
                prefs[key] = json.encodeToString(dto)
            }
        }
    }

    override suspend fun clear() {
        set(null)
    }
}

@Serializable
private data class SessionInfoDto(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
) {
    fun toSessionInfo() = SessionInfo(
        accessToken = accessToken,
        refreshToken = refreshToken,
        userId = userId,
    )

    companion object {
        fun from(info: SessionInfo) = SessionInfoDto(
            accessToken = info.accessToken,
            refreshToken = info.refreshToken,
            userId = info.userId,
        )
    }
}
