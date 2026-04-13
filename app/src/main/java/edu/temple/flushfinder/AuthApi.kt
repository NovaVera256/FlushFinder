package edu.temple.flushfinder

import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

data class AuthResult(
    val token: String? = null,
    val error: String? = null
)

object AuthApi {
    private const val BASE_URL = "https://flushfinder-5eq8.onrender.com/api/auth"
    private val mainHandler = Handler(Looper.getMainLooper())

    fun login(username: String, password: String, callback: (AuthResult) -> Unit) {
        postAuthRequest("$BASE_URL/login", username, password, callback)
    }

    fun register(username: String, password: String, callback: (AuthResult) -> Unit) {
        postAuthRequest("$BASE_URL/register", username, password, callback)
    }

    private fun postAuthRequest(
        endpoint: String,
        username: String,
        password: String,
        callback: (AuthResult) -> Unit
    ) {
        thread {
            try {

                val url = URL(endpoint)

                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doInput = true
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = 10000
                    readTimeout = 10000
                }

                val body = JSONObject().apply {
                    put("username", username)
                    put("password", password)
                }

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(body.toString())
                    writer.flush()
                }

                val stream = if (connection.responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream ?: connection.inputStream
                }

                val responseText = BufferedReader(InputStreamReader(stream)).use { reader ->
                    buildString {
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            append(line)
                        }
                    }
                }

                val json = JSONObject(responseText)

                val result = when {
                    json.has("token") -> AuthResult(token = json.getString("token"))
                    json.has("error") -> AuthResult(error = json.getString("error"))
                    else -> AuthResult(error = "Unexpected server response.")
                }

                connection.disconnect()

                mainHandler.post {
                    callback(result)
                }
            } catch (e: Exception) {
                mainHandler.post {
                    callback(AuthResult(error = e.message ?: "Network request failed."))
                }
            }
        }
    }
}