package edu.temple.flushfinder

import android.net.Uri
import android.os.Handler
import android.os.Looper
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
import androidx.core.net.toUri

data class SearchResponse(
    val results: List<BathroomLocation> = emptyList(),
    val error: String? = null
)

object SearchApi {
    private const val SEARCH_URL = "https://flushfinder-5eq8.onrender.com/api/locations/search"
    val mainHandler = Handler(Looper.getMainLooper())

    fun searchLocations(
        number: Int,
        latitude: Double,
        longitude: Double,
        changingStation: Boolean,
        airDryer: Boolean,
        paperTowels: Boolean,
        wheelchair: Boolean,
        token: String? = null,
        callback: (SearchResponse) -> Unit
    ) {
        thread {
            try {

                val connection = (URL(SEARCH_URL.toUri()
                    .buildUpon()
                    .appendQueryParameter("number", number.toString())
                    .appendQueryParameter("latitude", latitude.toString())
                    .appendQueryParameter("longitude", longitude.toString())
                    .appendQueryParameter("changing_station", changingStation.toString())
                    .appendQueryParameter("air_dryer", airDryer.toString())
                    .appendQueryParameter("paper_towels", paperTowels.toString())
                    .appendQueryParameter("wheelchair", wheelchair.toString())
                    .toString()
                ).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    doInput = true
                    useCaches = false
                    connectTimeout = 15000
                    readTimeout = 15000
                    setRequestProperty("Accept", "application/json")
                    if (!token.isNullOrBlank()) {
                        setRequestProperty("Authorization", "Bearer $token")
                    }
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

                val responseJson = JSONObject(responseText)

                if (responseJson.has("error")) {
                    postResult(callback, SearchResponse(error = responseJson.getString("error")))
                    connection.disconnect()
                    return@thread
                }

                val resultsArray = responseJson.optJSONArray("results") ?: JSONArray()
                val results = mutableListOf<BathroomLocation>()

                for (i in 0 until resultsArray.length()) {
                    val item = resultsArray.getJSONObject(i)
                    results.add(
                        BathroomLocation(
                            bathroomId = item.optInt("bathroom_id", -1),
                            latitude = item.getDouble("latitude"),
                            longitude = item.getDouble("longitude"),
                            name = item.getString("name"),
                            rating = if (item.has("rating")) item.optDouble("rating") else null,
                            changingStation = if (item.has("changing_station")) item.optBoolean("changing_station") else null,
                            airDryer = if (item.has("air_dryer")) item.optBoolean("air_dryer") else null,
                            paperTowels = if (item.has("paper_towels")) item.optBoolean("paper_towels") else null,
                            wheelchair = if (item.has("wheelchair")) item.optBoolean("wheelchair") else null,
                        )
                    )
                }

                connection.disconnect()
                postResult(callback, SearchResponse(results = results))
            } catch (e: Exception) {
                postResult(
                    callback,
                    SearchResponse(error = e.message ?: "Search request failed.")
                )
            }
        }
    }

    private fun postResult(callback: (SearchResponse) -> Unit, result: SearchResponse) {
        mainHandler.post {
            callback(result)
        }
    }
}