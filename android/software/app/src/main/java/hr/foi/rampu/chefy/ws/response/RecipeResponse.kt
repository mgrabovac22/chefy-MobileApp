package hr.foi.rampu.chefy.ws.response

import hr.foi.rampu.chefy.ws.items.RecipeItem

data class RecipeResponse(
    var count: Int,
    val results: List<RecipeItem>
)

data class RecipeItemResponse(
    var count: Int,
    val results: List<RecipeItem>
)

data class MyRecipeInsertResponse(
    var success: Boolean,
    var id : Int?,
    var error : String?
)

data class MyRecipeDeleteResponse(
    var success: Boolean,
)