package ai.atick.coronago

class Key {
    var name = "No Name"
    var phoneNumber = "01711000000"
    var gender = "OTHER"
    val locationChannelId = "location_channel"
    val uploadChannelId = "upload_channel"
    var birthDate: String = "16-12-1971"
    val locationTaskId = "Location Update"
    val uploadTaskId = "Location Upload"
    val locationUpdateInterval: Long = 15
    val uploadInterval: Long = 30
    val userUrl = "https://covid-callfornation.herokuapp.com/user"
    val locationUrl= "https://covid-callfornation.herokuapp.com/location"
}