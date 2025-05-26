package hr.foi.rampu.chefy.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import hr.foi.rampu.chefy.R
import hr.foi.rampu.chefy.models.recipe.MyRecipes
import hr.foi.rampu.chefy.models.recipe.MySteps
import hr.foi.rampu.chefy.data.db.RecipesDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MySecondStepFragment : Fragment() {

    private var recipeName: String? = null
    private var recipeDescription: String? = null
    private var recipeId: Int? = null
    private val db = RecipesDatabase.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_second_step, container, false)

        recipeId = arguments?.getInt("id")
        recipeName = arguments?.getString("recipe_name")
        recipeDescription = arguments?.getString("recipe_description")

        recipeId?.let {
            fetchSecondStepData(it)
        }

        val button: Button = view.findViewById(R.id.btn_next_step2)
        button.setOnClickListener {
            val thirdFragment = MyThirdStepFragment()

            val bundle = Bundle()
            bundle.putInt("id", recipeId!!)
            thirdFragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, thirdFragment)
                .addToBackStack(null)
                .commit()
        }

        val backButton: Button = view.findViewById(R.id.btn_back_step2)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }

    private fun fetchSecondStepData(recipeId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val steps = db.daoMyRecipes.getStepsForRecipeSorted(recipeId)
            val recipe = db.daoMyRecipes.getOneRecipe(recipeId)

            if (steps.isNotEmpty()) {
                if (steps.size > 1) {
                    val secondStep = steps[1]
                    withContext(Dispatchers.Main) {
                        displayStepDetails(secondStep, recipe)
                    }
                } else {
                    Log.e("secondStepFragment", "There are less than 2 steps for Recipe ID $recipeId.")
                }
            } else {
                Log.e("secondStepFragment", "No steps found for Recipe ID $recipeId.")
            }
        }
    }


    private fun displayStepDetails(step: MySteps, recipe: MyRecipes) {
        val textViewStepTitle: TextView = requireView().findViewById(R.id.title_second_step)
        val textViewStepTime: TextView = requireView().findViewById(R.id.step_time_second_step)
        val textViewStepName: TextView = requireView().findViewById(R.id.stageName_second_step)
        val textViewStepDescription: TextView = requireView().findViewById(R.id.description_second_step)
        val textViewStepDetails: TextView = requireView().findViewById(R.id.desc_preparation_second_step)
        val imageViewStep: ImageView = requireView().findViewById(R.id.image_second_step)

        textViewStepName.text = "${step.stage}. ${textViewStepName.text} ${step.name}!"
        textViewStepTitle.text = recipe.name
        textViewStepDescription.text = recipe.description
        textViewStepDetails.text = step.description
        val minutes = step.makingTime

        textViewStepTime.text = "$minutes minuta!"

        step.imagePath?.let {
            val imageUri = Uri.parse(it)
            imageViewStep.setImageURI(imageUri)
        }
    }
}
