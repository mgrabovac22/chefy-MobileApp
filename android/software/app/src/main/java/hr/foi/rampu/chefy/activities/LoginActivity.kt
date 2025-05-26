package hr.foi.rampu.chefy.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import hr.foi.rampu.chefy.LoginFragment
import hr.foi.rampu.chefy.MainActivity
import hr.foi.rampu.chefy.R
import hr.foi.rampu.chefy.models.recipe.Ingredients
import hr.foi.rampu.chefy.models.recipe.RecipeIngredient
import hr.foi.rampu.chefy.models.recipe.Recipes
import hr.foi.rampu.chefy.data.db.RecipesDatabase
import hr.foi.rampu.chefy.models.recipe.Steps
import hr.foi.rampu.chefy.models.recipe.Units
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Log.d("LoginActivity", "onCreate called")
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)

        RecipesDatabase.buildInstance(applicationContext)
        populateDatabase()

        if (isUserLoggedIn()) {
            Log.d("LoginActivity", "User is logged in. Navigating to MainActivity.")
            navigateToMainActivity()
        } else if (savedInstanceState == null) {
            Log.d("LoginActivity", "No active session. Loading LoginFragment.")
            replaceFragment(LoginFragment())
        }

        /*val deepLinkRemoteRecipeId = intent.getStringExtra("deepLinkRemoteRecipeId")
        if (deepLinkRemoteRecipeId != null) {
            Toast.makeText(this, "DEEP LINK: " + deepLinkRemoteRecipeId, Toast.LENGTH_SHORT).show()
        }*/
    }

    private fun isUserLoggedIn(): Boolean {
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val sessionExpiry = sharedPreferences.getLong("session_expiry", -1)
        return sessionExpiry > System.currentTimeMillis()
    }

    fun saveSession() {
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
       val expiryTime = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000) // 7 dana
      //  val expiryTime = System.currentTimeMillis() + (1 * 60 * 1000)  //1 min

        with(sharedPreferences.edit()) {
            putLong("session_expiry", expiryTime)
            apply()
        }
        Log.d("LoginActivity", "Session saved. Expires on: $expiryTime")
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun replaceFragment(fragment: Fragment) {

        val deepLinkRemoteRecipeId = intent.getStringExtra("deepLinkRemoteRecipeId")
        //Toast.makeText(this, "DEEP LINK: " + deepLinkRemoteRecipeId, Toast.LENGTH_SHORT).show()
        if (deepLinkRemoteRecipeId != null) {
            val bundle = Bundle().apply {
                putString("deepLinkRemoteRecipeId", deepLinkRemoteRecipeId)
            }
            fragment.arguments = bundle

        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun populateDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                insertRecipesFirst()
                insertStepsFirst()
                insertUnitsFirst()
                insertIngredientsFirst()
                insertRecipeIngredientsFirst()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error populating database: ${e.message}")
            }
        }
    }

    private suspend fun insertRecipesFirst() {
        val db = RecipesDatabase.getInstance()
        withContext(Dispatchers.IO) {
            if (db.daoRec.getRecipesCount() >= 50) {
                Log.d("InsertRecipesFirst", "Recipes already populated")
                return@withContext
            }

            val file = inputStreamToFile(assets.open("recipes.csv"), "recipes.file")
            file.forEachLine { line ->
                val tokens = line.split("%%%")
                if (tokens.size == 5) {
                    val rec = Recipes(null, tokens[0], tokens[1], tokens[2].toLong(), tokens[3], tokens[4])
                    Log.i("aaa", rec.toString())
                    db.daoRec.insertRecipes(
                        rec
                    )
                } else {
                    Log.e("InsertRecipesFirst", "Invalid recipe format: $line")
                }
            }
            Log.d("InsertRecipesFirst", "Recipes inserted successfully")
        }
    }

    private suspend fun insertStepsFirst() {
        val db = RecipesDatabase.getInstance()
        withContext(Dispatchers.IO) {
            if (db.daoSteps.getCountSteps() >= 150) {
                Log.d("InsertStepsFirst", "Steps already populated")
                return@withContext
            }

            val file = inputStreamToFile(assets.open("steps.csv"), "steps.file")
            file.forEachLine { line ->
                val tokens = line.split("%%%")
                if (tokens.size == 6) {
                    db.daoSteps.insertSteps(
                        Steps(null, tokens[0], tokens[1], tokens[2].toInt(), tokens[3].toInt(), tokens[4], tokens[5].toInt())
                    )
                } else {
                    Log.e("InsertStepsFirst", "Invalid step format: $line")
                }
            }
            Log.d("InsertStepsFirst", "Steps inserted successfully")
        }
    }

    private suspend fun insertUnitsFirst() {
        val db = RecipesDatabase.getInstance()
        withContext(Dispatchers.IO) {
            if (db.daoUnits.getUnitsCount() >= 10) {
                Log.d("InsertUnits", "Units already populated")
                return@withContext
            }

            val file = inputStreamToFile(assets.open("units.csv"), "units.file")
            file.forEachLine { line ->
                val tokens = line.split("%%%")
                if (tokens.size == 2) {
                    db.daoUnits.insertUnit(
                        Units(tokens[0].toInt(), tokens[1])
                    )
                } else {
                    Log.e("InsertUnitsFirst", "Invalid unit format: $line")
                }
            }
            Log.d("UnitsInserted", "Units inserted successfully")
        }
    }

    private suspend fun insertIngredientsFirst() {
        val db = RecipesDatabase.getInstance()
        withContext(Dispatchers.IO) {
            if (db.daoIngredient.getIngCount() >= 200) {
                Log.d("InsertIngredients", "Ingredients already populated")
                return@withContext
            }

            val file = inputStreamToFile(assets.open("ingredients.csv"), "ingredients.file")
            file.forEachLine { line ->
                val tokens = line.split("%%%")
                if (tokens.size == 2) {
                    db.daoIngredient.insertIngredient(
                        Ingredients(tokens[0].toInt(), tokens[1])
                    )
                } else {
                    Log.e("InsertIngredientsFirst", "Invalid ingredient format: $line")
                }
            }
            Log.d("IngredientsInserted", "Ingredient inserted successfully")
        }
    }

    private suspend fun insertRecipeIngredientsFirst() {
        val db = RecipesDatabase.getInstance()
        withContext(Dispatchers.IO) {
            if (db.daoRecipeIngredient.getRecIngCount() >= 314) {
                Log.d("RecipeIngredients", "RecipeIngredients already populated")
                return@withContext
            }

            val file = inputStreamToFile(assets.open("recipeIngredients.csv"), "recipeIngredients.file")
            file.forEachLine { line ->
                val tokens = line.split("%%%")
                if (tokens.size == 4) {
                    db.daoRecipeIngredient.insert(
                        RecipeIngredient(null,tokens[0].toInt(), tokens[1].toInt(), tokens[2].toInt(), tokens[3].toDouble())
                    )
                } else {
                    Log.e("RecipeIngredients", "Invalid ingredient format: $line")
                }
            }
            Log.d("RecipeIngredientsInserted", "Ingredient inserted successfully")
        }
    }

    private fun inputStreamToFile(inputStream: InputStream, fileName: String): File {
        val file = File(filesDir, fileName)
        if (file.exists()) {
            Log.d("InputStreamToFile", "File already exists: $fileName")
            file.delete()
        }

        FileOutputStream(file).use { output ->
            inputStream.copyTo(output)
        }
        return file
    }

}
