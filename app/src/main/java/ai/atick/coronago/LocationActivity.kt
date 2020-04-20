package ai.atick.coronago

import android.content.Context
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

class LocationActivity(private val context: Context) {

    private val database: AppDatabase = AppDatabase(context)

    fun updateLocation() {
        LocationServices
            .getFusedLocationProviderClient(context)
            .lastLocation
            .addOnSuccessListener { location ->
                val latitudeList = database.getListString("latitudeList")
                val longitudeList = database.getListString("longitudeList")
                val timestampList = database.getListString("timestampList")
                //////////////////////////////////////////////////////////////////
                latitudeList.add(location.latitude.toString())
                longitudeList.add(location.longitude.toString())
                timestampList.add(getTimeStamp())
                //////////////////////////////////////////////////////////////////
                database.putListString("latitudeList", latitudeList)
                database.putListString("longitudeList", longitudeList)
                database.putListString("timestampList", timestampList)
            }
    }
    /////////////////////////////////////////////////////////////////////////////////
    private fun getTimeStamp(): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm", Locale.US)
        val date = Calendar.getInstance().time
        return dateFormat.format(date)
    }
}