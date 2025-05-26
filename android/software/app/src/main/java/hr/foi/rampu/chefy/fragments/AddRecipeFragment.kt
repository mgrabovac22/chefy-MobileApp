package hr.foi.rampu.chefy

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.util.query
import hr.foi.rampu.chefy.activities.AddRecipeActivity
import hr.foi.rampu.chefy.adapters.MyRecipesAdapter
import hr.foi.rampu.chefy.data.db.RecipesDatabase
import hr.foi.rampu.chefy.models.recipe.MyRecipeIngredients
import hr.foi.rampu.chefy.fragments.IngredientsListFragment
import hr.foi.rampu.chefy.models.recipe.MyRecipes
import hr.foi.rampu.chefy.models.recipe.MySteps
import hr.foi.rampu.chefy.ws.WsChefy
import hr.foi.rampu.chefy.ws.items.RecipeIngredientItem
import hr.foi.rampu.chefy.ws.items.RecipeItem
import hr.foi.rampu.chefy.ws.items.StepRemote
import hr.foi.rampu.chefy.ws.response.GetStepsRemoteResponse
import hr.foi.rampu.chefy.ws.response.RecipeIngredientResponse
import hr.foi.rampu.chefy.ws.response.RecipeItemResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Call
import retrofit2.Callback
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.math.log

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddRecipeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddRecipeFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyRecipesAdapter
    private var recipesList = mutableListOf<MyRecipes>()
    private var id_user : Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add, container, false)


        val sharedPreferences = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        var user_id_string = sharedPreferences.getString("user_id", null)
        id_user = user_id_string?.toInt()

        val btnShoppingList: Button = view.findViewById(R.id.btnShoppingList)
        val btnAddNew: Button = view.findViewById(R.id.btnAddNew)
        recyclerView = view.findViewById(R.id.myRecipesRecyclerView)

        adapter = MyRecipesAdapter(
            recipesList,
            onDeleteClick = { deleteRecipe(it) },
            onEditClick = { editRecipe(it) },
            onRecipeClick = { openMyRecipe(it) }
        )

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.set(16, 16, 16, 16)
            }
        })
        recyclerView.adapter = adapter


        loadRecipesFromDatabase()

        btnAddNew.setOnClickListener {
            val intent = Intent(requireContext(), AddRecipeActivity::class.java)
            startActivity(intent)
        }
        btnShoppingList.setOnClickListener {
            val fragment = IngredientsListFragment()

            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        val btnDownload = view.findViewById<Button>(R.id.btnDownload)
        val progressBarDownload = view.findViewById<ProgressBar>(R.id.pbDownload)

        btnDownload.setOnClickListener {
            btnDownload.text = ""
            btnDownload.isEnabled = false
            progressBarDownload.visibility = View.VISIBLE
            downloadUsersPublishedRecepies {
                progressBarDownload.visibility = View.GONE
                btnDownload.text = "Dohvati svoje javne recepte"
                btnDownload.isEnabled = true
            }
        }

        return view
    }

    private fun downloadUsersPublishedRecepies(onComplete : () -> Unit) {
        //Log.d("DEBUG DOWNLOAD", "TEST1")
        val sharedPreferences = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        var user_id_string = sharedPreferences.getString("user_id", null)

        if(user_id_string != null){

            RecipesDatabase.buildInstance(requireContext())
            val roomDatabase = RecipesDatabase.getInstance()

            getAllRecipesForUser(user_id_string.toInt()){success,allRecipes ->
                //Log.d("DEBUG DOWNLOAD", "TEST2")
                if(!success || allRecipes.isEmpty()){
                    //Log.d("DEBUG DOWNLOAD", "TEST2 EXIT")
                    onComplete()
                    return@getAllRecipesForUser
                }


                val missingRecipes = allRecipes.filter { remoteRecipe ->
                    remoteRecipe.id !in recipesList.map { it.remoteId }
                }
                val missingRecipesCount = missingRecipes.size
                Log.d("DEBUG DOWNLOAD", "missingRecipesCount: " + missingRecipesCount)

                var addededRecipes = 0

                for(remoteRecipe in allRecipes){

                    if(missingRecipesCount == 0){
                        onComplete()
                        return@getAllRecipesForUser
                    }

                    //Log.d("DEBUG DOWNLOAD", "missingRecipesCount: " + missingRecipesCount)



                    //Log.d("DEBUG DOWNLOAD", "TEST3")
                    if (remoteRecipe.id !in recipesList.map { it.remoteId }) {
                    //nema ga u lokalnim, skini sve ostale stvari i ubaci ga ubaci ga
                    //dohvati korake

                        getAllStepsForRecipe(remoteRecipe.id){success, allSteps ->
                            //Log.d("DEBUG DOWNLOAD", "TEST4: " + success + " " + allSteps.isEmpty())
                            if(!success || allSteps.isEmpty()){
                                //Log.d("DEBUG DOWNLOAD", "TEST4 EXIT")
                                addededRecipes++
                                //Log.d("DEBUG DOWNLOAD", "addededRecipes: " + addededRecipes)
                                return@getAllStepsForRecipe
                            }

                            //dohvati sastojke
                            fetchIngredientsByRecipeId(remoteRecipe.id) { sucess, allIngredients ->
                                if (!sucess || allIngredients.isEmpty()) {
                                    addededRecipes++
                                    //Log.d("DEBUG DOWNLOAD", "addededRecipes: " + addededRecipes)
                                    return@fetchIngredientsByRecipeId
                                }

                                var newLocalRecipeId : Int = -1

                                val idLock = Any()

                                CoroutineScope(Dispatchers.IO).launch {

                                    //sve je dohvaćeno


                                        roomDatabase.runInTransaction {
                                            val maxLocalId = roomDatabase.daoMyRecipes.getMaxRecipeId() ?: 0
                                            newLocalRecipeId = maxLocalId + 1
                                            //Log.d("DEBUG DOWNLOAD", "newLocalRecipeId: " + newLocalRecipeId + " recipe: " + remoteRecipe.name)

                                            var localRecipeImagePath = ""
                                            if (remoteRecipe.picturePath != null) {
                                                val imagePath =
                                                    parseRemoteImagePath(remoteRecipe.picturePath)
                                                //Log.d("DEBUG DOWNLOAD", "TEST 2 imagePath: " + imagePath)
                                                val filename = "${remoteRecipe.id}"
                                                localRecipeImagePath =
                                                    downloadImageLocallyWithOkHttp(
                                                        imagePath,
                                                        filename
                                                    ).toString()
                                            }
                                            //Log.d("DEBUG DOWNLOAD", "localRecipeImagePath: " + localRecipeImagePath)

                                            //ubaci recept
                                            val newRecipe = MyRecipes(
                                                id = newLocalRecipeId,
                                                name = remoteRecipe.name,
                                                category = remoteRecipe.category,
                                                description = remoteRecipe.description,
                                                id_user = remoteRecipe.id_user,
                                                makingTime = remoteRecipe.makingTime,
                                                picturePaths = localRecipeImagePath,
                                                remoteId = remoteRecipe.id
                                            )
                                            roomDatabase.daoMyRecipes.insertRecipes(newRecipe)
                                            recipesList.add(
                                                roomDatabase.daoMyRecipes.getOneRecipe(
                                                    newLocalRecipeId
                                                )
                                            )
                                            //Log.d("DEBUG DOWNLOAD", "inserted: " + newRecipe.name)
                                        }


                                    //ubaci korake

                                    for (step in allSteps) {
                                        var localStepImagePath = ""

                                        if (step.image_path != null) {
                                            val imagePath =
                                                parseRemoteImagePath(step.image_path)
                                            //Log.d("IMAGE DEBUG", imagePath)
                                            val filename =
                                                "${remoteRecipe.id}-${step.stage}"
                                            localStepImagePath =
                                                downloadImageLocallyWithOkHttp(
                                                    imagePath,
                                                    filename
                                                ).toString()
                                        }


                                        val newStep = MySteps(
                                            name = step.name,
                                            description = step.description!!,
                                            makingTime = step.making_time,
                                            idRecipe = newLocalRecipeId,
                                            stage = step.stage,
                                            imagePath = localStepImagePath
                                        )
                                        roomDatabase.daoMySteps.insertSteps(newStep)
                                    }

                                    //ubaci sastojke
                                    for (ingredient in allIngredients) {
                                        val localIngredient =
                                            roomDatabase.daoIngredient.getIngredientByName(
                                                ingredient.ingredientName
                                            )
                                        val localUnit =
                                            roomDatabase.daoUnits.getUnitByName(ingredient.unitName)

                                        val newIgredient = MyRecipeIngredients(
                                            idRecipe = newLocalRecipeId,
                                            idIngredient = localIngredient?.id!!,
                                            idUnit = localUnit?.id!!,
                                            quantity = ingredient.quantity

                                        )
                                        roomDatabase.daoMyRecipeIngredients.insert(
                                            newIgredient
                                        )
                                    }

                                    withContext(Dispatchers.Main) {
                                        addededRecipes++

                                        Log.d("DEBUG DOWNLOAD", "addededRecipes: " + addededRecipes)
                                        adapter.notifyDataSetChanged()
                                        if(addededRecipes == missingRecipesCount){
                                            Log.d("DEBUG DOWNLOAD", "addededRecipes == missingRecipesCount")
                                            onComplete()
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadRecipesFromDatabase()
        //Log.d("RESUME DEBUG","RESUMED")


    }

    private fun loadRecipesFromDatabase() {

        if(id_user != null){
            CoroutineScope(Dispatchers.IO).launch {
                val db = RecipesDatabase.getInstance()
                val recipes = db.daoMyRecipes.getRecipesByUserId(id_user!!)
                //Log.d("RESUME DEBUG","db.daoMyRecipes.getRecipes()")

                withContext(Dispatchers.Main) {
                    recipesList.clear()
                    recipesList.addAll(recipes)
                    //Log.d("RESUME DEBUG","recipesList: " + recipesList)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun deleteRecipe(recipe: MyRecipes) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = RecipesDatabase.getInstance()

            if(recipe.remoteId != null && recipe.remoteId != -1){
                val filesToDelete : MutableList<String> = mutableListOf()
                filesToDelete.add("downloadedImages/${recipe.remoteId}.jpg")
                filesToDelete.add("downloadedImages/${recipe.remoteId}-1.jpg")
                filesToDelete.add("downloadedImages/${recipe.remoteId}-2.jpg")
                filesToDelete.add("downloadedImages/${recipe.remoteId}-3.jpg")

                for(fileName in filesToDelete){
                    val file = File(requireContext().filesDir, fileName)
                    if (file.exists()) {
                        val deleted = file.delete()
                        if (deleted) {
                            Log.d("DEBUG", "Slika je uspješno obrisana.")
                        } else {
                            Log.d("DEBUG", "Brisanje slike nije uspjelo.")
                        }
                    }
                }
            }

            db.daoMyRecipes.deleteRecipe(recipe)

            withContext(Dispatchers.Main) {
                loadRecipesFromDatabase()
            }
        }
    }

    private fun editRecipe(recipe: MyRecipes) {
        val intent = Intent(requireContext(), AddRecipeActivity::class.java)
        intent.putExtra("RECIPE_ID", recipe.id)
        startActivity(intent)
    }

    private fun openMyRecipe(recipe: MyRecipes) {
        val fragment = MyDetailRecipeFragment().apply {
            arguments = Bundle().apply {
                putInt("id", recipe.id!!)
                putString("name", recipe.name)
                putString("description", recipe.description)
                putString("picturePath", recipe.picturePaths)
                putLong("makingTime", recipe.makingTime)
                putString("category", recipe.category)
                putInt("remoteId", recipe.remoteId ?: -1)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Add.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddRecipeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun parseRemoteImagePath(imagePath : String) : String{
        return WsChefy.IMAGE_BASE_URL + imagePath
    }


    private fun getAllRecipesForUser(id_user: Int, callback : (Boolean, List<RecipeItem>)-> Unit) {
        WsChefy.recipeService.getRecipeByUserId(id_user).enqueue(
            object : Callback<RecipeItemResponse> {
                override fun onResponse(
                    call: Call<RecipeItemResponse>,
                    response: retrofit2.Response<RecipeItemResponse>
                ) {
                    if (response.isSuccessful) {

                        val recipes = response.body()?.results
                        Log.d("GET RECIPE BY ID", "recipe: " + recipes)
                        if(recipes != null){
                            callback(true, recipes)
                        }
                        else{
                            callback(false, emptyList())
                        }



                    } else {
                        Log.d("GET RECIPE BY ID", "notSuccessful")
                        callback(false, emptyList())
                    }
                }

                override fun onFailure(call: Call<RecipeItemResponse>, t: Throwable) {
                    Log.d("GET RECIPE BY ID", "onFailure")
                    callback(false, emptyList())
                }

            }
        )
    }

    private fun getAllStepsForRecipe(id_recipe : Int,callback: (Boolean, List<StepRemote>) -> Unit) {
        WsChefy.stepsService.getAllStepsForRecipe(id_recipe).enqueue(
            object : Callback<GetStepsRemoteResponse> {
                override fun onResponse(
                    call: Call<GetStepsRemoteResponse>,
                    response: retrofit2.Response<GetStepsRemoteResponse>
                ) {
                    if (response.isSuccessful) {
                        val allFetchedSteps  = response.body()?.steps ?: emptyList()

                        if(allFetchedSteps.isEmpty()){
                            callback(false, emptyList())
                        }else{
                            callback(true, allFetchedSteps)
                        }


                    } else {
                        //Log.e("TEST DEBUG", "Greška u odgovoru: ${response.code()}")
                        callback(false, emptyList())
                    }
                }

                override fun onFailure(call: Call<GetStepsRemoteResponse>, t: Throwable) {
                    callback(false, emptyList())
                }

            }
        )
    }

    private fun fetchIngredientsByRecipeId(
        recipeId: Number,
        callback: (Boolean, List<RecipeIngredientItem>) -> Unit
    ) {
        WsChefy.recipeIngredientService.getIngredientsByRecipeId(recipeId.toInt()).enqueue(
            object : Callback<RecipeIngredientResponse> {
                override fun onResponse(
                    call: Call<RecipeIngredientResponse>,
                    response: retrofit2.Response<RecipeIngredientResponse>
                ) {
                    if (response.isSuccessful) {
                        val ingredients = response.body()?.results ?: emptyList()

                        Log.d("RecipeIngredients", "Dohvaćeni sastojci: $ingredients")
                        callback(true, ingredients)
                    } else {
                        Log.e("RecipeIngredients", "Greška u odgovoru: ${response.code()}")
                        callback(false, emptyList())
                    }
                }

                override fun onFailure(call: Call<RecipeIngredientResponse>, t: Throwable) {
                    Log.e("RecipeIngredients", "Greška u dohvaćanju podataka: ${t.message}")
                    callback(false, emptyList())
                }
            }
        )
    }


    fun downloadImageLocallyWithOkHttp(remoteImageUrl: String, fileName: String): String? {
        val request = Request.Builder()
            .url(remoteImageUrl)
            .build()

        try {
            // Koristi WsChefy.client koji već sadrži Basic Auth
            val response: Response = WsChefy.client.newCall(request).execute()

            // Provjeri status odgovora
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }

            // Ako je uspješno preuzeta slika, pohranjuj je lokalno
            val inputStream: InputStream = response.body()?.byteStream() ?: return null
            val file = File(requireContext().filesDir, "downloadedImages/$fileName.jpg")

            // Kreiraj direktorij ako ne postoji
            file.parentFile?.mkdirs()

            val outputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } != -1) {
                outputStream.write(buffer, 0, length)
            }

            // Zatvori streamove
            outputStream.close()
            inputStream.close()

            return file.absolutePath  // Vraća putanju do lokalno pohranjene slike
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("DEBUG DOWNLOAD", "error: " + e.message)
        }
        return null
    }

}