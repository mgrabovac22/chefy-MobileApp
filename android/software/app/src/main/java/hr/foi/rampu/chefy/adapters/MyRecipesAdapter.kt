package hr.foi.rampu.chefy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.foi.rampu.chefy.R
import hr.foi.rampu.chefy.models.recipe.MyRecipes
import android.graphics.BitmapFactory
import android.util.Log

class MyRecipesAdapter(
    private val recipesList: MutableList<MyRecipes>,
    private val onDeleteClick: (MyRecipes) -> Unit,
    private val onEditClick: (MyRecipes) -> Unit,
    private val onRecipeClick: (MyRecipes) -> Unit
) : RecyclerView.Adapter<MyRecipesAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeImage: ImageView = itemView.findViewById(R.id.recipeImage)
        val recipeName: TextView = itemView.findViewById(R.id.recipeName)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        val icPublish: ImageView = itemView.findViewById(R.id.icPublish)

        fun bind(recipe: MyRecipes) {
            recipeName.text = recipe.name

            val bitmap = BitmapFactory.decodeFile(recipe.picturePaths)

            if (bitmap != null) {
                recipeImage.setImageBitmap(bitmap)
            } else {
                recipeImage.setImageResource(R.drawable.food_picture)
            }

            if(recipe.remoteId != null && recipe.remoteId != -1){
                icPublish.setImageResource(R.drawable.ic_public)
                //Log.d("RESUME DEBUG", "PUBLIC: " + recipe.remoteId)
            }
            else{
                icPublish.setImageResource(R.drawable.ic_lock)
            }

            btnDelete.setOnClickListener { onDeleteClick(recipe) }
            btnEdit.setOnClickListener { onEditClick(recipe) }
            itemView.setOnClickListener { onRecipeClick(recipe) }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.added_recipe_item, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipesList[position])
    }

    override fun getItemCount(): Int = recipesList.size
}
