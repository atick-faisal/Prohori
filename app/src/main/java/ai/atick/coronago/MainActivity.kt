package ai.atick.coronago

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var networkActivity: NetworkActivity
    private lateinit var locationActivity: LocationActivity

    private var userUrl: String = "http://home.jamiussiam.com:8090/user"
    private var locationUrl: String = "http://home.jamiussiam.com:8090/location"
    // network security config was required for http request //

    // --------- Dummy Data --------- //
    private var phone = "01711010101"
    private var gender = "OTHER"
    private var birthDate = "01-01-1969"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        networkActivity = NetworkActivity(this)
        locationActivity = LocationActivity(this)

        userDataButton.setOnClickListener {
            val userData = networkActivity.userDataObject(
                phone = phone,
                gender = gender,
                birthDate = birthDate
            )
            networkActivity.postData(
                url = userUrl,
                data = userData
            )
        }

        locationDataButton.setOnClickListener {
            val locationData = networkActivity.locationDataObject(
                latitude = locationActivity.latitude,
                longitude = locationActivity.longitude,
                timeStamp = locationActivity.getTimeStamp()
            )
            networkActivity.postData(
                url = locationUrl,
                data = locationData
            )
        }
    }
}
