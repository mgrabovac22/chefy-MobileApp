package hr.foi.rampu.chefy.ws.service

import hr.foi.rampu.chefy.ws.items.RecipeIngredientPost
import hr.foi.rampu.chefy.ws.response.RecipeIngredientResponse
import hr.foi.rampu.chefy.ws.response.RecipeIngredientResponsePost
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RecipeIngredientService {

    @GET("recipeIngridients.php")
    fun getIngredientsByRecipeId(@Query("id_recipe") recipeId: Int): Call<RecipeIngredientResponse>

    @POST("recipeIngridients.php")
    fun insertRecipeIngridientsList(
        @Body recipeIngredients : List<RecipeIngredientPost>
    ): Call<RecipeIngredientResponsePost>

}