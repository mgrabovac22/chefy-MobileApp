package hr.foi.rampu.chefy.ws

import android.content.Context
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import hr.foi.rampu.chefy.ws.service.AvatarOptionCategoryService
import hr.foi.rampu.chefy.ws.service.AvatarOptionService
import hr.foi.rampu.chefy.ws.service.RecipeIngredientService
import hr.foi.rampu.chefy.ws.service.RecipeService
import hr.foi.rampu.chefy.ws.service.StepsService
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WsChefy {
    private const val BASE_URL = "http://157.230.8.219/chefy/"
    private const val USERNAME = "smartcoders"
    private const val PASSWORD = "y39T>("
    const val IMAGE_BASE_URL = "http://157.230.8.219/chefy"

    val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", Credentials.basic(USERNAME, PASSWORD))
                .build()
            chain.proceed(request)
        })
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private var picasso: Picasso? = null
    fun getPicasso(context: Context): Picasso {
        if (picasso == null) {
            picasso = Picasso.Builder(context)
                .downloader(OkHttp3Downloader(client))
                .build()
        }
        return picasso!!
    }

    val recipeService: RecipeService = retrofit.create(RecipeService::class.java)
    val stepsService: StepsService = retrofit.create(StepsService::class.java)
    val avatarOptionCategoryService: AvatarOptionCategoryService = retrofit.create(
        AvatarOptionCategoryService::class.java)
    val avatarOptionService: AvatarOptionService = retrofit.create(AvatarOptionService::class.java)
    val recipeIngredientService: RecipeIngredientService = retrofit.create(RecipeIngredientService::class.java)
}
