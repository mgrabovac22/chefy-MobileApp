package hr.foi.rampu.chefy.ws.response

import hr.foi.rampu.chefy.ws.items.RecipeIngredientItem

data class RecipeIngredientResponse(
    val count: Int,
    val results: List<RecipeIngredientItem>
)

data class RecipeIngredientResponsePost(
    var success: Boolean,
    var message : String?,
    var error : String?
)