package hr.foi.rampu.chefy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hr.foi.rampu.chefy.activities.SearchRecipesActivity
import hr.foi.rampu.chefy.adapters.RecipeAdapter
import hr.foi.rampu.chefy.models.recipe.Recipes
import hr.foi.rampu.chefy.data.db.RecipesDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var recyclerView: RecyclerView
    private val db = RecipesDatabase.getInstance()

    private val randomNumbers: List<Int> = (1..50).shuffled().take(5)

    private val randomRecipes = mutableListOf<Recipes>()
    var check = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rv_random_recipes)
        recyclerView.layoutManager = LinearLayoutManager(view.context)

        val adapter = RecipeAdapter(randomRecipes) { selectedRecipe ->
            val bundle = Bundle().apply {
                putInt("recipe_id", selectedRecipe.id ?: -1)

            }
            val detailRecipe = DetailRecipeFragment().apply {
                arguments = bundle
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, detailRecipe)
                .addToBackStack(null)
                .commit()
        }
        recyclerView.setAdapter(adapter)
        CoroutineScope(Dispatchers.IO).launch {
            if(!check){
                for (i in randomNumbers.indices) {
                    val recipe = db.daoRec.getOneRecipe(randomNumbers[i])
                    randomRecipes.add(recipe)
                    check = true
                }
            }
        }
        if (randomRecipes.isNotEmpty()) {
            recyclerView.adapter = RecipeAdapter(randomRecipes) {
                    selectedRecipe ->
                val bundle = Bundle().apply {
                    putInt("recipe_id", selectedRecipe.id ?: -1)
                    putString("recipe_name", selectedRecipe.name)
                    putString("recipe_picture", selectedRecipe.picturePaths)
                    putString("recipe_desc", selectedRecipe.description)
                }

                val detailRecipe = DetailRecipeFragment().apply {
                    arguments = bundle
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, detailRecipe)
                    .addToBackStack(null)
                    .commit()
            }
        }
        
        else {
            Log.e("MainActivity", "Recipe list is empty or contains null entries.")
        }

        val searchView = view.findViewById<SearchView>(R.id.searchView)
        searchView.clearFocus()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    searchView.clearFocus() //ovo popravi duplo otvaranje activitija

                    val intent = Intent(requireContext(), SearchRecipesActivity::class.java).apply {
                        putExtra("search_query", it)
                    }
                    startActivity(intent)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Ne radimo ništa za promjenu teksta trenutno
                return true
            }
        })

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Home.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

    }

}