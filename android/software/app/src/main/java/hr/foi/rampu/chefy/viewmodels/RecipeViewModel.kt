import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.ViewModel

class RecipeViewModel : ViewModel() {
    private var recipeId: Int = 0
    private var recipeName = ""
    private var description = ""
    private var makingTime = ""
    private var picturePath: String? = null
    private var category = ""

    fun setRecipeId(newId: Int) { recipeId = newId }
    fun setRecipeName(name: String) { recipeName = name }
    fun setDescription(desc: String) { description = desc }
    fun setMakingTime(time: String) { makingTime = time }
    fun setPicturePath(path: String) { picturePath = path }
    fun setCategory(cat: String) { category = cat }

    fun getRecipeId(): Int = recipeId
    fun getRecipeName(): String = recipeName
    fun getDescription(): String = description
    fun getMakingTime(): String = makingTime
    fun getPicturePath(): String? = picturePath
    fun getCategory(): String = category
}

