package hr.foi.rampu.chefy.activities

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hr.foi.rampu.chefy.R
import hr.foi.rampu.chefy.adapters.AvatarCategoryAdapter
import hr.foi.rampu.chefy.adapters.AvatarOptionAdapter
import hr.foi.rampu.chefy.models.avatar.AvatarCategoriesData
import hr.foi.rampu.chefy.models.avatar.AvatarCategory
import hr.foi.rampu.chefy.models.avatar.AvatarOption
import hr.foi.rampu.chefy.ws.response.AvatarOptionCategoriesResponseGet
import hr.foi.rampu.chefy.ws.items.AvatarOptionCategoryItem
import hr.foi.rampu.chefy.ws.items.AvatarOptionItem
import hr.foi.rampu.chefy.ws.response.AvatarOptionResponseGet
import hr.foi.rampu.chefy.ws.response.AvatarOptionResponsePut
import hr.foi.rampu.chefy.ws.WsChefy
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AvatarEditorActivity : AppCompatActivity() {

    private lateinit var selectedCategory: AvatarCategory
    private lateinit var avatarCategoryAdapter: AvatarCategoryAdapter
    private lateinit var optionAdapter: AvatarOptionAdapter

    private val selectedOptions = mutableMapOf<String, String>()
    private var selectedOption: AvatarOption? = null

    private lateinit var loadingCircle: ProgressBar
    private lateinit var errorMessageTextView: TextView
    private lateinit var contentContainer: ConstraintLayout

    private var user_id : Int? = null

    private var hasUnsavedChanges : Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_avatar_editor)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)

        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        var user_id_string = sharedPreferences.getString("user_id", null)
        user_id = user_id_string?.toInt()
        //Toast.makeText (this, user_id.toString(), Toast.LENGTH_SHORT).show()

        AvatarCategoriesData.buildInstance(this)
        val avatarCategories = AvatarCategoriesData.getInstance()

        loadingCircle = findViewById(R.id.pbRecipesFragmentLoading)
        errorMessageTextView = findViewById(R.id.tvErrorMessage)
        contentContainer = findViewById(R.id.contentContainer)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnBack = findViewById<Button>(R.id.btnBack)



        val optionsRecyclerView = findViewById<RecyclerView>(R.id.rvOptions)
        val recyclerViewCategories = findViewById<RecyclerView>(R.id.rvCategories)
        getSavedOptions()



        selectedCategory = avatarCategories.first()
        //selectedOption = selectedCategory.options.find{ it.name == selectedOptions[selectedCategory.name]}


        avatarCategoryAdapter = AvatarCategoryAdapter(this,avatarCategories,selectedCategory) { newSelectedCategory ->
            //Toast.makeText(this, "CLICK", Toast.LENGTH_SHORT).show()
            selectedCategory = newSelectedCategory

            selectedOption = selectedCategory.options.find { it.name == selectedOptions[selectedCategory.name]}

            avatarCategoryAdapter.setSelectedCategory(newSelectedCategory) //obavijesti adaptera da je promijenjena kategorija
            updateOptionsForCategory(selectedCategory)
        }
        recyclerViewCategories.adapter = avatarCategoryAdapter
        recyclerViewCategories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


        optionAdapter = AvatarOptionAdapter(this, selectedCategory.options , selectedOption) { newSelectedOption ->

            if(selectedOption != newSelectedOption){
                showHasChanges()
            }


            selectedOption = newSelectedOption
            selectedOptions[selectedCategory.name] = newSelectedOption.name
            updateAvatarPreview(selectedCategory, newSelectedOption)

        }
        optionsRecyclerView.adapter = optionAdapter
        optionsRecyclerView.layoutManager = GridLayoutManager(this, 3)


        //odaberi početnu kategoriju
        avatarCategoryAdapter.setSelectedCategory(selectedCategory)
        updateOptionsForCategory(selectedCategory)


        //val iconSave = findViewById<ImageView>(R.id.iconChanges)
        btnSave.setOnClickListener{
            fetchAvatarOptionCategories{ avatarOptionCategories, success ->
                if(!success){
                    return@fetchAvatarOptionCategories
                }

                val avatarOptionsPostList : MutableList<AvatarOptionItem> = mutableListOf()

                selectedOptions.forEach { selectedOption ->

                    val categoryId =  avatarOptionCategories.find { category -> category.name == selectedOption.key }?.id

                    if(categoryId != null && user_id != null){
                        val option  = AvatarOptionItem( id_user = user_id!!, id_avatarOptionCategory = categoryId, name = selectedOption.value)
                        //Add to list
                        avatarOptionsPostList.add(option)
                    }
                }
                updateAvatarOptions(avatarOptionsPostList){sucess ->

                    if(!sucess){
                        Toast.makeText(this, "Greška! Pokušajte ponovo.", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        saveAvatarOptionsToSharedPreferences()

                        showSavedChanges()



                        //Toast.makeText(this, "Spremljeno!", Toast.LENGTH_SHORT).show()
                    }


                }


            }
        }

        btnBack.setOnClickListener{
            onBackPressed()
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onBackPressed() {
        // Provjeri ako postoje nespremljene promjene (npr., varijabla koja to označava)
        if (hasUnsavedChanges) {
            // Prikazivanje dijaloškog okvira
            AlertDialog.Builder(this)
                .setMessage("Imate neželjene promjene, jeste li sigurni da želite izaći?")
                .setCancelable(false)
                .setPositiveButton("Da") { _, _ ->
                    // Zatvori aktivnost
                    super.onBackPressed()
                }
                .setNegativeButton("Ne") { dialog, _ ->
                    // Zatvori dijalog bez zatvaranja aktivnosti
                    dialog.dismiss()
                }
                .create()
                .show()
        } else {
            // Ako nema nespremljenih promjena, samo pozovi super.onBackPressed()
            super.onBackPressed()
        }
    }


    private fun showHasChanges() {
        hasUnsavedChanges = true

        val imvIcon = findViewById<ImageView>(R.id.iconChanges)
        imvIcon.setImageResource(R.drawable.ic_sync_problem_24)
        val color = Color.rgb(255, 152, 0)
        imvIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN)

        val btnSave = findViewById<Button>(R.id.btnSave)
        btnSave.visibility = View.VISIBLE

    }

    private fun showSavedChanges() {


        val imvIcon = findViewById<ImageView>(R.id.iconChanges)
        imvIcon.setImageResource(R.drawable.ic_check_circle_24)
        val color = Color.rgb(76, 175, 80)
        imvIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN)

        val btnSave = findViewById<Button>(R.id.btnSave)
        btnSave.visibility = View.GONE

        hasUnsavedChanges = false
    }


    private fun updateAvatarPreview(selectedCategory: AvatarCategory, selectedOption: AvatarOption) {

        val imageViewLayer = findViewById<ImageView>(selectedCategory.layoutViewId)
        imageViewLayer.setImageResource(selectedOption.imageResId)
        imageViewLayer.visibility = if (selectedOption.imageResId != R.drawable.avatar_option_none) View.VISIBLE else View.INVISIBLE
        saveSelectedOption(selectedCategory, selectedOption)
    }

    private fun updateOptionsForCategory(category: AvatarCategory) {
        optionAdapter.updateOptions(category.options, selectedOption)
    }





    private fun getSavedOptions(){

        if(user_id == null){
            return
        }

        fetchAvatarOptionCategories{ avatarOptionCategories, success ->

            if(!success){
                return@fetchAvatarOptionCategories
            }

            fetchAvatarOptionsByUserId(user_id!!) { avatarOptions, sucess ->

                if(!sucess){
                    return@fetchAvatarOptionsByUserId
                }

                avatarOptions.forEach { option ->
                    val categoryName =
                        avatarOptionCategories.find { category -> category.id == option.id_avatarOptionCategory }?.name
                    val optionName = option.name;
                    if (categoryName != null) {
                        //Log.d("POSTAVLJANJE:", categoryName + " " + optionName)
                        selectedOptions[categoryName] = optionName
                    }

                }
                loadSavedOptions()
            }
        }



        //DEFAULT
        /*selectedOptions["Background"] = "Background Sky"
        selectedOptions["Skin"] = "Skin 3"
        selectedOptions["Hat"] = "White Hat"
        selectedOptions["Eyes"] = "Blue Eyes"
        selectedOptions["Nose"] = "Nose 1"
        selectedOptions["Mustaches"] = "Mustache 1"
        selectedOptions["Hair"] = "None"
        selectedOptions["Mouth"] = "Smile 1"
        selectedOptions["Bow"] = "Bow Red"
        selectedOptions["Shirt"] = "Shirt White Rounded"*/
    }


    private fun saveSelectedOption(category: AvatarCategory, option: AvatarOption) {
        selectedOptions[category.name] = option.name
    }




    private fun loadSavedOptions() {

        if(selectedOptions.isNotEmpty()){
            val avatarCategories = AvatarCategoriesData.getInstance()

            selectedOptions.forEach { (categoryName, optionName) ->
                val category : AvatarCategory? = avatarCategories.find { it.name == categoryName}
                if(category != null){
                    val option : AvatarOption? = category.options.find { it.name == optionName}
                    if(option != null){
                        updateAvatarPreview(category, option)
                    }
                }
            }

            showSavedChanges()

            selectedOption = selectedCategory.options.find{ it.name == selectedOptions[selectedCategory.name]}
            updateOptionsForCategory(selectedCategory)
            loadingCircle.visibility = View.GONE
            contentContainer.visibility = View.VISIBLE

        }
        else{
            loadingCircle.visibility = View.GONE
            errorMessageTextView.visibility = View.VISIBLE
            errorMessageTextView.text = getString(R.string.loadAvatarOptionsFailure)
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

    private fun updateAvatarOptions(updatedOption: List<AvatarOptionItem>, callback:(Boolean) -> Unit) {
        WsChefy.avatarOptionService.updateAvatarOption(updatedOption).enqueue(
            object : Callback<AvatarOptionResponsePut> {
                override fun onResponse(
                    call: Call<AvatarOptionResponsePut>,
                    response: Response<AvatarOptionResponsePut>
                ) {
                    if (response.isSuccessful) {
                        //Toast.makeText(applicationContext, "SUCCESS", Toast.LENGTH_SHORT).show()

                        if(response.body()?.error != null){

                            Log.d("AvatarOptionUpdate", "error: " + response.body()?.error)

                        }

                        if(response.body()?.message != null){

                            Log.d("AvatarOptionUpdate", "message: " + response.body()?.message)

                        }
                        //Log.d("AvatarOptionUpdate", "Avatar opcija uspješno ažurirana!")


                        callback(true);
                    } else {

                        //Toast.makeText(applicationContext, "ERROR", Toast.LENGTH_SHORT).show()
                        Log.e("AvatarOptionUpdate", "Greška pri ažuriranju avatar opcije: ${response.code()}")
                        callback(false);
                    }
                }

                override fun onFailure(call: Call<AvatarOptionResponsePut>, t: Throwable) {

                    //Toast.makeText(applicationContext, "Failure", Toast.LENGTH_SHORT).show()
                    Log.e("AvatarOptionUpdate", "Greška: ${t.message}")
                    callback(false);
                }
            }
        )
    }

    private fun saveAvatarOptionsToSharedPreferences() {
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        selectedOptions.forEach { (categoryName, optionName) ->
            var key = "avatar-" + categoryName
            editor.putString(key, optionName)
        }

        editor.putBoolean("avatarFetch", true)

        editor.apply()
    }


}