package hr.foi.rampu.chefy.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import hr.foi.rampu.chefy.R
import hr.foi.rampu.chefy.models.avatar.AvatarOption

class AvatarOptionAdapter(
    private val context: Context,
    private var options: List<AvatarOption>,
    private var selectedOption: AvatarOption?,
    private val onOptionSelected: (AvatarOption) -> Unit
) : RecyclerView.Adapter<AvatarOptionAdapter.AvatarOptionViewHolder>() {

    inner class AvatarOptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val optionIcon: ImageView = itemView.findViewById(R.id.ivItemIcon)
        //private val optionName: TextView = itemView.findViewById(R.id.tv)

        fun bind(option: AvatarOption) {
            optionIcon.setImageResource(option.imageResId)
            //optionName.text = option.name

            val cardViewOption = itemView.findViewById<CardView>(R.id.cvOption)

            //Posebno označi kategoriju koja je označena
            if (option == selectedOption) {
                cardViewOption.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorAvatar))
            } else {
                cardViewOption.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorSecondary))
            }

            itemView.setOnClickListener {
                onOptionSelected(option)
                selectedOption = option
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarOptionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.avatar_item_option, parent, false)
        return AvatarOptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: AvatarOptionViewHolder, position: Int) {
        holder.bind(options[position])
    }

    override fun getItemCount(): Int = options.size


    fun updateOptions(newOptions: List<AvatarOption>, currentSelectedOption : AvatarOption?) {
        options = newOptions
        selectedOption = currentSelectedOption
        notifyDataSetChanged()
    }
}