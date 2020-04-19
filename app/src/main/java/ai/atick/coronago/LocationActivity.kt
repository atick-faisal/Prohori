package ai.atick.coronago

import android.content.Context
import android.widget.Toast
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

class LocationActivity(private val context: Context) {

    var latitude: String = "20.00001"
    var longitude: String = "90.00001"

    init {
        this.latitude = "20.50500"
        LocationServices
            .getFusedLocationProviderClient(context)
            .lastLocation
            .addOnSuccessListener { location ->
                Toast.makeText(
                    context,
                    "Latitude: ${location.latitude}",
                    Toast.LENGTH_LONG
                ).show()
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