package ai.atick.coronago

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private lateinit var networkActivity: NetworkActivity
    private lateinit var database: AppDatabase
    private val key: Key = Key()
    private var registered = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        //////////////////////////////////////////////////
        networkActivity = NetworkActivity(this)
        database = AppDatabase(this)
        registered = database.getBoolean("registered")
//        if (registered) {
//            startActivity(Intent(this, HomeActivity::class.java))
//            finish()
//        }
//        else setContentView(R.layout.activity_main)
        setContentView(R.layout.activity_main)
        nameText.setText(database.getString("name"))
        phoneText.setText(database.getString("phoneNumber"))
        birthdayText.setText(database.getString("birthDate"))
        genderSelector.onItemSelectedListener = SpinnerListener(this)
        ////////////////////////////////////////////////////////////////////////////
        askForPermissions()
        createNotificationChannel(key.locationChannelId, "Location Channel")
        createNotificationChannel(key.uploadChannelId, "Upload Channel")
    }

    fun registerUser(@Suppress("UNUSED_PARAMETER") v: View) {
        if (!isAnyFieldEmpty()) {
            saveUserData()
            val userData = networkActivity.userDataObject(
                phoneNumber = database.getString("phoneNumber"),
                gender = database.getString("gender"),
                birthDate = database.getString("birthDate")
            )
            createNewUser(userData)
        }
    }

    ///////////////////////////////////////////////////////////////////////////////
    private fun saveUserData() {
        database.putString("name", nameText.text.toString())
        database.putString("phoneNumber", phoneText.text.toString())
        database.putString("birthDate", birthdayText.text.toString())
    }

    ////////////////////////////////////////////////////////////////////////////////
    private fun isAnyFieldEmpty(): Boolean {
        val warning = getString(R.string.field_required)
        when {
            TextUtils.isEmpty(nameText.text.toString()) -> {
                nameText.error = warning
                return true
            }
            TextUtils.isEmpty(phoneText.text.toString()) -> {
                phoneText.error = warning
                return true
            }
            TextUtils.isEmpty(birthdayText.text.toString()) -> {
                birthdayText.error = warning
                return true
            }
            else -> {
                return false
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////
    private fun createNewUser(data: JSONObject) {
        val queue = Volley.newRequestQueue(this)
        val request = JsonObjectRequest(
            Request.Method.POST, key.userUrl, data,
            Response.Listener<JSONObject> { response ->
                database.putBoolean("registered", true)
                try {
                    val registered = response.getBoolean("success")
                    if (registered) Toast.makeText(this, "Registration Complete", Toast.LENGTH_LONG).show()
                } catch (e: JSONException) {
                }
                Log.d("corona", response.toString())
                startActivity(Intent(this, HomeActivity::class.java))
            },
            Response.ErrorListener { error ->
                Log.d("corona", error.toString())
            }
        )
        queue.add(request)
    }

    ////////////////////////////////////////////////////////////////////////////////
    private fun createNotificationChannel(id: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(id, channelName, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    ////////////////////////////////////////////////////////////////
    fun showDatePickerDialog(v: View) {
        Log.d("corona", "View: $v")
        val newFragment = DatePickerFragment()
        newFragment.show(supportFragmentManager, "datePicker")
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private fun askForPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) permissions.add(
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
        val permissionsToRequest = permissionsToRequest(permissions)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size > 0) {
                Toast.makeText(this, "Please allow permissions...", Toast.LENGTH_LONG).show()
                requestPermissions(permissionsToRequest.toTypedArray(), key.permissionKey)
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private fun permissionsToRequest(allPermissions: MutableList<String>): MutableList<String> {
        val result = mutableListOf<String>()
        for (permission in allPermissions) {
            if (!hasPermission(permission)) result.add(permission)
        }
        return result
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private fun hasPermission(permission: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        ) return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        return true
    }
}