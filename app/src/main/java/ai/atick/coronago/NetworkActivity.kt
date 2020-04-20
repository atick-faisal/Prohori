package ai.atick.coronago

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class NetworkActivity(private val context: Context) {

    private val database: AppDatabase = AppDatabase(context)
    private val key: Key = Key()
    /////////////////////////////////////////////////////////
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
    fun getData(url: String) {
        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                try {
                    val dataObject = JSONObject(response)
                    val userArray = dataObject.getJSONArray("user")
                    val userData = userArray.getJSONObject(0)
                    val riskFactor = userData.getDouble("riskFactor")
                    val mainActivity = context as Activity
                    mainActivity.riskText.text = riskFactor.toString()
                } catch (e: JSONException) {
                }
                Log.d("corona", response.toString())
            },
            Response.ErrorListener { error ->
                Log.d("corona", error.toString())
            }
        )
        queue.add(request)
    }
    //////////////////////////////////////////////////////////////////
    fun postDataBackground(url: String, data: JSONObject) {
        val queue = Volley.newRequestQueue(context)
        val request = JsonObjectRequest(
            Request.Method.POST, url, data,
            Response.Listener<JSONObject> { response ->
                cleanDatabase()
                Log.d("corona", response.toString())
            },
            Response.ErrorListener { error ->
                Log.d("corona", error.toString())
            }
        )
        queue.add(request)
    }
    //////////////////////////////////////////////////////////
    fun createUser(url: String, data: JSONObject) {
        val queue = Volley.newRequestQueue(context)
        val request = JsonObjectRequest(
            Request.Method.POST, url, data,
            Response.Listener<JSONObject> { response ->
                database.putBoolean("registered", true)
                val mainActivity = context as Activity
                mainActivity.registrationForm.visibility = View.GONE
                mainActivity.dashboard.visibility = View.VISIBLE
                getData(key.userUrl + "/${database.getString("phoneNumber")}")
                try {
                    val registered = response.getBoolean("success")
                    if (registered) {
                        Toast.makeText(
                            context,
                            "Registration Complete",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Already Registered",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: JSONException) {
                }
                cleanDatabase()
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