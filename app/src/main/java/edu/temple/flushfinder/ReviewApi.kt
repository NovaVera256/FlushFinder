package edu.temple.flushfinder

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

data class BathroomReview(
    val rating: Int,
    val text: String?,
    val createdAt: String?,
    val username: String?
)

data class ReviewsResponse(
    val reviews: List<BathroomReview> = emptyList(),
    val success: Boolean = false,
    val error: String? = null
)

object ReviewApi {
    private const val BASE_URL = "https://flushfinder-5eq8.onrender.com/api/locations"
    private val mainHandler = Handler(Looper.getMainLooper())

    fun getReviews(
        bathroomId: Int,
        token: String? = null,
        callback: (ReviewsResponse) -> Unit
    ) {
        thread {
            try {
                val connection = (URL("$BASE_URL/$bathroomId/reviews").openConnection() as HttpURLConnection).apply {
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

                val responseText = readResponse(connection)
                val json = parseJsonOrError(responseText, connection.responseCode)

                connection.disconnect()

                if (json.has("error")) {
                    mainHandler.post {
                        callback(ReviewsResponse(error = json.getString("error")))
                    }

                    return@thread
                }

                val resultsArray = json.optJSONArray("results") ?: JSONArray()
                val reviews = mutableListOf<BathroomReview>()

                for (i in 0 until resultsArray.length()) {
                    val item = resultsArray.getJSONObject(i)

                    reviews.add(BathroomReview(rating = item.optInt(
                        "rating", 0),
                        text = if (item.isNull("text")) null else item.optString("text"),
                        createdAt = if (item.isNull("created_at")) null else item.optString("created_at"),
                        username = if (item.isNull("username")) null else item.optString("username")
                    )
                    )
                }

                mainHandler.post {
                    callback(
                        ReviewsResponse(
                            reviews = reviews,
                            success = true)
                    )
                }
            } catch (e: Exception) {
                mainHandler.post {
                    callback(ReviewsResponse(error = e.message ?: "Could not load reviews."))
                }
            }
        }
    }

    fun createReview(
        bathroomId: Int,
        rating: Int,
        text: String?,
        token: String? = null,
        callback: (ReviewsResponse) -> Unit
    ) {
        thread {
            try {
                val body = JSONObject().apply {
                    put("rating", rating)
                    if (text.isNullOrBlank()) {
                        put("text", JSONObject.NULL)
                    } else {
                        put("text", text)
                    }
                }

                val connection = (URL("$BASE_URL/$bathroomId/reviews/new").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
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
                    json.has("error") -> ReviewsResponse(error = json.getString("error"))
                    json.optString("result") == "success" -> ReviewsResponse(success = true)
                    connection.responseCode in 200..299 -> ReviewsResponse(success = true)
                    else -> ReviewsResponse(error = "Unexpected server response.")
                }

                mainHandler.post {
                    callback(result)
                }
            } catch (e: Exception) {
                mainHandler.post {
                    callback(ReviewsResponse(error = e.message ?: "Could not create review."))
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