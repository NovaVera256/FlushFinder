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

data class LocationWriteResponse(
    val success: Boolean = false,
    val error: String? = null
)

object LocationsApi {

    private const val BASE_URL = "https://flushfinder-5eq8.onrender.com/api/locations"
    private val mainHandler = Handler(Looper.getMainLooper())

    fun createLocation(
        latitude: Double,
        longitude: Double,
        changingStation: Boolean,
        airDryer: Boolean,
        paperTowels: Boolean,
        wheelchair: Boolean,
        token: String? = null,
        callback: (LocationWriteResponse) -> Unit
    ) {

        val body = JSONObject().apply {
            put("latitude", latitude)
            put("longitude", longitude)
            put("changing_station", changingStation)
            put("air_dryer", airDryer)
            put("paper_towels", paperTowels)
            put("wheelchair", wheelchair)
        }

        sendJsonRequest(
            endpoint = "$BASE_URL/new",
            method = "POST",
            body = body,
            token = token,
            callback = callback
        )
    }

    fun updateLocation(
        bathroomId: Int,
        latitude: Double? = null,
        longitude: Double? = null,
        changingStation: Boolean? = null,
        airDryer: Boolean? = null,
        paperTowels: Boolean? = null,
        wheelchair: Boolean? = null,
        token: String? = null,
        callback: (LocationWriteResponse) -> Unit
    ) {
        val body = JSONObject().apply {
            latitude?.let { put("latitude", it) }
            longitude?.let { put("longitude", it) }
            changingStation?.let { put("changing_station", it) }
            airDryer?.let { put("air_dryer", it) }
            paperTowels?.let { put("paper_towels", it) }
            wheelchair?.let { put("wheelchair", it) }
        }

        sendJsonRequest(
            endpoint = "$BASE_URL/$bathroomId",
            method = "PUT",
            body = body,
            token = token,
            callback = callback
        )
    }

    private fun sendJsonRequest(
        endpoint: String,
        method: String,body: JSONObject,
        token: String?,
        callback: (LocationWriteResponse) -> Unit
    ) {
        thread {
            try {
                val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                    requestMethod = method
                    doInput = true
                    doOutput = true
                    useCaches = false
                    connectTimeout = 15000
                    readTimeout = 15000
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")

                    if (!token.isNullOrBlank()) {
                        setRequestProperty("Authorization", "Bearer $token")
                    }
                }

                OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(body.toString())
                    writer.flush()
                }

                val responseText = readResponse(connection)
                val json = parseJsonOrError(responseText, connection.responseCode)

                connection.disconnect()

                val result = when {
                    json.has("error") -> LocationWriteResponse(error = json.getString("error"))
                    json.optString("result") == "success" -> LocationWriteResponse(success = true)
                    connection.responseCode in 200..299 -> LocationWriteResponse(success = true)
                    else -> LocationWriteResponse(error = "Unexpected server response.")
                }

                mainHandler.post {
                    callback(result)
                }
            } catch (e: Exception) {
                mainHandler.post {
                    callback(LocationWriteResponse(error = e.message ?: "Location request failed."))
                }
            }
        }
    }

    private fun readResponse(connection: HttpURLConnection): String {
        val stream = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: connection.inputStream
        }

        return BufferedReader(InputStreamReader(stream)).use { reader ->
            buildString {
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    append(line)
                }
            }
        }
    }

    private fun parseJsonOrError(responseText: String, responseCode: Int): JSONObject {
        return if (responseText.trim().startsWith("{")) {
            JSONObject(responseText)
        } else {
            JSONObject().put("error", "Server returned non-JSON response: $responseCode")
        }
    }
}