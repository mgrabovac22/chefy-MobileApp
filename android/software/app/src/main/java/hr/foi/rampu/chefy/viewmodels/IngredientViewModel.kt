package hr.foi.rampu.chefy.viewmodels

import androidx.lifecycle.ViewModel

class IngredientViewModel : ViewModel() {
    private val ingredientsList = mutableListOf<List<Any>>()

    fun setIngredient(ingredientId: Int, quantity: String, unitId: Int) {
        ingredientsList.add(listOf(ingredientId, quantity, unitId))
    }

    fun getIngredients(): List<List<Any>> {
        return ingredientsList
    }

    fun removeIngredientAt(index: Int) {
        if (index in ingredientsList.indices) {
            ingredientsList.removeAt(index)
        }
    }

}

