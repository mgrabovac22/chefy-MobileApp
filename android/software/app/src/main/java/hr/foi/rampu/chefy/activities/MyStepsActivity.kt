package hr.foi.rampu.chefy.activities

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import hr.foi.rampu.chefy.R
import hr.foi.rampu.chefy.databinding.ActivityStepsBinding
import hr.foi.rampu.chefy.fragments.MyFirstStepFragment

class MyStepsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStepsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStepsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)

        val recipeName = intent.getStringExtra("recipe_name")
        val recipeDescription = intent.getStringExtra("recipe_description")
        val recipeAuthor = intent.getStringExtra("recipe_author")
        val recipeId = intent.getIntExtra("recipe_id", 1)
        val firstStepFragment = MyFirstStepFragment().apply {
            val bundle = Bundle().apply {
                putInt("id", recipeId)
                putString("recipe_name", recipeName)
                putString("recipe_description", recipeDescription)
                putString("recipe_author", recipeAuthor)
            }
            arguments = bundle
        }

        if (savedInstanceState == null) {
            val firstStepFragment = MyFirstStepFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, firstStepFragment)
                .addToBackStack(null)
                .commit()
        }

        replaceFragment(firstStepFragment)


    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }
}
