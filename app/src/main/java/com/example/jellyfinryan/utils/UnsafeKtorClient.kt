package com.example.jellyfinryan.utils

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import okhttp3.OkHttpClient
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object UnsafeKtorClient {
      /**
     * Creates a Ktor HttpClient that accepts all SSL certificates
     * This is needed for Jellyfin servers with self-signed certificates
     */
    fun createUnsafeKtorClient(): HttpClient {
        return try {
            Log.d("UnsafeKtorClient", "Creating unsafe Ktor client for SSL bypass")
            
            // Create a trust manager that accepts all certificates
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                    // Accept all certificates
                }

                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                    // Accept all certificates  
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            })

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())

            // Create HttpClient with OkHttp engine and SSL bypass
            HttpClient(OkHttp) {
                engine {
                    config {
                        sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                        hostnameVerifier { _, _ -> true }
                    }
                }
                  // Add default headers for Emby compatibility (reverse proxy support)
                install(DefaultRequest) {
                    headers.append("X-Emby-Client", "JellyfinRyan")
                    headers.append("X-Emby-Client-Version", "1.0")
                    headers.append("X-Emby-Device", "AndroidTV")
                    headers.append("X-Emby-Device-Id", "jellyfin-ryan-android-tv")
                    headers.append("X-Emby-Device-Name", "JellyfinRyan Android TV")
                }
            }
        } catch (e: Exception) {
            Log.e("UnsafeKtorClient", "Failed to create unsafe Ktor client", e)
            throw RuntimeException("Failed to create unsafe Ktor client", e)
        }
    }
}
