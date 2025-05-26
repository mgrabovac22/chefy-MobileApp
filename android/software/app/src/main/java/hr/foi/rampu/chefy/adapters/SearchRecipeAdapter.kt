package hr.foi.rampu.chefy.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.foi.rampu.chefy.R
import hr.foi.rampu.chefy.ws.items.RecipeItem
import hr.foi.rampu.chefy.ws.WsChefy

class SearchRecipeAdapter(
    private val recipeList: MutableList<RecipeItem>,
    private val onItemClick: (RecipeItem) -> Unit
) : RecyclerView.Adapter<SearchRecipeAdapter.SearchRecipeViewHolder>() {

    inner class SearchRecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val recipeName: TextView = view.findViewById(R.id.tv_recipe_name)
        private val recipeAuthor: TextView = view.findViewById(R.id.tv_chef)
        private val recipeMakingTime: TextView = view.findViewById(R.id.tv_time)
        private val recipePicture: ImageView = view.findViewById(R.id.iw_recipe_picture)

        init {
            itemView.setOnClickListener {
                onItemClick(recipeList[adapterPosition])
            }
        }

        fun bind(recipe: RecipeItem) {
            recipeName.text = recipe.name
            recipeAuthor.text = recipe.username
            recipeMakingTime.text = convertTimeMinutes(recipe.makingTime)


            recipePicture.setImageResource(R.drawable.placeholder)

            if(recipe.picturePath != null){
                val imagePath = parseImagePath(recipe.picturePath!!)
                Log.d("IMAGE DEBUG", imagePath);

                WsChefy.getPicasso(itemView.context)
                    .load(imagePath)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(recipePicture)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchRecipeViewHolder {
        val recipeView = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.search_recipe_item, parent, false)
        return SearchRecipeViewHolder(recipeView)
    }

    override fun onBindViewHolder(holder: SearchRecipeViewHolder, position: Int) {
        holder.bind(recipeList[position])
    }

    override fun getItemCount() = recipeList.size

    /*private fun convertTime(makingTime: Long): String {
        val miliMinutes = (makingTime / 60000).toInt()

        if (miliMinutes < 60) {
            return miliMinutes.toString() + "min"
        }
        else if (miliMinutes % 60 == 0) {
            val hours = (miliMinutes / 60).toString()
            return hours + "h"
        }
        else {
            val hours = (miliMinutes / 60).toString()
            val minutes = (miliMinutes % 60).toString()
            return hours + "h" + minutes + "min"
        }

    }*/

    private fun convertTimeMinutes(makingTime: Long): String {

        if (makingTime < 60) {
            return makingTime.toString() + "min"
        }
        else if (makingTime % 60 == 0.toLong()) {
            val hours = (makingTime / 60).toString()
            return hours + "h"
        }
        else {
            val hours = (makingTime / 60).toString()
            val minutes = (makingTime % 60).toString()
            return hours + "h " + minutes + "min"
        }

    }

    private fun parseImagePath(imagePath : String) : String{
        return WsChefy.IMAGE_BASE_URL + imagePath
    }
}
