package edu.temple.flushfinder

import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

data class UserReview (
    val attachedUser: Int,
    val rating: Float,
    val text: String
)

data class ReviewResponse (
    val results: List<UserReview> = emptyList(),
    val error: String? = null
)

object ReviewApi {
    private const val REVIEW_URL = "https://flushfinder-5eq8.onrender.com/api/locations/"

    fun getReviews (
        bathroom_id: Int,
        token: String? = null,
        callback: (ReviewResponse) -> Unit
    ) {
        thread {
            try{
                val connection = (URL(REVIEW_URL + bathroom_id.toString()).openConnection() as HttpURLConnection).apply{
                    requestMethod = "GET"
                    doInput = true
                    useCaches = false
                    connectTimeout = 15000
                    readTimeout = 15000
                    setRequestProperty("Accept", "application/json")
                    if(!token.isNullOrBlank()) {
                        setRequestProperty("Authorize", "Bearer $token")
                    }
                }


            } catch (e: Exception) {
//                postResult(
//                    callback,
//                    ReviewResponse(error = e.message ?: "Search request failed.")
//                )
            }
        }
    }
}