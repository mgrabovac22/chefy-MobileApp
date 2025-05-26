package hr.foi.rampu.chefy.ws.service


import hr.foi.rampu.chefy.ws.response.MyRecipeDeleteResponse
import hr.foi.rampu.chefy.ws.response.MyRecipeInsertResponse
import hr.foi.rampu.chefy.ws.response.RecipeItemResponse
import hr.foi.rampu.chefy.ws.response.RecipeResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface RecipeService {

    @GET("recipes.php")
    fun getRecipeById(@Query("id") query: Int): Call<RecipeItemResponse>

    @GET("recipes.php")
    fun getRecipeByUserId(@Query("id_user") query: Int): Call<RecipeItemResponse>

    @GET("recipes.php")
    fun searchRecipes(@Query("name") query: String): Call<RecipeResponse>

    @GET("recipes.php")
    fun searchRecipesWithFilters(@QueryMap filters: Map<String, String>): Call<RecipeResponse>

    @Multipart
    @POST("recipes.php")
    fun insertMyRecipe(
        @Part("recipe") myRecipe : RequestBody,
        @Part image: MultipartBody.Part
    ) : Call<MyRecipeInsertResponse>

    @DELETE("recipes.php")
    fun deleteMyRecipe(@Query("id") query: Int): Call<MyRecipeDeleteResponse>
}