package ai.atick.coronago

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private var userUrl: String = "http://home.jamiussiam.com:8090/user"

    // --------- Dummy Data --------- //
    private var phone = "01711010101"
    private var gender = "OTHER"
    private var birthDate = "01-01-1969"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sendButton.setOnClickListener {
            val userData = userDataObject(
                phone = phone,
                gender = gender,
                birthDate = birthDate
            )
            postData(
                url = userUrl,
                data = userData
            )
        }
    }

    private fun postData(url: String, data: JSONObject) {
        val queue = Volley.newRequestQueue(this)
        val request = JsonObjectRequest(Request.Method.POST, url, data,
            Response.Listener<JSONObject> { response ->
                Log.d("corona", response.toString())
            },
            Response.ErrorListener {error ->
                Log.d("corona", error.toString())
            }
        )
        queue.add(request)
    }

    private fun userDataObject(phone: String, gender: String, birthDate: String): JSONObject {
        val dataObject = JSONObject()
        dataObject.put("phone", phone)
        dataObject.put("gender", gender)
        dataObject.put("birthDate", birthDate)
        Log.d("corona", dataObject.toString())
        return dataObject
    }
}
