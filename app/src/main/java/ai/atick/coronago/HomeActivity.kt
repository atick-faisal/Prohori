package ai.atick.coronago

import android.app.Activity
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_home.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class HomeActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mapReady = false
    private lateinit var map: GoogleMap
    private lateinit var database: AppDatabase
    private lateinit var workActivity: WorkActivity
    private val key: Key = Key()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        database = AppDatabase(this)
        workActivity = WorkActivity(this)
        //////////////////////////////////////////////////
        supportActionBar?.hide()
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
                map.addMarker(
                    MarkerOptions()
                        .position(currentLocation)
                        .title(database.getString("name"))
                        .icon(BitmapDescriptorFactory.defaultMarker())
                        .draggable(false)
                        .visible(true))
                locationText.text = cityName
            }
    }
}
