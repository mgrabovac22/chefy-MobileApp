package hr.foi.rampu.chefy.ws.response

import hr.foi.rampu.chefy.ws.items.AvatarOptionCategoryItem

data class AvatarOptionCategoriesResponseGet(
    var count: Int,
    val results: List<AvatarOptionCategoryItem>
)