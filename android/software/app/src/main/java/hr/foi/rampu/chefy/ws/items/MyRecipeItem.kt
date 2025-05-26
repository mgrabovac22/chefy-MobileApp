package hr.foi.rampu.chefy.ws.items

import com.google.gson.annotations.SerializedName

data class MyRecipeItem(
    val id: Int,
    val name: String,
    val description: String,
    @SerializedName("picture_path") val picturePath: String?,
    @SerializedName("making_time") val makingTime : Long,
    val category: String,
    val id_user: Int,
)