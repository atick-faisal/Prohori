package ai.atick.coronago

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    //////////////////////////////////////////////////////////
    private lateinit var networkActivity: NetworkActivity
    private lateinit var locationActivity: LocationActivity
    private lateinit var testActivity: TestActivity
    private lateinit var database: AppDatabase
    private lateinit var workActivity: WorkActivity
    //////////////////////////////////////////////////////////
    private val key: Key = Key()
    //////////////////////////////////////////////////////////
    private var registered = false
    private var mapReady = false
    private lateinit var map: GoogleMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //////////////////////////////////////////////////
        networkActivity = NetworkActivity(this)
        locationActivity = LocationActivity(this)
        testActivity = TestActivity(this)
        workActivity = WorkActivity(this)
        database = AppDatabase(this)
        //////////////////////////////////////////////////
        supportActionBar?.hide()
        //////////////////////////////////////////////////
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(key.mapKey)
        }
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)
        ///////////////////////////////////////////////////////////
        registered = database.getBoolean("registered")
        if (registered) {
            registrationForm.visibility = View.GONE
            dashboard.visibility = View.VISIBLE
            if (database.getString("name") != "") userName.text = database.getString("name").substring(0, 1)
            networkActivity.getData(key.userUrl + "/${database.getString("phoneNumber")}")
        } else {
            registrationForm.visibility = View.VISIBLE
            dashboard.visibility = View.GONE
        }
        /////////////////////////////////////////////////////////////////////////////
        registerButton.setOnClickListener {
            key.name = nameText.text.toString()
            key.birthDate = birthdayText.text.toString()
            key.phoneNumber = phoneText.text.toString()
            database.putString("name", key.name)
            database.putString("gender", key.gender)
            database.putString("phoneNumber", key.phoneNumber)
            database.putString("birthDate", key.birthDate)
            val userData = networkActivity.userDataObject(
                phoneNumber = key.phoneNumber,
                gender = key.gender,
                birthDate = key.birthDate
            )
            networkActivity.createUser(key.userUrl, userData)
            workActivity.createPeriodicTasks()
            if (database.getString("name") != "") userName.text = database.getString("name").substring(0, 1)
            Log.d("corona", "New: ${key.birthDate}")
        }
        ////////////////////////////////////////////////////////////////////////////////////////////
        requestPermissions()
        createNotificationChannel(key.locationChannelId, "Location Channel")
        createNotificationChannel(key.uploadChannelId, "Upload Channel")
    }
    ///////////////////////////////////////////////////////////
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(key.mapKey)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(key.mapKey, mapViewBundle)
        }
        mapView.onSaveInstanceState(mapViewBundle)
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
    //////////////////////////////////////////////////////////////////////////////
    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED -> {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        0
                    )
                }
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED -> {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        0
                    )
                }
                checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED -> {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.INTERNET),
                        0
                    )
                }
                checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED -> {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_NETWORK_STATE),
                        0
                    )
                }
            }
        }
    }
    ////////////////////////////////////////////////////////////////////
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == 0) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                refreshMap()
                //requestPermissions()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Please allow permissions...",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissions()
            }
        }
    }
    ///////////////////////////////////////
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }
    ///////////////////////////////////////
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }
    ///////////////////////////////////////
    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }
    ///////////////////////////////////////
    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }
    ///////////////////////////////////////
    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }
    ///////////////////////////////////////
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
    ///////////////////////////////////////
    override fun onMapReady(googleMap: GoogleMap) {
        mapReady = true
        map = googleMap
        refreshMap()
    }
    ///////////////////////////////////////////////////////////
    private fun refreshMap() {
        map.setMinZoomPreference(12F)
        LocationServices
            .getFusedLocationProviderClient(this)
            .lastLocation
            .addOnSuccessListener { location ->
                val latitude = location.latitude
                val longitude = location.longitude
                val geo = Geocoder(this, Locale.US)
                val addresses = geo.getFromLocation(latitude, longitude, 1)
                val cityName = addresses[0].getAddressLine(0)
                val currentLocation = LatLng(latitude, longitude)
                map.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))
                map.addMarker(MarkerOptions()
                    .position(currentLocation)
                    .title(database.getString("name"))
                    .icon(BitmapDescriptorFactory.defaultMarker())
                    .draggable(false)
                    .visible(true))
                locationText.text = cityName
            }
    }
}
