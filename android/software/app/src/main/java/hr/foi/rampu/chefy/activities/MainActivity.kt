package hr.foi.rampu.chefy

import PeriodicNotificationWorker
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import hr.foi.rampu.chefy.activities.SearchRecipesActivity
import hr.foi.rampu.chefy.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recipeId = intent.getIntExtra("recipe_id", -1)
        if (recipeId != -1) {
            val bundle = Bundle().apply {
                putInt("recipe_id", recipeId)
            }
            val detailRecipeFragment = DetailRecipeFragment().apply {
                arguments = bundle
            }
            replaceFragment(detailRecipeFragment)
        } else {
            val openFragment = intent.getStringExtra("open_fragment")
            if (openFragment == "Add") {
                replaceFragment(AddRecipeFragment())
            } else {
                replaceFragment(HomeFragment())
            }
        }

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.saved -> replaceFragment(SavedFragment())
                R.id.add -> replaceFragment(AddRecipeFragment())
                R.id.settings -> replaceFragment(SettingsFragment())
                R.id.profile -> replaceFragment(ProfileFragment())
            }
            true
        }

        val workRequest = PeriodicWorkRequestBuilder<PeriodicNotificationWorker>(
            4, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "chefy_notification_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )




        val deepLinkRemoteRecipeId = intent.getStringExtra("deepLinkRemoteRecipeId")
        //Toast.makeText(this, "DEEP LINK: " + deepLinkRemoteRecipeId, Toast.LENGTH_SHORT).show()
        if (deepLinkRemoteRecipeId != null) {
            intent.removeExtra("deepLinkRemoteRecipeId")
            val intent = Intent(this, SearchRecipesActivity::class.java)
            intent.putExtra("deepLinkRemoteRecipeId", deepLinkRemoteRecipeId)
            startActivity(intent)
        }

        val deepLinkingFailed = intent.getStringExtra("deepLinkingFailed")
        if(deepLinkingFailed != null){
            intent.removeExtra("deepLinkingFailed")
            Toast.makeText(this, "Neuspješno dohvaćanje recepta", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        applyDarkMode()
    }

    private fun applyDarkMode() {
        val darkModeEnabled = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            .getBoolean("dark_mode", false)

        if (darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        Log.d("MainActivity", "Dark Mode: $darkModeEnabled")


    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }


}
