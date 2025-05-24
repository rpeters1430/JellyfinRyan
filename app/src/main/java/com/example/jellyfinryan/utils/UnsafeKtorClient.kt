package com.example.jellyfinryan.utils

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object UnsafeKtorClient {

    private const val TAG = "UnsafeKtorClient"

    /**
     * Creates a Ktor HttpClient that bypasses ALL SSL certificate validation
     * This is specifically designed for Jellyfin servers with self-signed certificates
     */
    fun createUnsafeKtorClient(): HttpClient {
        return try {
            Log.d(TAG, "Creating unsafe Ktor client with comprehensive SSL bypass")

            // Create trust manager that accepts ALL certificates
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                    // Accept all client certificates
                    Log.v(TAG, "Accepting client certificate: $authType")
                }

                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                    // Accept all server certificates
                    Log.v(TAG, "Accepting server certificate: $authType")
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            })

            // Create SSL context with trust-all manager
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())

            // Create HttpClient with OkHttp engine and comprehensive SSL bypass
            HttpClient(OkHttp) {
                // Configure OkHttp engine with SSL bypass
                engine {
                    config {
                        sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                        hostnameVerifier { hostname, session ->
                            Log.v(TAG, "Bypassing hostname verification for: $hostname")
                            true // Accept all hostnames
                        }

                        // Additional SSL bypass settings
                        retryOnConnectionFailure(true)
                        followRedirects(true)
                        followSslRedirects(true)

                        // Configure timeouts at OkHttp level instead of Ktor level
                        connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                        readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    }
                }

                // Install default request headers for Jellyfin/Emby compatibility
                install(DefaultRequest) {
                    headers.append("X-Emby-Client", "JellyfinRyan")
                    headers.append("X-Emby-Client-Version", "1.0")
                    headers.append("X-Emby-Device", "AndroidTV")
                    headers.append("X-Emby-Device-Id", "jellyfin-ryan-android-tv")
                    headers.append("X-Emby-Device-Name", "JellyfinRyan Android TV")
                    headers.append("User-Agent", "JellyfinRyan/1.0 (Android TV)")
                    headers.append("Accept", "application/json")
                    headers.append("Content-Type", "application/json")
                }

                // NOTE: HttpTimeout removed due to compatibility issues
                // Timeouts are configured at OkHttp level above
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create unsafe Ktor client - falling back to basic client", e)

            // Fallback to basic HttpClient if SSL bypass fails
            HttpClient(OkHttp) {
                install(DefaultRequest) {
                    headers.append("X-Emby-Client", "JellyfinRyan")
                    headers.append("User-Agent", "JellyfinRyan/1.0")
                }
            }
        }
    }

    /**
     * Create unsafe OkHttpClient for direct HTTP calls
     */
    fun createUnsafeOkHttpClient(): okhttp3.OkHttpClient {
        return UnsafeOkHttpClient.getUnsafeOkHttpClient()
    }
}