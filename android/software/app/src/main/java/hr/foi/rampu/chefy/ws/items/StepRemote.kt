package hr.foi.rampu.chefy.ws.items

data class StepRemote(
    val id : Int,
    val name : String,
    val description : String?,
    val stage : Int,
    val making_time : Int,
    val image_path : String?,
    val id_recipe : Int
)
