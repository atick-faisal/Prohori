package ai.atick.coronago

class Key {
    val locationChannelId = "location_channel"
    val uploadChannelId = "upload_channel"
    val locationTaskId = "Location Update"
    val uploadTaskId = "Location Upload"
    val locationUpdateInterval: Long = 15
    val uploadInterval: Long = 17
    val userUrl = "https://covid-callfornation.herokuapp.com/user"
    val locationUrl= "https://covid-callfornation.herokuapp.com/location"
    val mapKey = "MapViewBundleKey"
    val permissionKey = 1011
}