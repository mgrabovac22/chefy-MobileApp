package hr.foi.rampu.chefy.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import hr.foi.rampu.chefy.R
import hr.foi.rampu.chefy.activities.RemoteStepsActivity
import hr.foi.rampu.chefy.activities.StepsActivity
import hr.foi.rampu.chefy.models.avatar.AvatarCategoriesData
import hr.foi.rampu.chefy.models.avatar.AvatarCategory
import hr.foi.rampu.chefy.models.avatar.AvatarOption
import hr.foi.rampu.chefy.ws.response.AvatarOptionCategoriesResponseGet
import hr.foi.rampu.chefy.ws.items.AvatarOptionCategoryItem
import hr.foi.rampu.chefy.ws.items.AvatarOptionItem
import hr.foi.rampu.chefy.ws.response.AvatarOptionResponseGet
import hr.foi.rampu.chefy.ws.items.RecipeIngredientItem
import hr.foi.rampu.chefy.ws.response.RecipeIngredientResponse
import hr.foi.rampu.chefy.ws.items.RecipeItem
import hr.foi.rampu.chefy.ws.WsChefy
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response





class DetailRecipeRemoteFragment : Fragment() {

    private var recipeId: Int = 0
    private var recipeName: String = ""
    private var recipeDescription: String = ""
    private var recipePicturePath: String? = null
    private var makingTime: Long = 0L
    private var recipeCategory: String = ""
    private var user_id: Int? = null
    private var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            recipeId = it.getInt("id", 0)
            recipeName = it.getString("name", "")
            recipeDescription = it.getString("description", "")
            recipePicturePath = it.getString("picturePath")
            makingTime = it.getLong("makingTime", 0L)
            recipeCategory = it.getString("category", "")
            username = it.getString("username", "")
            user_id = it.getInt("id_user", 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_detail_recipe_remote, container, false)

        val recipeImageView = view.findViewById<ImageView>(R.id.tv_detail_recipe_picture)
        val recipeNameTextView = view.findViewById<TextView>(R.id.tv_detail_recipe_name)
        val recipeDescriptionTextView = view.findViewById<TextView>(R.id.tv_description)
        val makingTimeTextView = view.findViewById<TextView>(R.id.tv_making_time)
        val categoryTextView = view.findViewById<TextView>(R.id.tv_category)
        val authorTextView = view.findViewById<TextView>(R.id.tv_author)




        recipeNameTextView.text = recipeName
        recipeDescriptionTextView.text = recipeDescription
        makingTimeTextView.text = convertTimeMinutes(makingTime)
        categoryTextView.text = recipeCategory
        authorTextView.text = username

        if(recipePicturePath != null){
            val imagePath = parseImagePath(recipePicturePath!!)
            Log.d("IMAGE DEBUG", imagePath);

            WsChefy.getPicasso(requireContext())
                .load(imagePath)
                .into(recipeImageView)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val button: FloatingActionButton = view.findViewById(R.id.btn_preparation)
        val buttonBack: Button = view.findViewById(R.id.back_button)
        val btnShare = view.findViewById<Button>(R.id.btnShare)

        buttonBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        button.setOnClickListener {
            openStepsActivity()
        }

        btnShare.setOnClickListener{

            //val shareMessage = "intent://recipe/$recipeId#Intent;scheme=chefy;package=hr.foi.rampu.chefy;end"
            //val shareMessage = "chefy://recipe/$recipeId"

            //val shareMessage = "Pogledaj ovaj ukusan recept: http://157.230.8.219/chefy/redirect.php?id=$recipeId"

            val shareMessage = "🍽️ Pogledaj ovaj ukusan recept! 😋 \n\n🌐Poveznica: \nhttp://157.230.8.219/chefy/redirect.php?id=$recipeId \n\nUživaj u kuhanju! 🔥 \nChefy 👨‍🍳"

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareMessage)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, "Podijeli recept s drugima"))
        }

        loadIngridients()
        if(user_id != null){
            loadSavedAvatarOptions(context?: return)
        }
    }


    private fun loadIngridients() {
        fetchIngredientsByRecipeId(recipeId) { ingredients, success ->
            if (success) {
                val ingridientsTextView = view?.findViewById<TextView>(R.id.tv_detail_ingredients)
                var ingridients_details = ""
                ingredients.forEach {
                    ingridients_details +=  "- ${it.quantity} ${it.unitName} ${it.ingredientName}\n"
                }
                ingridientsTextView?.text = ingridients_details
            } else {
                Log.d("INGRIDIENTS DEBUG","Greška prilikom dohvaćanja sastojaka.")
            }
        }
    }


    private fun openStepsActivity() {
        val intent = Intent(requireActivity(), RemoteStepsActivity::class.java).apply {
            putExtra("recipe_id", recipeId)
            putExtra("recipe_name", recipeName)
            putExtra("recipe_description", recipeDescription)


        }
        startActivity(intent)
    }

    companion object {

        @JvmStatic
        fun newInstance(recipe : RecipeItem) =
            DetailRecipeRemoteFragment().apply {
                arguments = Bundle().apply {
                    putInt("id", recipe.id)
                    putString("name", recipe.name)
                    putString("description", recipe.description)
                    putString("picturePath", recipe.picturePath)
                    putLong("makingTime", recipe.makingTime)
                    putString("category", recipe.category)
                    putInt("id_user", recipe.id_user)
                    putString("username", recipe.username)
                }
            }
    }

    private fun parseImagePath(imagePath : String) : String{
        return WsChefy.IMAGE_BASE_URL + imagePath
    }

    private fun convertTimeMinutes(makingTime: Long): String {

        if (makingTime < 60) {
            return makingTime.toString() + "min"
        }
        else if (makingTime % 60 == 0.toLong()) {
            val hours = (makingTime / 60).toString()
            return hours + "h"
        }
        else {
            val hours = (makingTime / 60).toString()
            val minutes = (makingTime % 60).toString()
            return hours + "h " + minutes + "min"
        }

    }

    private fun loadSavedAvatarOptions(context: Context){

        fetchAvatarOptionCategories{ avatarOptionCategories, success ->

            if(!success){
                return@fetchAvatarOptionCategories
            }

            fetchAvatarOptionsByUserId(user_id!!) { userAvatarOptions, success ->

                if(!success){
                    return@fetchAvatarOptionsByUserId
                }

                AvatarCategoriesData.buildInstance(context)
                val avatarCategories = AvatarCategoriesData.getInstance()

                userAvatarOptions.forEach { option ->
                    val categoryName =
                        avatarOptionCategories.find { category -> category.id == option.id_avatarOptionCategory }?.name
                    val optionName = option.name;
                    if (categoryName != null) {

                        val category : AvatarCategory? = avatarCategories.find { it.name == categoryName}
                        if(category != null){
                            val avatar_option : AvatarOption? = category.options.find { it.name == optionName}
                            if(avatar_option != null){
                                updateAvatarPreview(category, avatar_option)
                            }
                        }
                    }
                }
                val avatarContainer = view?.findViewById<CardView>(R.id.avatarContainer)
                avatarContainer?.visibility = View.VISIBLE
            }
        }
    }



    private fun fetchAvatarOptionCategories(callback: (List<AvatarOptionCategoryItem>, Boolean) -> Unit) {
        WsChefy.avatarOptionCategoryService.getAvatarOptionCategories().enqueue(
            object : Callback<AvatarOptionCategoriesResponseGet> {
                override fun onResponse(
                    call: Call<AvatarOptionCategoriesResponseGet>,
                    response: Response<AvatarOptionCategoriesResponseGet>
                ) {
                    if (response.isSuccessful) {
                        val avatarOptionCategories  = response.body()?.results ?: emptyList()
                        if(avatarOptionCategories.isNotEmpty()){
                            callback(avatarOptionCategories, true)
                        }
                        else{
                            callback(emptyList(), false)
                        }

                        Log.d("AvatarCategories", "Dohvaćene kategorije: $avatarOptionCategories")
                    } else {
                        Log.e("AvatarCategories", "Greška u odgovoru: ${response.code()}")
                        callback(emptyList(), false)
                    }
                }

                override fun onFailure(call: Call<AvatarOptionCategoriesResponseGet>, t: Throwable) {

                    Log.e("AvatarCategories", "Greška u dohvaćanju podataka: ${t.message}")
                    callback(emptyList(), false)
                }
            }
        )
    }

    private fun fetchAvatarOptionsByUserId(idUser : Number,callback: (List<AvatarOptionItem>, Boolean) -> Unit) {
        WsChefy.avatarOptionService.getAvatarOptionsByUserId(idUser.toString()).enqueue(
            object : Callback<AvatarOptionResponseGet> {
                override fun onResponse(
                    call: Call<AvatarOptionResponseGet>,
                    response: Response<AvatarOptionResponseGet>
                ) {
                    if (response.isSuccessful) {
                        val avatarOptions  = response.body()?.results ?: emptyList()

                        Log.d("AvatarOptions", "Dohvaćene opcije: $avatarOptions")
                        callback(avatarOptions, true)
                    } else {
                        Log.e("AvatarOptions", "Greška u odgovoru: ${response.code()}")
                        callback(emptyList(), false)
                    }
                }

                override fun onFailure(call: Call<AvatarOptionResponseGet>, t: Throwable) {
                    Log.e("AvatarOptions", "Greška u dohvaćanju podataka: ${t.message}")
                    callback(emptyList(), false)
                }

            }
        )
    }

    private fun fetchIngredientsByRecipeId(
        recipeId: Number,
        callback: (List<RecipeIngredientItem>, Boolean) -> Unit
    ) {
        WsChefy.recipeIngredientService.getIngredientsByRecipeId(recipeId.toInt()).enqueue(
            object : Callback<RecipeIngredientResponse> {
                override fun onResponse(
                    call: Call<RecipeIngredientResponse>,
                    response: Response<RecipeIngredientResponse>
                ) {
                    if (response.isSuccessful) {
                        val ingredients = response.body()?.results ?: emptyList()

                        Log.d("RecipeIngredients", "Dohvaćeni sastojci: $ingredients")
                        callback(ingredients, true)
                    } else {
                        Log.e("RecipeIngredients", "Greška u odgovoru: ${response.code()}")
                        callback(emptyList(), false)
                    }
                }

                override fun onFailure(call: Call<RecipeIngredientResponse>, t: Throwable) {
                    Log.e("RecipeIngredients", "Greška u dohvaćanju podataka: ${t.message}")
                    callback(emptyList(), false)
                }
            }
        )
    }




    private fun updateAvatarPreview(selectedCategory: AvatarCategory, selectedOption: AvatarOption) {

        val imageViewLayer = view?.findViewById<ImageView>(selectedCategory.layoutViewId)
        imageViewLayer?.setImageResource(selectedOption.imageResId)
        imageViewLayer?.visibility = if (selectedOption.imageResId != R.drawable.avatar_option_none) View.VISIBLE else View.INVISIBLE
    }


}
