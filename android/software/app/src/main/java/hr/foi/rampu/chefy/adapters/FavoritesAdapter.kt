package hr.foi.rampu.chefy.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.foi.rampu.chefy.R
import hr.foi.rampu.chefy.models.recipe.Recipes
import java.io.IOException

class FavoritesAdapter(
    private var recipeList: List<Recipes>,
    private val onDeleteClick: (Recipes) -> Unit,
    private val onRecipeClick: (Recipes) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder>() {

    inner class FavoritesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeImage: ImageView = itemView.findViewById(R.id.recipeImage)
        val recipeName: TextView = itemView.findViewById(R.id.recipeName)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.favourites_recipe_item, parent, false)
        return FavoritesViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoritesViewHolder, position: Int) {
        val recipe = recipeList[position]
        holder.recipeName.text = recipe.name

        if (recipe.picturePaths.isNotEmpty()) {
            try {
                val assetManager = holder.itemView.context.assets
                val imageStream = assetManager.open(recipe.picturePaths)
                val drawable = Drawable.createFromStream(imageStream, null)
                holder.recipeImage.setImageDrawable(drawable)
            } catch (e: IOException) {
                e.printStackTrace()
                holder.recipeImage.setImageResource(R.drawable.food_picture)
            }
        } else {
            holder.recipeImage.setImageResource(R.drawable.food_picture)
        }

        holder.btnDelete.setOnClickListener { onDeleteClick(recipe) }
        holder.itemView.setOnClickListener { onRecipeClick(recipe) }
    }

    override fun getItemCount(): Int = recipeList.size

    fun updateData(newRecipeList: List<Recipes>) {
        recipeList = newRecipeList
        notifyDataSetChanged()
    }
}
