package hr.foi.rampu.chefy.adapters

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.foi.rampu.chefy.R
import hr.foi.rampu.chefy.models.recipe.Recipes

class RecipeAdapter(
    private var recipeList: MutableList<Recipes>,
    private val onItemClick: (Recipes) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val recipeName: TextView = view.findViewById(R.id.tv_recipe_name)
        private val recipeMakingTime: TextView = view.findViewById(R.id.tv_time)
        private val recipePicturePath: ImageView = view.findViewById(R.id.iw_recipe_picture)

        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(recipeList[adapterPosition])
                }
            }
        }

        fun bind(recipe: Recipes) {
            recipeName.text = recipe.name
            recipeMakingTime.text = convertTime(recipe.makingTime)

            if (!recipe.picturePaths.isNullOrEmpty()) {
                    val assetManager = itemView.context.assets
                    val imageStream = assetManager.open("${recipe.picturePaths}")
                    val drawable = Drawable.createFromStream(imageStream, null)
                    recipePicturePath.setImageDrawable(drawable)
            } else {
                recipePicturePath.setImageResource(R.drawable.food_picture)
            }
        }

        private fun convertTime(makingTime: Long): String {
            val minutes = (makingTime / 60000).toInt()
            return when {
                minutes < 60 -> "${minutes}min"
                minutes % 60 == 0 -> "${minutes / 60}h"
                else -> "${minutes / 60}h ${minutes % 60}mi"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val recipeView = LayoutInflater.from(parent.context)
            .inflate(R.layout.random_recipe_item, parent, false)
        return RecipeViewHolder(recipeView)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        Log.d("RecipeAdapter", "Binding recipe at position: $position")
        Log.e("Recipelist", "LISt: ${recipeList}")
        holder.bind(recipeList[position])
    }

    override fun getItemCount(): Int = recipeList.size

}
