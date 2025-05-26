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
import androidx.activity.OnBackPressedCallback
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

class FirstStepFragment : Fragment() {

    private var recipeName: String? = null
    private var recipeDescription: String? = null
    private var recipeId: Int? = null
    private val db = RecipesDatabase.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_first_step, container, false)

        recipeId = arguments?.getInt("recipe_id")
        recipeName = arguments?.getString("recipe_name")
        recipeDescription = arguments?.getString("recipe_description")

        recipeId?.let {
            fetchFirstStepData(it)
        }

        val backButton: Button = view.findViewById(R.id.btn_back_step1)
        backButton.setOnClickListener {
            openMainActivity()
        }

        val button: Button = view.findViewById(R.id.btn_next_step1)
        button.setOnClickListener {
            val secondFragment = SecondStepFragment()

            val bundle = Bundle()
            bundle.putInt("recipeId", recipeId!!)
            secondFragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, secondFragment)
                .addToBackStack(null)
                .commit()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                openMainActivity()
            }
        })

        return view
    }

    private fun openMainActivity() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        startActivity(intent)
    }

    private fun fetchFirstStepData(recipeId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val steps = db.daoRec.getStepsForRecipeSorted(recipeId)
            val recipe = db.daoRec.getOneRecipe(recipeId)

            if (steps.isNotEmpty()) {
                val firstStep = steps.first()
                withContext(Dispatchers.Main) {
                    displayStepDetails(firstStep, recipe)
                }
            } else {
                Log.e("FirstStepFragment", "No steps found for Recipe ID $recipeId.")
            }
        }
    }

    private fun displayStepDetails(step: Steps, recipe: Recipes) {
        val textViewStepTitle: TextView = requireView().findViewById(R.id.title_first_step)
        val textViewStepTime: TextView = requireView().findViewById(R.id.step_time_first_step)
        val textViewStepName: TextView = requireView().findViewById(R.id.stageName_first_step)
        val textViewStepDescription: TextView = requireView().findViewById(R.id.description_first_step)
        val textViewStepDetails: TextView = requireView().findViewById(R.id.desc_preparation_first_step)
        val imageViewStep: ImageView = requireView().findViewById(R.id.image_first_step)

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
            imageViewStep.setImageResource(R.drawable.first_step_image)
        }

        if(seconds!=0){
            textViewStepTime.text = "$minutes minuta i $seconds sekundi!"
        }
        else{
            textViewStepTime.text = "$minutes minuta!"
        }
    }
}
