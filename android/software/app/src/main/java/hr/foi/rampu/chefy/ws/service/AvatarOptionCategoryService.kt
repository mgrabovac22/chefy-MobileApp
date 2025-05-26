package hr.foi.rampu.chefy.ws.service

import hr.foi.rampu.chefy.ws.response.AvatarOptionCategoriesResponseGet
import retrofit2.Call
import retrofit2.http.GET

interface AvatarOptionCategoryService {

    @GET("avatarOptionCategories.php")
    fun getAvatarOptionCategories(): Call<AvatarOptionCategoriesResponseGet>
}
