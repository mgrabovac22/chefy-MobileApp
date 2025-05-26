package hr.foi.rampu.chefy

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import hr.foi.rampu.chefy.activities.MyStepsActivity
import hr.foi.rampu.chefy.data.db.RecipesDatabase
import hr.foi.rampu.chefy.models.avatar.AvatarCategoriesData
import hr.foi.rampu.chefy.models.avatar.AvatarCategory
import hr.foi.rampu.chefy.models.avatar.AvatarOption
import hr.foi.rampu.chefy.models.recipe.MyRecipes
import hr.foi.rampu.chefy.ws.response.AvatarOptionCategoriesResponseGet
import hr.foi.rampu.chefy.ws.items.AvatarOptionCategoryItem
import hr.foi.rampu.chefy.ws.items.AvatarOptionItem
import hr.foi.rampu.chefy.ws.response.AvatarOptionResponseGet
import hr.foi.rampu.chefy.ws.WsChefy
import hr.foi.rampu.chefy.ws.items.MyRecipeItem
import hr.foi.rampu.chefy.ws.items.RecipeIngredientPost
import hr.foi.rampu.chefy.ws.items.StepRemote
import hr.foi.rampu.chefy.ws.response.AvatarOptionResponsePost
import hr.foi.rampu.chefy.ws.response.MyRecipeDeleteResponse
import hr.foi.rampu.chefy.ws.response.MyRecipeInsertResponse
import hr.foi.rampu.chefy.ws.response.MyStepInsertResponse
import hr.foi.rampu.chefy.ws.response.RecipeIngredientResponse
import hr.foi.rampu.chefy.ws.response.RecipeIngredientResponsePost
import hr.foi.rampu.chefy.ws.response.RecipeResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DetailRecipeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyDetailRecipeFragment : Fragment() {

    private lateinit var recipeNameTextView: TextView
    private lateinit var recipePictureImageView: ImageView
    private lateinit var recipeDescription: TextView
    private lateinit var recipeCategory: TextView
    private lateinit var tv_detail_ingredients: TextView
    private lateinit var tv_making_time: TextView
    private lateinit var btnPublish: Button


    private var recipeId: Int? = null
    private var recipeName: String? = null
    private var recipePicture: String? = null
    private var recipeDesc: String? = null
    private var category: String? = null
    private var makingTime: Long? = null
    private var user_id : Int? = null

    private var remoteId : Int? = null
    private var recipePublished : Boolean? = null

    private val db: RecipesDatabase by lazy { RecipesDatabase.getInstance() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            recipeId = it.getInt("id", -1)
            recipeName = it.getString("name")
            recipeDesc = it.getString("description")
            recipePicture = it.getString("picturePath")
            category = it.getString("category")
            makingTime = it.getLong("makingTime")
            remoteId = it.getInt("remoteId")
        }
        val imageViewLayer = view?.findViewById<ImageView>(R.id.imvAuthorPicture)
        imageViewLayer?.setImageResource(R.drawable.logo)
        imageViewLayer?.bringToFront()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewRecipe = inflater.inflate(R.layout.fragment_my_detail_recipe, container, false)
        recipeNameTextView = viewRecipe.findViewById(R.id.tv_detail_recipe_name)
        recipePictureImageView = viewRecipe.findViewById(R.id.tv_detail_recipe_picture)
        recipeDescription = viewRecipe.findViewById(R.id.tv_description)
        tv_detail_ingredients = viewRecipe.findViewById(R.id.tv_detail_ingredients)
        recipeCategory = viewRecipe.findViewById(R.id.tv_category)
        tv_making_time = viewRecipe.findViewById(R.id.tv_making_time)

        btnPublish = viewRecipe.findViewById<Button>(R.id.btnPublish)


        val sharedPreferences = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)

        val authorRecipeTextView: TextView = viewRecipe.findViewById(R.id.tv_author)
        val author = sharedPreferences.getString("user_firstName", null)
        authorRecipeTextView.text = author
        //Toast.makeText(requireContext(), author, Toast.LENGTH_SHORT).show()


        var user_id_string = sharedPreferences.getString("user_id", null)
        user_id = user_id_string?.toInt()


        val backButton: Button = viewRecipe.findViewById(R.id.back_button)

        recipeNameTextView.text = recipeName
        recipeDescription.text = recipeDesc
        recipeCategory.text = category
        tv_making_time.text = makingTime?.let { convertTimeMinutes(it.toLong()) }


        recipePicture?.let {
            val imageUri = Uri.parse(it)
            recipePictureImageView.setImageURI(imageUri)
        } ?: run {
            recipePictureImageView.setImageResource(R.drawable.food_picture)
        }



        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }


        btnPublish.setOnClickListener {
            if(recipePublished != null){
                manageRecipePublish(recipePublished!!)
            }
        }


        //Toast.makeText(context, "Recipe local id = " + recipeId, Toast.LENGTH_SHORT).show()
        //Toast.makeText(context, "Recipe remote id = " + remoteId, Toast.LENGTH_SHORT).show()

        return viewRecipe
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val button: FloatingActionButton = view.findViewById(R.id.btn_preparation)
        button.setOnClickListener {
            openStepsActivity()
        }

        loadRecipeData()
        loadIngredients()
        loadSavedAvatarOptions(requireContext())
        checkIfRecipeIsPublished()

        val imageViewLayer = view.findViewById<ImageView>(R.id.imvAuthorPicture)
        imageViewLayer?.setImageResource(R.drawable.logo)
    }

    private fun checkIfRecipeIsPublished() {
        if(remoteId == -1){
            recipePublished = false
        }
        else{
            recipePublished = true
        }
        updatePublishButtonState()
    }



    private fun loadRecipeData() {
        recipeId?.let { id ->
            if (id != -1) {
                CoroutineScope(Dispatchers.IO).launch {
                    val recipe = db.daoMyRecipes.getOneRecipe(id)
                    withContext(Dispatchers.Main) {
                        recipe?.let {
                            recipeNameTextView.text = it.name
                            recipeDescription.text = it.description

                            if (!it.picturePaths.isNullOrEmpty()) {
                                val imageFile = File(it.picturePaths)
                                if (imageFile.exists()) {
                                    val imageUri = Uri.fromFile(imageFile)
                                    recipePictureImageView.setImageURI(imageUri)
                                } else {
                                    recipePictureImageView.setImageResource(R.drawable.food_picture)
                                }
                            } else {
                                recipePictureImageView.setImageResource(R.drawable.food_picture)
                            }
                        }
                    }
                }
            }
        }
    }


    private fun loadIngredients() {
        recipeId?.let { id ->
            if (id != -1) {
                CoroutineScope(Dispatchers.IO).launch {
                    val ingredients = db.daoMyRecipeIngredients.getFullIngredientsForRecipe(id)

                    Log.d("MyDetailRecipeFragment", "Ingredients fetched: $ingredients")

                    withContext(Dispatchers.Main) {
                        if (ingredients.isNotEmpty()) {
                            val ingredientText = ingredients.joinToString("\n") {
                                "- ${it.quantity} ${it.unitName} ${it.ingredientName}"
                            }
                            tv_detail_ingredients.text = ingredientText
                        } else {
                            tv_detail_ingredients.text = "No ingredients available"
                        }
                    }
                }
            }
        }
    }



    private fun openStepsActivity() {
        val intent = Intent(requireActivity(), MyStepsActivity::class.java).apply {
            putExtra("recipe_name", recipeName)
            putExtra("recipe_description", recipeDesc)
            putExtra("recipe_id", recipeId)
        }
        startActivity(intent)
    }


    companion object {
        fun newInstance(id: Int, recipeName: String, recipePicture: String, recipeAuthor: String, recipeDesc: String) =
            MyDetailRecipeFragment().apply {
                arguments = Bundle().apply {
                    putInt("id", id)
                    putString("recipe_name", recipeName)
                    putString("recipe_picture", recipePicture)
                    putString("recipe_desc", recipeDesc)
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
        val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val isAvatarFetched = sharedPreferences.contains("avatarFetch")
        if (isAvatarFetched) {

            AvatarCategoriesData.buildInstance(context)
            val avatarCategories = AvatarCategoriesData.getInstance()

            val allEntries = sharedPreferences.all
            val avatarEntries = allEntries.filter { it.key.startsWith("avatar-") }
            avatarEntries.forEach { entry ->
                val categoryName = entry.key.split("-").last()
                val category : AvatarCategory? = avatarCategories.find { it.name == categoryName}
                val optionName = entry.value.toString()
                if(category != null){
                    val avatarOption : AvatarOption? = category.options.find { it.name == optionName}
                    if(avatarOption != null){
                        Log.d("TEST DEBUG AVATAR2", categoryName + " - " + optionName)
                        updateAvatarPreview(category, avatarOption)
                    }

                }
            }

            showAvatarContainer()


        } else {
            //Toast.makeText(context, "db", Toast.LENGTH_SHORT).show()
            loadSavedAvatarOptionsFromDB(context)
        }
    }

    private fun showAvatarContainer() {
        val avatarContainer = view?.findViewById<CardView>(R.id.avatarContainer)
        avatarContainer?.visibility = View.VISIBLE
    }


    private fun loadSavedAvatarOptionsFromDB(context: Context){

        fetchAvatarOptionCategories{ avatarOptionCategories, success ->

            if(!success || user_id == null){
                return@fetchAvatarOptionCategories
            }

            fetchAvatarOptionsByUserId(user_id!!) { userAvatarOptions, success ->

                if(!success){
                    return@fetchAvatarOptionsByUserId
                }

                val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

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
                                saveAvatarOptionToSharedPreferences(categoryName,optionName, sharedPreferences)
                            }
                        }
                    }
                }

                showAvatarContainer()
            }
        }
    }

    private fun saveAvatarOptionToSharedPreferences(
        categoryName: String,
        optionName: String,
        sharedPreferences: SharedPreferences
    ) {

        val editor = sharedPreferences.edit()

        var key = "avatar-" + categoryName
        editor.putString(key, optionName)

        editor.putBoolean("avatarFetch", true)

        editor.apply()
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

    private fun fetchAvatarOptionsByUserId(idUser : Int,callback: (List<AvatarOptionItem>, Boolean) -> Unit) {
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

    private fun updateAvatarPreview(selectedCategory: AvatarCategory, selectedOption: AvatarOption) {

        val imageViewLayer = view?.findViewById<ImageView>(selectedCategory.layoutViewId)
        Log.d("TEST DEBUG AVATAR", imageViewLayer.toString())
        imageViewLayer?.setImageResource(selectedOption.imageResId)
        imageViewLayer?.visibility = if (selectedOption.imageResId != R.drawable.avatar_option_none) View.VISIBLE else View.INVISIBLE
    }


    private fun manageRecipePublish(recipePublished: Boolean) {
        btnPublish.isEnabled = false
        if(!recipePublished){
            val myRecipe = MyRecipeItem(
                id = recipeId!!,
                name = recipeName!!,
                description = recipeDesc!!,
                picturePath = recipePicture,
                makingTime = makingTime!!,
                category = category!!,
                id_user = user_id!!
            )

            val gson = Gson()
            val recipeJson = gson.toJson(myRecipe)

            val requestBody = RequestBody.create(MediaType.parse("application/json"), recipeJson)

            val file = File(recipePicture)
            val requestBodyImage = RequestBody.create(MediaType.parse("image/jpeg"), file)
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestBodyImage)


            postMyRecipe(requestBody, imagePart){success, remoteId ->
                //Toast.makeText(context, "Uploaded", Toast.LENGTH_SHORT).show()
                //Toast.makeText(context, "Remote id: " +  remoteId.toString(), Toast.LENGTH_SHORT).show()
                if(!success || remoteId == null){
                    return@postMyRecipe
                }


                uploadIngredients(remoteId){}


                uploadAllSteps(remoteId){
                    //update remoteId in local db
                    updateLocalRecipe(myRecipe.id, remoteId){
                        this.remoteId = remoteId
                        this.recipePublished = true
                        updatePublishButtonState()
                    }
                }





            }
        }
        else{
            //Toast.makeText(context, "Recipe remote id = " + remoteId, Toast.LENGTH_SHORT).show()

            if(this.remoteId != -1){
                deleteMyRecipe(this.remoteId!!){
                    //Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()

                    this.remoteId = null
                    this.recipePublished = false
                    updateLocalRecipe(recipeId!!,null){
                        updatePublishButtonState()
                    }

                }
            }
        }
    }


    private fun updatePublishButtonState() {

        val tvStatus = view?.findViewById<TextView>(R.id.tv_status)

        if(recipePublished != null){
            if(recipePublished == true){

                tvStatus?.text = "Javno"
                tvStatus?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_public, 0)
                tvStatus?.compoundDrawablePadding = 8

                btnPublish.text = "Povuci"
            }
            else{

                tvStatus?.text = "Privatno"
                tvStatus?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_lock, 0)
                tvStatus?.compoundDrawablePadding = 8

                btnPublish.text = "Objavi"
            }
            btnPublish.isEnabled = true
        }
    }


    private fun postMyRecipe(myRecipe: RequestBody, imagePart : MultipartBody.Part , callback:(Boolean, Int?) -> Unit) {
        WsChefy.recipeService.insertMyRecipe(myRecipe,imagePart).enqueue(
            object : Callback<MyRecipeInsertResponse> {
                override fun onResponse(
                    call: Call<MyRecipeInsertResponse>,
                    response: Response<MyRecipeInsertResponse>
                ) {
                    if (response.isSuccessful) {
                        //Toast.makeText(applicationContext, "SUCCESS", Toast.LENGTH_SHORT).show()
                        Log.d("TEST RECIPE UPLOAD", response.body().toString())
                        if(response.body()?.error != null){
                            //Toast.makeText(context, response.body()?.error, Toast.LENGTH_SHORT).show()
                        }


                        if(response.body()?.id != null){

                            callback(true,response.body()?.id);
                        }
                        else{
                            callback(false, null);
                        }
                    } else {
                        //Toast.makeText(applicationContext, "ERROR", Toast.LENGTH_SHORT).show()
                        Log.d("TEST RECIPE UPLOAD", "ERROR")
                        callback(false, null);
                    }
                }

                override fun onFailure(call: Call<MyRecipeInsertResponse>, t: Throwable) {

                    //Toast.makeText(applicationContext, "Failure", Toast.LENGTH_SHORT).show()
                    Log.d("TEST RECIPE UPLOAD", "onFailure: " + t.message)

                    t.printStackTrace()

                    callback(false, null);
                }
            }
        )
    }

    private fun postMyStep(myStep: RequestBody, imagePart : MultipartBody.Part , callback:(Boolean) -> Unit) {
        WsChefy.stepsService.insertMyStep(myStep,imagePart).enqueue(
            object : Callback<MyStepInsertResponse> {
                override fun onResponse(
                    call: Call<MyStepInsertResponse>,
                    response: Response<MyStepInsertResponse>
                ) {
                    if (response.isSuccessful) {
                        callback(true);
                    } else {
                        callback(false);
                    }
                }

                override fun onFailure(call: Call<MyStepInsertResponse>, t: Throwable) {
                    callback(false);
                }
            }
        )
    }

    private fun uploadIngredients(remoteId : Int, callback: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = RecipesDatabase.getInstance()
            val allIngredientsLocal = db.daoMyRecipeIngredients.getFullIngredientsForRecipe(recipeId!!)

            val remoteRecipeIngredients : MutableList<RecipeIngredientPost> = mutableListOf()

            for(localIngredient in allIngredientsLocal){

                val converted = RecipeIngredientPost(
                    recipeId = remoteId,
                    ingredientName = localIngredient.ingredientName,
                    unitName = localIngredient.unitName,
                    quantity = localIngredient.quantity
                )

                remoteRecipeIngredients.add(converted)
            }


            withContext(Dispatchers.Main) {

                postRecipeIngredientsList(remoteRecipeIngredients){}
                callback(true)
            }

        }
    }

    private fun postRecipeIngredientsList(remoteRecipeIngredients: List<RecipeIngredientPost>, callback: (Boolean) -> Unit) {
        WsChefy.recipeIngredientService.insertRecipeIngridientsList(remoteRecipeIngredients).enqueue(
            object : Callback<RecipeIngredientResponsePost> {
                override fun onResponse(
                    call: Call<RecipeIngredientResponsePost>,
                    response: Response<RecipeIngredientResponsePost>
                ) {
                    if (response.isSuccessful) {

                        if(response.body()?.error != null){
                            Log.d("INGREDIENTS DEBUG", "error: " + response.body()?.error)
                        }

                        if(response.body()?.message != null){
                            Log.d("INGREDIENTS DEBUG", "message: " + response.body()?.message)
                        }



                        callback(true);
                    } else {
                        Log.d("INGREDIENTS DEBUG", "NOT OK")
                        callback(false);
                    }
                }

                override fun onFailure(call: Call<RecipeIngredientResponsePost>, t: Throwable) {
                    Log.d("INGREDIENTS DEBUG", "onFailure")
                    callback(false);
                }
            }
        )
    }


    private fun uploadAllSteps(remoteId : Int, callback: (Boolean) -> Unit) {

        CoroutineScope(Dispatchers.IO).launch {
            val db = RecipesDatabase.getInstance()
            val allSteps = db.daoMySteps.getOneStep(recipeId!!)

            withContext(Dispatchers.Main) {

                for(step in allSteps){

                    val myStepRemote = StepRemote(
                        id = -1,
                        name = step.name,
                        description = step.description,
                        stage = step.stage,
                        making_time = step.makingTime,
                        image_path = step.imagePath,
                        id_recipe = remoteId
                    )

                    val gson = Gson()
                    val myStepRemoteJson = gson.toJson(myStepRemote)
                    val requestBody = RequestBody.create(MediaType.parse("application/json"), myStepRemoteJson)

                    val file = File(step.imagePath)
                    val requestBodyImage = RequestBody.create(MediaType.parse("image/jpeg"), file)
                    val imagePart = MultipartBody.Part.createFormData("image", file.name, requestBodyImage)

                    postMyStep(requestBody,imagePart){}
                }

                callback(true)
            }
        }
    }



    private fun updateLocalRecipe(localId : Int, remoteId : Int?, callback:(Boolean) -> Unit) {

        CoroutineScope(Dispatchers.IO).launch {
            val db = RecipesDatabase.getInstance()

            val recipe = db.daoMyRecipes.getOneRecipe(localId)
            recipe.remoteId = remoteId

            db.daoMyRecipes.updateRecipe(recipe)

            withContext(Dispatchers.Main) {
                callback(true)
            }
        }
    }








    private fun deleteMyRecipe(id: Int, callback:(Boolean) -> Unit) {
        WsChefy.recipeService.deleteMyRecipe(id).enqueue(object : Callback<MyRecipeDeleteResponse> {
            override fun onResponse(call: Call<MyRecipeDeleteResponse>, response: Response<MyRecipeDeleteResponse>) {
                if (response.isSuccessful) {
                    //Toast.makeText(context, "Recept uspješno obrisan", Toast.LENGTH_SHORT).show()
                    callback(true);
                } else {
                    //Toast.makeText(context, "Greška u brisanju recepta", Toast.LENGTH_SHORT).show()
                    callback(false);
                }
            }

            override fun onFailure(call: Call<MyRecipeDeleteResponse>, t: Throwable) {

                //Toast.makeText(context, "Neuspješno brisanje recepta", Toast.LENGTH_SHORT).show()
                callback(false);
            }
        })
    }
}