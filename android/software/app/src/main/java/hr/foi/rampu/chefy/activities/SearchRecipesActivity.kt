package hr.foi.rampu.chefy.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hr.foi.rampu.chefy.MainActivity
import hr.foi.rampu.chefy.R
import hr.foi.rampu.chefy.adapters.SearchRecipeAdapter
import hr.foi.rampu.chefy.data.db.RecipesDatabase
import hr.foi.rampu.chefy.fragments.DetailRecipeRemoteFragment
import hr.foi.rampu.chefy.ws.items.RecipeItem
import hr.foi.rampu.chefy.ws.response.RecipeResponse
import hr.foi.rampu.chefy.ws.WsChefy
import hr.foi.rampu.chefy.ws.response.AvatarOptionResponseGet
import hr.foi.rampu.chefy.ws.response.RecipeItemResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchRecipesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchRecipeAdapter
    private lateinit var loadingCircle: ProgressBar
    private lateinit var errorMessageTextView: TextView
    private lateinit var filterButton: ImageView
    private var query: String = ""

    private val recipes = mutableListOf<RecipeItem>()

    private var wasDeepLinked : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_recipes)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)


        val intent = intent
        val data = intent.data


        val deepLinkRemoteRecipeId = intent.getStringExtra("deepLinkRemoteRecipeId")
        //Toast.makeText(this, "DEEP LINK: " + deepLinkRemoteRecipeId, Toast.LENGTH_SHORT).show()
        //Log.d("DEBUG OTVARANJA", "TEST-1")
        //provjeri deep link
        if (data != null && data.scheme == "chefy" && data.host == "recipe") {
            intent.removeExtra("deepLinkRemoteRecipeId")
            val remoteRecipeId = data.pathSegments.getOrNull(0)

            val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
            var user_id_string = sharedPreferences.getString("user_id", null)
            if(user_id_string == null){
                //Log.d("DEBUG OTVARANJA", "TEST1")
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra("deepLinkRemoteRecipeId", remoteRecipeId)
                startActivity(intent)
                finish()
            }
            else{
                wasDeepLinked = true

                if (remoteRecipeId != null) {
                    //Log.d("DEBUG OTVARANJA", "TEST2")
                    openSharedRecipeDetails(remoteRecipeId)
                }
            }

        }
        //Provjeri ako se korisnik log inal nakon otvaranja deep linka tek
        else if (deepLinkRemoteRecipeId != null) {
            //Log.d("DEBUG OTVARANJA", "TEST0")
            wasDeepLinked = true
            openSharedRecipeDetails(deepLinkRemoteRecipeId)
        }
        else{
            findViewById<View>(R.id.search_container).visibility = View.VISIBLE
        }



        loadingCircle = findViewById(R.id.pbRecipesFragmentLoading)
        errorMessageTextView = findViewById(R.id.tvErrorMessage)

        recyclerView = findViewById(R.id.rvSearchResults)
        recyclerView.layoutManager = LinearLayoutManager(this)

        filterButton = findViewById(R.id.ivFilter)
        filterButton.setOnClickListener { showFilterPopup() }

        adapter = SearchRecipeAdapter(recipes) { recipe ->
            //Toast.makeText(this, "Odabrano je: ${recipe.name}", Toast.LENGTH_SHORT).show()


            val detailRecipe = DetailRecipeRemoteFragment.newInstance(recipe)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detailRecipe)
                .addToBackStack(null)
                .commit()

            findViewById<View>(R.id.fragment_container).visibility = View.VISIBLE
            findViewById<View>(R.id.search_container).visibility = View.GONE

        }
        recyclerView.adapter = adapter

        val searchView = findViewById<SearchView>(R.id.svSearch)

        this.query = intent.getStringExtra("search_query") ?: ""
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setText(this.query)

        performSearch(this.query)


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                if(!query.isNullOrEmpty()){

                    performSearch(query)
                }
                this@SearchRecipesActivity.query = query ?: ""

                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if(searchView.query.isEmpty()){
                    val intent = Intent(this@SearchRecipesActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                return true
            }
        })

        val searchBar = findViewById<SearchView>(R.id.svSearch)
        val closeButton = searchBar.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)

        closeButton.setOnClickListener {
            searchView.setQuery("", false)
            searchView.clearFocus()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


    }

    private fun openSharedRecipeDetails(remoteRecipeId: String) {
        //Log.d("DEBUG OTVARANJA", "PRETRAZUJEMO");
        intent.removeExtra("deepLinkRemoteRecipeId")

        getRecipeDetails(remoteRecipeId.toInt()){success, recipe ->
            //Log.d("DEBUG OTVARANJA", "recipe: " + recipe);
            if(!success || recipe == null){
                //Log.d("DEBUG OTVARANJA", "PRAZNO!");
                //ili PREBACI NA HOME SCREEN!

                RecipesDatabase.buildInstance(this)

                val intent = Intent(this, MainActivity::class.java)

                intent.putExtra("deepLinkingFailed", remoteRecipeId)

                startActivity(intent)
                finish()
                return@getRecipeDetails
            }




            val detailRecipe = DetailRecipeRemoteFragment.newInstance(recipe)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detailRecipe)
                .addToBackStack(null)
                .commit()

            findViewById<View>(R.id.fragment_container).visibility = View.VISIBLE
            findViewById<View>(R.id.search_container).visibility = View.GONE

        }



    }

    private fun getRecipeDetails(remoteRecipeId: Int, callback : (Boolean, RecipeItem?)-> Unit) {
        //Log.d("GET RECIPE BY ID", "remoteRecipeId: " + remoteRecipeId)
        WsChefy.recipeService.getRecipeById(remoteRecipeId).enqueue(
            object : Callback<RecipeItemResponse> {
                override fun onResponse(
                    call: Call<RecipeItemResponse>,
                    response: Response<RecipeItemResponse>
                ) {
                    if (response.isSuccessful) {

                        val recipes = response.body()?.results
                        Log.d("GET RECIPE BY ID", "recipe: " + recipes)
                        if(recipes != null){
                            callback(true, recipes.firstOrNull())
                        }
                        else{
                            callback(false, null)
                        }



                    } else {
                        Log.d("GET RECIPE BY ID", "notSuccessful")
                        callback(false, null)
                    }
                }

                override fun onFailure(call: Call<RecipeItemResponse>, t: Throwable) {
                    Log.d("GET RECIPE BY ID", "onFailure")
                    callback(false, null)
                }

            }
        )
    }


    private fun showFilterPopup() {
        val popupView = layoutInflater.inflate(R.layout.filter_popup, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        val searchView = findViewById<SearchView>(R.id.svSearch)

        val location = IntArray(2)
        searchView.getLocationOnScreen(location)

        popupWindow.showAtLocation(searchView, Gravity.NO_GRAVITY, 0, location[1] + searchView.height)

        val cbFilterTime = popupView.findViewById<CheckBox>(R.id.cbFilterTime)
        val etMaxTime = popupView.findViewById<EditText>(R.id.etMaxTime)
        val cbFilterCategory = popupView.findViewById<CheckBox>(R.id.cbFilterCategory)
        val spinnerCategory = popupView.findViewById<Spinner>(R.id.spinnerCategory)
        val cbFilterIngredients = popupView.findViewById<CheckBox>(R.id.cbFilterIngredients)
        val llIngredients = popupView.findViewById<LinearLayout>(R.id.llIngredients)
        val btnAddIngredient = popupView.findViewById<ImageButton>(R.id.btnAddIngredient)
        val etIngredient = popupView.findViewById<EditText>(R.id.etIngredient)
        val btnClose = popupView.findViewById<ImageView>(R.id.ivClose)

        val ingredientsList = mutableListOf<String>()

        val categories = listOf("Doručak", "Ručak", "Večera", "Desert", "Salata", "Grickalice", "Prilog")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        cbFilterTime.setOnCheckedChangeListener { _, isChecked ->
            etMaxTime.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        cbFilterCategory.setOnCheckedChangeListener { _, isChecked ->
            spinnerCategory.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        cbFilterIngredients.setOnCheckedChangeListener { _, isChecked ->
            llIngredients.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        btnAddIngredient.setOnClickListener {
            val ingredient = etIngredient.text.toString()
            if (ingredient.isNotBlank()) {
                ingredientsList.add(ingredient)
                etIngredient.text.clear()

                val ingredientLayout = LinearLayout(this)
                ingredientLayout.orientation = LinearLayout.HORIZONTAL

                val textView = TextView(this)
                textView.text = ingredient
                ingredientLayout.addView(textView)

                val removeButton = ImageView(this)
                removeButton.setImageResource(R.drawable.ic_remove)
                removeButton.setOnClickListener {
                    ingredientsList.remove(ingredient)
                    llIngredients.removeView(ingredientLayout)
                }
                ingredientLayout.addView(removeButton)

                llIngredients.addView(ingredientLayout)
            }
        }

        btnClose.bringToFront()
        btnClose.setOnClickListener {
            popupWindow.dismiss()
        }


        val refreshButton = popupView.findViewById<ImageView>(R.id.ivRefresh)

        refreshButton.bringToFront()
        refreshButton.setOnClickListener {
            cbFilterTime.isChecked = false
            cbFilterCategory.isChecked = false
            cbFilterIngredients.isChecked = false
            etMaxTime.text.clear()
            ingredientsList.clear()
            spinnerCategory.setSelection(0)
            llIngredients.removeAllViews()

            val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            searchEditText.setText(this.query)

            performSearch(this.query)
            popupWindow.dismiss()

        }


        popupView.findViewById<Button>(R.id.btnApplyFilters).setOnClickListener {
            val isTimeChecked = cbFilterTime.isChecked
            val isCategoryChecked = cbFilterCategory.isChecked
            val isIngredientsChecked = cbFilterIngredients.isChecked

            if (isTimeChecked || isCategoryChecked || isIngredientsChecked) {
                val maxTime = if (isTimeChecked) etMaxTime.text.toString() else null
                val category = if (isCategoryChecked) spinnerCategory.selectedItem?.toString() else null
                val ingredients = if (isIngredientsChecked) ingredientsList else null

                applyFilters(maxTime, category, ingredients)
                popupWindow.dismiss()
            } else {
                popupWindow.dismiss()
            }
        }

        popupWindow.showAtLocation(findViewById(R.id.main), Gravity.CENTER, 0, 0)
    }

    private fun applyFilters(maxTime: String?, category: String?, ingredients: List<String>?) {
        val queryParams = mutableMapOf<String, String>()

        val searchQuery = findViewById<SearchView>(R.id.svSearch).query.toString()
        if (searchQuery.isNotBlank()) {
            queryParams["name"] = searchQuery
        }

        if (!maxTime.isNullOrBlank()) {
            queryParams["making_time"] = maxTime
        }

        if (!category.isNullOrBlank()) {
            queryParams["category"] = category
        }

        if (!ingredients.isNullOrEmpty()) {
            queryParams["ingredients"] = ingredients.joinToString(",")
        }

        performFilteredSearch(queryParams)
    }

    private fun performFilteredSearch(queryParams: Map<String, String>) {
        changeDisplay(isLoading = true, resultsFound = true)

        WsChefy.recipeService.searchRecipesWithFilters(queryParams).enqueue(object : Callback<RecipeResponse> {
            override fun onResponse(call: Call<RecipeResponse>, response: Response<RecipeResponse>) {
                if (response.isSuccessful) {
                    recipes.clear()

                    if (response.body()?.results?.isEmpty() == true) {
                        changeDisplay(isLoading = false, resultsFound = false, message = getString(R.string.searchEmpty))
                    } else {
                        response.body()?.results?.let { recipes.addAll(it) }
                        adapter.notifyDataSetChanged()
                        changeDisplay(isLoading = false, resultsFound = true)
                    }
                } else {
                    Toast.makeText(this@SearchRecipesActivity, "Greška u pretraživanju", Toast.LENGTH_SHORT).show()
                    changeDisplay(isLoading = false, resultsFound = false, message = getString(R.string.searchFailure))
                }
            }

            override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {
                Log.e("SearchRecipesActivity", "Greška: ${t.message}")
                Toast.makeText(this@SearchRecipesActivity, "Neuspješno dohvaćanje podataka", Toast.LENGTH_SHORT).show()
                changeDisplay(isLoading = false, resultsFound = false, message = getString(R.string.dataGetFailure))
            }
        })
    }

    private fun performSearch(query: String) {
        changeDisplay(true,true)
        WsChefy.recipeService.searchRecipes(query).enqueue(object : Callback<RecipeResponse> {
            override fun onResponse(call: Call<RecipeResponse>, response: Response<RecipeResponse>) {
                if (response.isSuccessful) {
                    recipes.clear()

                    if(response.body()?.results?.isEmpty() == true){
                        changeDisplay(false,false, getString(R.string.searchEmpty))
                    }
                    else{
                        response.body()?.results?.let { recipes.addAll(it) }
                        adapter.notifyDataSetChanged()
                        changeDisplay(false,true)
                    }

                } else {
                    Toast.makeText(this@SearchRecipesActivity, "Greška u pretrazivanju", Toast.LENGTH_SHORT).show()
                    changeDisplay(false,false, getString(R.string.searchFailure))
                }

            }

            override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {
                Log.e("SearchRecipesActivity", "Greška: ${t.message}")
                Toast.makeText(this@SearchRecipesActivity, "Neuspješno dohvaćanje podataka", Toast.LENGTH_SHORT).show()
                changeDisplay(false,false, getString(R.string.dataGetFailure))
            }
        })
    }

    private fun changeDisplay(isLoading: Boolean, resultsFound : Boolean, message : String = "") {
        errorMessageTextView.isVisible = !resultsFound
        errorMessageTextView.text = message

        loadingCircle.isVisible = isLoading
        recyclerView.isVisible = !isLoading
    }

    override fun onBackPressed() {

        if(wasDeepLinked)
        {
            RecipesDatabase.buildInstance(applicationContext)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        else{
            val fragmentContainer = findViewById<View>(R.id.fragment_container)
            val searchContainer = findViewById<View>(R.id.search_container)

            if (fragmentContainer.visibility == View.VISIBLE) {
                fragmentContainer.visibility = View.GONE
                searchContainer.visibility = View.VISIBLE
            } else {
                super.onBackPressed()
            }
        }



    }

}
