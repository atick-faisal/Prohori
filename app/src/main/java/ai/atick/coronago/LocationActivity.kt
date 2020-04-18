package ai.atick.coronago

import android.content.Context
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

class LocationActivity(context: Context) {

    var latitude: String = "24.00001"
    var longitude: String = "90.00001"

    init {
        LocationServices
            .getFusedLocationProviderClient(context)
            .lastLocation
            .addOnSuccessListener { location ->
                this.latitude = location.latitude.toString()
                this.longitude = location.longitude.toString()
            }
    }

    fun getTimeStamp(): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm", Locale.US)
        val date = Calendar.getInstance().time
        return dateFormat.format(date)
    }
}