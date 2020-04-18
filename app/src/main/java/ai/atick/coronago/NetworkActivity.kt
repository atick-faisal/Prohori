package ai.atick.coronago

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class NetworkActivity(private val context: Context) {

    private val database: AppDatabase = AppDatabase(context)
    private lateinit var userId: String

    fun userDataObject(
        phone: String,
        gender: String,
        birthDate: String
    ): JSONObject {
        val dataObject = JSONObject()
        dataObject.put("phone", phone)
        dataObject.put("gender", gender)
        dataObject.put("birthDate", birthDate)
        Log.d("corona", dataObject.toString())
        return dataObject
    }

    fun locationDataObject(
        latitude: String,
        longitude: String,
        timeStamp: String
    ): JSONObject {
        val dataObject = JSONObject()
        userId = database.getString("userId")
        dataObject.put("userId", userId)
        dataObject.put("latitude", latitude)
        dataObject.put("longitude", longitude)
        dataObject.put("timeStamp", timeStamp)
        Log.d("corona", dataObject.toString())
        return dataObject
    }

    fun postData(url: String, data: JSONObject) {
        val queue = Volley.newRequestQueue(context)
        val request = JsonObjectRequest(
            Request.Method.POST, url, data,
            Response.Listener<JSONObject> { response ->
                try {
                    val userId = response.getInt("id").toString()
                    database.putString("userId", userId)
                } catch (e: JSONException) {
                    Log.d("corona", e.toString())
                }
                Log.d("corona", response.toString())
            },
            Response.ErrorListener { error ->
                Log.d("corona", error.toString())
            }
        )
        queue.add(request)
    }
}