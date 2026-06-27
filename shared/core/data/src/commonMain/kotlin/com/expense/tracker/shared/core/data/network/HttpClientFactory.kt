package com.expense.tracker.shared.core.data.network

import com.expense.tracker.shared.core.domain.session.SessionStorage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class HttpClientFactory(
    private val sessionStorage: SessionStorage? = null,
    private val baseUrl: String? = null,
) {
    fun create(): HttpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        prettyPrint = false
                    },
                )
            }

            install(Logging) {
                level = LogLevel.ALL
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }

            defaultRequest {
                baseUrl?.let { url(it) }
                contentType(ContentType.Application.Json)
            }

            sessionStorage?.let { storage ->
                install(Auth) {
                    bearer {
                        loadTokens {
                            storage.get()?.let {
                                BearerTokens(it.accessToken, it.refreshToken)
                            }
                        }
                        refreshTokens {
                            storage.get()?.let {
                                BearerTokens(it.accessToken, it.refreshToken)
                            }
                        }
                    }
                }
            }
        }
}
