package ai.atick.coronago

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.LocationServices
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TestActivity(private val context: Context) {

    private val database: AppDatabase = AppDatabase(context)
    private val networkActivity: NetworkActivity = NetworkActivity(context)

    val latitudeList: ArrayList<String> = ArrayList()
    val longitudeList: ArrayList<String> = ArrayList()
    val timestampList: ArrayList<String> = ArrayList()

    fun updateLocation() {
        LocationServices
            .getFusedLocationProviderClient(context)
            .lastLocation
            .addOnSuccessListener { location ->
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
        val latitudeList = database.getListString("latitudeList").toMutableList()
        val longitudeList = database.getListString("longitudeList").toMutableList()
        val timestampList = database.getListString("timestampList").toMutableList()

        Log.d("corona", timestampList.size.toString())

        val dataArray = JSONArray()

        timestampList.forEachIndexed { index, timestamp ->
            val jsonObject = networkActivity.locationDataObject(
                latitude = latitudeList[index],
                longitude = longitudeList[index],
                timeStamp = timestamp
            )
            dataArray.put(jsonObject)
        }

        Log.d("corona", "My Data: $dataArray")
    }

}