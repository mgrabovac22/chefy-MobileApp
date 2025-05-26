package hr.foi.rampu.chefy

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hr.foi.rampu.chefy.adapters.FavoritesAdapter
import hr.foi.rampu.chefy.data.dao.FavouritesDAO
import hr.foi.rampu.chefy.data.db.RecipesDatabase
import hr.foi.rampu.chefy.models.recipe.Favourites
import hr.foi.rampu.chefy.models.recipe.FavouritesWithRecipes
import hr.foi.rampu.chefy.models.recipe.Recipes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavedFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_saved, container, false)

        RecipesDatabase.buildInstance(requireContext())

        val database = RecipesDatabase.getInstance()
        val favouritesDAO = database.daoFavourites

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewFavorites)

        lifecycleScope.launch {
            insertSampleFavourites(favouritesDAO)
            val favouritesWithRecipes = fetchFavouritesWithRecipes(favouritesDAO)

            val recipeList = favouritesWithRecipes.map { it.recipe }

            withContext(Dispatchers.Main) {
                val adapter = FavoritesAdapter(
                    recipeList = recipeList,
                    onDeleteClick = { recipe -> handleDelete(favouritesDAO, recipe) },
                    onRecipeClick = { recipe ->
                        val bundle = Bundle().apply {
                            putInt("recipe_id", recipe.id ?: -1)
                            putString("recipe_name", recipe.name)
                            putString("recipe_picture", recipe.picturePaths)
                            putString("recipe_desc", recipe.description)
                        }

                        val detailRecipe = DetailRecipeFragment().apply {
                            arguments = bundle
                        }

                        parentFragmentManager.beginTransaction()
                            .replace(R.id.frame_layout, detailRecipe)
                            .addToBackStack(null)
                            .commit()
                    }
                )

                recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

                recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                        outRect.set(16, 16, 16, 16)
                    }
                })

                recyclerView.adapter = adapter
            }
        }

        return view
    }

    private suspend fun insertSampleFavourites(favouritesDAO: FavouritesDAO) {
        withContext(Dispatchers.IO) {
            val sampleFavourites = listOf(
                Favourites(idRecipe = 11),
                Favourites(idRecipe = 22),
                Favourites(idRecipe = 33),
                Favourites(idRecipe = 3),
                Favourites(idRecipe = 14),
                Favourites(idRecipe = 7),
                Favourites(idRecipe = 49)
            )

            if (favouritesDAO.getAllFavouritesWithRecipes().isEmpty()) {
                sampleFavourites.forEach { favouritesDAO.insertFavourites(it) }
            }
        }
    }

    private suspend fun fetchFavouritesWithRecipes(favouritesDAO: FavouritesDAO): List<FavouritesWithRecipes> {
        return withContext(Dispatchers.IO) {
            favouritesDAO.getAllFavouritesWithRecipes()
        }
    }

    private fun handleDelete(favouritesDAO: FavouritesDAO, recipe: Recipes) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val favourite = favouritesDAO.getAllFavouritesWithRecipes()
                    .find { it.recipe.id == recipe.id }?.favourite

                favourite?.let {
                    favouritesDAO.deleteFavourite(it)
                }
            }

            val updatedFavouritesWithRecipes = fetchFavouritesWithRecipes(favouritesDAO)
            val updatedRecipeList = updatedFavouritesWithRecipes.map { it.recipe }

            withContext(Dispatchers.Main) {
                val recyclerView = requireView().findViewById<RecyclerView>(R.id.recyclerViewFavorites)
                val adapter = recyclerView.adapter as? FavoritesAdapter
                adapter?.updateData(updatedRecipeList)

            }
        }
    }
}
