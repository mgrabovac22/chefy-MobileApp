package hr.foi.rampu.chefy.ws.items

import com.google.gson.annotations.SerializedName

data class RecipeIngredientItem(
    @SerializedName("id_ingridient") val ingredientId: Int,
    @SerializedName("id_recipe") val recipeId: Int,
    @SerializedName("id_unit") val unitId: Int,
    val quantity: Double,
    @SerializedName("ingredient_name") val ingredientName: String,
    @SerializedName("unit_name") val unitName: String
)

data class RecipeIngredientPost(
    val ingredientName: String,
    val recipeId: Int,
    val unitName: String,
    val quantity: Double,
)
