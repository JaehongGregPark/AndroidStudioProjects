package com.example.worldradio.network

/**
 * Radio Browser 서버 목록 관리
 *
 * 서버 장애 또는 지연 시 자동 전환
 */


import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object ServerManager {

    private val servers = listOf(
        "https://all.api.radio-browser.info/json/",
        "https://nl1.api.radio-browser.info/json/",
        "https://de1.api.radio-browser.info/json/"
    )

    suspend fun getFastestServer(
        context: Context
    ): String {

        val prefs =
            context.getSharedPreferences(
                "server_pref",
                Context.MODE_PRIVATE
            )

        val saved = prefs.getString("fastest", null)

        if (saved != null) return saved

        var fastestServer = servers.first()
        var fastestTime = Long.MAX_VALUE

        withContext(Dispatchers.IO) {

            for (server in servers) {

                try {

                    val client =
                        OkHttpClient.Builder()
                            .connectTimeout(3, TimeUnit.SECONDS)
                            .build()

                    val request =
                        Request.Builder()
                            .url(server)
                            .build()

                    val start = System.currentTimeMillis()

                    client.newCall(request)
                        .execute()
                        .close()

                    val time =
                        System.currentTimeMillis() - start

                    if (time < fastestTime) {
                        fastestTime = time
                        fastestServer = server
                    }

                } catch (_: Exception) {}
            }
        }

        prefs.edit()
            .putString("fastest", fastestServer)
            .apply()

        return fastestServer
    }


    /**
     * 서버 목록 반환
     */
    fun getServers(): List<String> {
        return servers
    }
}