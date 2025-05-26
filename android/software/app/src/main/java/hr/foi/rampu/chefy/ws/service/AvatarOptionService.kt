package hr.foi.rampu.chefy.ws.service

import hr.foi.rampu.chefy.ws.response.AvatarOptionResponseGet
import hr.foi.rampu.chefy.ws.response.AvatarOptionResponsePost
import hr.foi.rampu.chefy.ws.response.AvatarOptionResponsePut
import hr.foi.rampu.chefy.ws.items.AvatarOptionItem
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface AvatarOptionService {

    @GET("avatarOptions.php")
    fun getAvatarOptionsByUserId(@Query("id_user") query: String): Call<AvatarOptionResponseGet>

    @POST("avatarOptions.php")
    fun insertAvatarOptions(
        @Body avatarOption: List<AvatarOptionItem>
    ): Call<AvatarOptionResponsePost>

    @PUT("avatarOptions.php")
    fun updateAvatarOption(
        @Body avatarOption: List<AvatarOptionItem>
    ): Call<AvatarOptionResponsePut>
}
