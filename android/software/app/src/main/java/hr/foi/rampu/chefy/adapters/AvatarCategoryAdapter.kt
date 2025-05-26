package hr.foi.rampu.chefy.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import hr.foi.rampu.chefy.R
import hr.foi.rampu.chefy.models.avatar.AvatarCategory

class AvatarCategoryAdapter(
    private val context: Context,
    private val categories: List<AvatarCategory>,
    private var selectedCategory: AvatarCategory,
    private val onCategorySelected: (AvatarCategory) -> Unit
) : RecyclerView.Adapter<AvatarCategoryAdapter.AvatarCategoryViewHolder>() {

    inner class AvatarCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var categoryIcon: ImageView
        private var categoryName: TextView


        init
        {
            //pronadji odgovarajuce View-e u layout fajlu (item_category.xml)
            categoryIcon = itemView.findViewById(R.id.ivCategoryIcon)
            categoryName = itemView.findViewById(R.id.txCategoryName)
        }



        fun bind(category: AvatarCategory) {

            //za svaki category kao napravi kartice - stavi Tekst, Sliku i background color
            val categoryCardView = itemView.findViewById<CardView>(R.id.cvCategory)
            val categoryText = itemView.findViewById<TextView>(R.id.txCategoryName)

            if (category == selectedCategory) {
                categoryCardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorPrimary))
                categoryText.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorSecondary))

            } else {
                categoryCardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorAvatar))
                categoryText.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
            }

            categoryIcon.setImageResource(category.iconResId)
            categoryName.text = category.displayName

            //postavi clicklistener
            itemView.setOnClickListener {
                onCategorySelected(category)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarCategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.avatar_item_category, parent, false)
        return AvatarCategoryViewHolder(view)
    }

    fun setSelectedCategory(category: AvatarCategory) {
        selectedCategory = category
        notifyDataSetChanged()
    }



    override fun onBindViewHolder(holder: AvatarCategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size



}
