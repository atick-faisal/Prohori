package ai.atick.coronago

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class NetworkActivity(private val context: Context) {

    private val database: AppDatabase = AppDatabase(context)
    private var uploadSuccessful = false

    fun userDataObject(
        phoneNumber: String,
        gender: String,
        birthDate: String
    ): JSONObject {
        val dataObject = JSONObject()
        dataObject.put("phoneNumber", phoneNumber)
        dataObject.put("gender", gender)
        dataObject.put("birthDate", birthDate)
        Log.d("corona", dataObject.toString())
        return dataObject
    }
    /////////////////////////////////////////////////////////
    fun locationObject(
        latitude: String,
        longitude: String,
        timeStamp: String
    ): JSONObject {
        val dataObject = JSONObject()
        dataObject.put("latitude", latitude)
        dataObject.put("longitude", longitude)
        dataObject.put("timeStamp", timeStamp)
        return dataObject
    }
    /////////////////////////////////////////////////////////
    fun locationDataObject(
        phoneNumber: String,
        locationArray: JSONArray
    ): JSONObject {
        val locationDataObject = JSONObject()
        locationDataObject.put("phoneNumber", phoneNumber)
        locationDataObject.put("locationData", locationArray)
        return locationDataObject
    }

    //////////////////////////////////////////////////////////////////
    fun postDataBackground(url: String, data: JSONObject) {
        val queue = Volley.newRequestQueue(context)
        val request = JsonObjectRequest(
            Request.Method.POST, url, data,
            Response.Listener<JSONObject> { response ->
                try { uploadSuccessful = response.getBoolean("success") }
                catch (e: JSONException) {}
                if (uploadSuccessful) cleanDatabase()
                Log.d("corona", response.toString())
            },
            Response.ErrorListener { error ->
                Log.d("corona", error.toString())
            }
        )
        queue.add(request)
    }

    ///////////////////////////////////////////////////////////
    private fun cleanDatabase() {
        database.remove("latitudeList")
        database.remove("longitudeList")
        database.remove("timestampList")
    }
}