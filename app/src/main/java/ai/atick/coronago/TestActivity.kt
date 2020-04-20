package ai.atick.coronago

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.LocationServices
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

////////////////////////////////////////////////////////////////////////////////////////////////////
// ---------------------- This Activity is for testing purposes ----------------------------------//
////////////////////////////////////////////////////////////////////////////////////////////////////

class TestActivity(private val context: Context) {

    private val database: AppDatabase = AppDatabase(context)
    private val networkActivity: NetworkActivity = NetworkActivity(context)
    private var locationUrl: String = "https://covid-callfornation.herokuapp.com/location"

    fun updateLocation() {
        LocationServices
            .getFusedLocationProviderClient(context)
            .lastLocation
            .addOnSuccessListener { location ->
                val latitudeList = database.getListString("latitudeList")
                val longitudeList = database.getListString("longitudeList")
                val timestampList = database.getListString("timestampList")

                latitudeList.add(location.latitude.toString())
                longitudeList.add(location.longitude.toString())
                timestampList.add(getTimeStamp())

                database.putListString("latitudeList", latitudeList)
                database.putListString("longitudeList", longitudeList)
                database.putListString("timestampList", timestampList)

                Toast.makeText(
                    context,
                    "${timestampList.size} Location Added",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun getTimeStamp(): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm", Locale.US)
        val date = Calendar.getInstance().time
        return dateFormat.format(date)
    }

    fun uploadLocation() {
        val phoneNumber = database.getString("phoneNumber")
        val latitudeList = database.getListString("latitudeList")
        val longitudeList = database.getListString("longitudeList")
        val timestampList = database.getListString("timestampList")

        Log.d("corona", timestampList.size.toString())

        val locationArray = JSONArray()
        timestampList.forEachIndexed { index, timestamp ->
            val locationObject = networkActivity.locationObject(
                latitude = latitudeList[index],
                longitude = longitudeList[index],
                timeStamp = timestamp
            )
            locationArray.put(locationObject)
        }
        val locationDataObject = networkActivity.locationDataObject(
            phoneNumber = phoneNumber,
            locationArray = locationArray
        )

        Log.d("corona", "My Data: $locationDataObject")

        networkActivity.postDataBackground(locationUrl, locationDataObject)
    }

}