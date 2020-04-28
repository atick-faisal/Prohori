package ai.atick.coronago

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_home.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

class HomeActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mapReady = false
    private lateinit var map: GoogleMap
    private lateinit var database: AppDatabase
    private lateinit var workActivity: WorkActivity
    private lateinit var locationActivity: LocationActivity
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var circle: Circle? = null
    private var marker: Marker? = null
    private val key: Key = Key()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        database = AppDatabase(this)
        workActivity = WorkActivity(this)
        locationActivity = LocationActivity(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    updateGoogleMap(location)
                }
            }
        }

        //////////////////////////////////////////////////
        supportActionBar?.elevation = 0f
        val name = database.getString("name")
        if (name != "") supportActionBar?.title = name
        else supportActionBar?.title = getString(R.string.no_mane)
        //////////////////////////////////////////////////
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(key.mapKey)
        }
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)
        getData(key.userUrl + "/" + database.getString("phoneNumber"))
        workActivity.createPeriodicTasks()
    }

    //////////////////////////////////////////////////////////////////
    private fun getData(url: String) {
        val queue = Volley.newRequestQueue(this)
        val request = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                try {
                    val dataObject = JSONObject(response)
                    val userArray = dataObject.getJSONArray("user")
                    val userData = userArray.getJSONObject(0)
                    val riskFactor = userData.getDouble("riskFactor")
                    riskText.text = riskFactor.toString()
                    YoYo.with(Techniques.Tada)
                        .duration(700)
                        .playOn(riskText)
                } catch (e: JSONException) {
                }
                Log.d("corona", response.toString())
            },
            Response.ErrorListener { error ->
                Log.d("corona", error.toString())
                var message = "Something Went Wrong"
                when (error) {
                    is NetworkError -> message = "Please Turn on Internet"
                    is ServerError -> message = "Server not Found"
                    is AuthFailureError -> message = "Authentication Failed"
                    is ParseError -> message = "Parsing Error"
                    is NoConnectionError -> message = "No Connection"
                    is TimeoutError -> message = "Request Timed Out"
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        )
        queue.add(request)
    }

    ///////////////////////////////////////////////////////////
    fun requestLocationUpdate(@Suppress("UNUSED_PARAMETER") v: View) {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var isLocationEnabled = false
        try { isLocationEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)}
        catch (e: Exception) {}
        if (isLocationEnabled) {
            fabButton.visibility = View.GONE
            YoYo.with(Techniques.RollOut).duration(500).playOn(fabButton)
            updatingLocation.visibility = View.VISIBLE
            YoYo.with(Techniques.BounceInUp).duration(700).playOn(updatingLocation)
            ////////////////////////////////////////////////////////////////////////////////////
            Handler().postDelayed({
                updatingLocation.visibility = View.GONE
                YoYo.with(Techniques.DropOut).duration(500).playOn(updatingLocation)
                fabButton.visibility = View.VISIBLE
                YoYo.with(Techniques.RollIn).duration(700).playOn(fabButton)
            }, 4000)
            ////////////////////////////////////////////////////////////////////////////////////
            val locationRequest = LocationRequest.create()?.apply {
                interval = 10000
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            //////////////////////////////////////////////////////
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            Toast.makeText(this, "Please Enable Location", Toast.LENGTH_LONG).show()
        }
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
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        map.setMinZoomPreference(14F)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            updateGoogleMap(location)
        }
    }

    ///////////////////////////////////////////////////////////
    private fun updateGoogleMap(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        val geo = Geocoder(this, Locale.US)
        val addresses = geo.getFromLocation(latitude, longitude, 1)
        val cityName = addresses[0].getAddressLine(0)
        val currentLocation = LatLng(latitude, longitude)
        map.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))
        circle?.remove()
        marker?.remove()
        circle = map.addCircle(
            CircleOptions()
                .center(LatLng(latitude, longitude))
                .radius(location.accuracy.toDouble())
                .fillColor(ContextCompat.getColor(this, R.color.map_circle))
                .strokeColor(ContextCompat.getColor(this, R.color.colorAccent))
                .strokeWidth(5.0f)
        )
        marker = map.addMarker(
            MarkerOptions()
                .position(currentLocation)
                .title(database.getString("name"))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .draggable(false)
                .visible(true)
        )
        locationText.text = cityName
    }

    ////////////////////////////////////////////////////////////
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.home_menu, menu)
        return true
    }

    //////////////////////////////////////////////////////////////////
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logoutButton -> {
                database.putBoolean("registered", false)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                true
            }
            R.id.uploadButton -> {
                locationActivity.updateLocation()
                val uploadWorkRequest = OneTimeWorkRequestBuilder<UploadWork>()
                    .setInitialDelay(2, TimeUnit.SECONDS)
                    .build()
                WorkManager.getInstance(this).enqueue(uploadWorkRequest)
                Toast.makeText(this, "Uploading...", Toast.LENGTH_LONG).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
