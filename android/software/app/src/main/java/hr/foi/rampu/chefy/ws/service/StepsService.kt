package hr.foi.rampu.chefy.ws.service

import hr.foi.rampu.chefy.ws.response.GetStepsRemoteResponse
import hr.foi.rampu.chefy.ws.response.MyRecipeInsertResponse
import hr.foi.rampu.chefy.ws.response.MyStepInsertResponse
import hr.foi.rampu.chefy.ws.response.RecipeResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface StepsService {

    @GET("steps.php")
    fun getAllStepsForRecipe(@Query("id_recipe") query: Int): Call<GetStepsRemoteResponse>

    @Multipart
    @POST("steps.php")
    fun insertMyStep(
        @Part("step") myStep : RequestBody,
        @Part image: MultipartBody.Part
    ) : Call<MyStepInsertResponse>

}