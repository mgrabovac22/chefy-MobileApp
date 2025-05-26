package hr.foi.rampu.chefy.models.avatar

import hr.foi.rampu.chefy.models.avatar.AvatarOption

data class AvatarCategory(
    val name: String,
    val displayName: String,
    val iconResId: Int,
    val layoutViewId: Int,
    val options: List<AvatarOption>
)