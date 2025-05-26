package hr.foi.rampu.chefy.fragments

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import hr.foi.rampu.chefy.MainActivity
import hr.foi.rampu.chefy.R
import hr.foi.rampu.chefy.models.recipe.Recipes
import hr.foi.rampu.chefy.data.db.RecipesDatabase
import hr.foi.rampu.chefy.models.recipe.Steps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ThirdStepFragment: Fragment() {
    private var recipeName: String? = null
    private var recipeDescription: String? = null
    private var recipeId: Int? = null
    private val db = RecipesDatabase.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_third_step, container, false)

        recipeId = arguments?.getInt("recipeId")
        recipeName = arguments?.getString("recipe_name")
        recipeDescription = arguments?.getString("recipe_description")

        recipeId?.let {
            fetchThirdStepData(it)
        }

        val backButton: Button = view.findViewById(R.id.btn_back_step3)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val nextButton: Button = view.findViewById(R.id.btn_next_step3)
        nextButton.setOnClickListener {
            openMainActivity()
        }

        return view
    }

    private fun openMainActivity() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        startActivity(intent)
    }

    private fun fetchThirdStepData(recipeId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val steps = db.daoRec.getStepsForRecipeSorted(recipeId)
            val recipe = db.daoRec.getOneRecipe(recipeId)

            if (steps.isNotEmpty()) {
                val thirdStep = steps.last()
                withContext(Dispatchers.Main) {
                    displayStepDetails(thirdStep, recipe)
                }
            } else {
                Log.e("ThirdStepFragment", "No steps found for Recipe ID $recipeId.")
            }
        }
    }

    private fun displayStepDetails(step: Steps, recipe: Recipes) {
        val textViewStepTitle: TextView = requireView().findViewById(R.id.title_third_step)
        val textViewStepTime: TextView = requireView().findViewById(R.id.step_time_third_step)
        val textViewStepName: TextView = requireView().findViewById(R.id.stageName_third_step)
        val textViewStepDescription: TextView = requireView().findViewById(R.id.description_third_step)
        val textViewStepDetails: TextView = requireView().findViewById(R.id.desc_preparation_third_step)
        val imageViewStep: ImageView = requireView().findViewById(R.id.image_third_step)

        textViewStepName.text = "${step.stage}. ${textViewStepName.text} ${step.name}!"
        textViewStepTitle.text = recipe.name
        textViewStepDescription.text = recipe.description
        textViewStepDetails.text = step.description
        val minutes = step.makingTime / 60000
        val seconds = (step.makingTime % 60000) / 1000

        if (step.imagePath.isNotEmpty()) {
            val assetManager = requireContext().assets
            val imageStream = assetManager.open("${step.imagePath}")
            val drawable = Drawable.createFromStream(imageStream, null)
            imageViewStep.setImageDrawable(drawable)
        } else {
            imageViewStep.setImageResource(R.drawable.third_step_image)
        }

        if(seconds!=0){
            textViewStepTime.text = "$minutes minuta i $seconds sekundi!"
        }
        else{
            textViewStepTime.text = "$minutes minuta!"
        }

    }
}