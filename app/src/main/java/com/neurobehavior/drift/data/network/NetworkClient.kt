package com.neurobehavior.drift.data.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkClient @Inject constructor() {
    private val TAG = "NetworkClient"
    private val BASE_URL = "http://10.0.2.2:5000"

    suspend fun post(endpoint: String, payload: JSONObject, token: String? = null): NetworkResult = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val url = URL("$BASE_URL$endpoint")
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")
            if (token != null) {
                conn.setRequestProperty("Authorization", "Bearer $token")
            }
            conn.doOutput = true
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(payload.toString())
            writer.flush()
            writer.close()

            val code = conn.responseCode
            val isSuccess = code in 200..299
            val stream = if (isSuccess) conn.inputStream else conn.errorStream
            if (stream == null) {
                return@withContext NetworkResult.Failure(code, "No response stream")
            }

            val reader = BufferedReader(InputStreamReader(stream))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            reader.close()

            val responseString = sb.toString()
            if (isSuccess) {
                NetworkResult.Success(JSONObject(responseString))
            } else {
                val errorMsg = try {
                    JSONObject(responseString).optString("error", "HTTP error $code")
                } catch (e: Exception) {
                    "HTTP error $code"
                }
                NetworkResult.Failure(code, errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "POST error at $endpoint", e)
            NetworkResult.Failure(-1, e.message ?: "Unknown network error")
        } finally {
            conn?.disconnect()
        }
    }

    suspend fun get(endpoint: String, token: String?): NetworkResult = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val url = URL("$BASE_URL$endpoint")
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/json")
            if (token != null) {
                conn.setRequestProperty("Authorization", "Bearer $token")
            }
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            val code = conn.responseCode
            val isSuccess = code in 200..299
            val stream = if (isSuccess) conn.inputStream else conn.errorStream
            if (stream == null) {
                return@withContext NetworkResult.Failure(code, "No response stream")
            }

            val reader = BufferedReader(InputStreamReader(stream))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            reader.close()

            val responseString = sb.toString()
            if (isSuccess) {
                NetworkResult.Success(JSONObject(responseString))
            } else {
                val errorMsg = try {
                    JSONObject(responseString).optString("error", "HTTP error $code")
                } catch (e: Exception) {
                    "HTTP error $code"
                }
                NetworkResult.Failure(code, errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "GET error at $endpoint", e)
            NetworkResult.Failure(-1, e.message ?: "Unknown network error")
        } finally {
            conn?.disconnect()
        }
    }
}

sealed class NetworkResult {
    data class Success(val data: JSONObject) : NetworkResult()
    data class Failure(val code: Int, val message: String) : NetworkResult()
}
