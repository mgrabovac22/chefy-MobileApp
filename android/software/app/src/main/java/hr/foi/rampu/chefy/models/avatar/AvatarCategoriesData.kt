package hr.foi.rampu.chefy.models.avatar

import android.content.Context
import hr.foi.rampu.chefy.R

object AvatarCategoriesData {

    private var avatarCategories: List<AvatarCategory>? = null

    fun getInstance(): List<AvatarCategory> {
        return avatarCategories ?: throw NullPointerException("AvatarCategories list has not yet been created!")
    }

    fun buildInstance(context: Context) {
        if (avatarCategories == null) { // Provjerava da li je već inicijalizirana
            avatarCategories = listOf(
                AvatarCategory(
                    //Skin
                    name = "Skin",
                    displayName = context.getString(R.string.categoryNameSkin),
                    iconResId = R.drawable.ic_avatar_base,
                    layoutViewId = R.id.imvBase,
                    options = listOf(
                        //AvatarOption("None", R.drawable.avatar_option_none),
                        AvatarOption("Skin 1", R.drawable.avatar_base_1),
                        AvatarOption("Skin 2", R.drawable.avatar_base_2),
                        AvatarOption("Skin 3", R.drawable.avatar_base_3),
                        AvatarOption("Skin 4", R.drawable.avatar_base_4),
                    )
                ),
                AvatarCategory(
                    //Hat
                    name = "Hat",
                    displayName = context.getString(R.string.categoryNameHat),
                    iconResId = R.drawable.ic_avatar_hat,
                    layoutViewId = R.id.imvHat,
                    options = listOf(
                        //AvatarOption("None", R.drawable.avatar_option_none),
                        AvatarOption("White Hat", R.drawable.avatar_hat_white),
                        AvatarOption("Green Hat", R.drawable.avatar_hat_green),
                        AvatarOption("Pink Hat", R.drawable.avatar_hat_pink),
                        AvatarOption("Purple Hat", R.drawable.avatar_hat_purple),
                        AvatarOption("Sky Hat", R.drawable.avatar_hat_sky)
                    )
                ),
                AvatarCategory(
                    //Eyes
                    name = "Eyes",
                    displayName = context.getString(R.string.categoryNameEyes),
                    iconResId = R.drawable.ic_avatar_eyes,
                    layoutViewId = R.id.imvEyes,
                    options = listOf(
                        AvatarOption("Female Blue Eyes", R.drawable.avatar_eyes_blue_female),
                        AvatarOption("Female Green Eyes", R.drawable.avatar_eyes_green_female),
                        AvatarOption("Female Brown Eyes", R.drawable.avatar_eyes_brown_female),
                        AvatarOption("Blue Eyes", R.drawable.avatar_eyes_blue),
                        AvatarOption("Green Eyes", R.drawable.avatar_eyes_green),
                        AvatarOption("Brown Eyes", R.drawable.avatar_eyes_brown)
                    )
                ),
                AvatarCategory(
                    //Nose
                    name = "Nose",
                    displayName = context.getString(R.string.categoryNameNose),
                    iconResId = R.drawable.ic_avatar_nose,
                    layoutViewId = R.id.imvNose,
                    options = listOf(
                        AvatarOption("Nose 1", R.drawable.avatar_nose_1),
                        AvatarOption("Nose 2", R.drawable.avatar_nose_2),
                        AvatarOption("Nose 3", R.drawable.avatar_nose_3),
                        AvatarOption("Nose 4", R.drawable.avatar_nose_4),
                        AvatarOption("Nose 5", R.drawable.avatar_nose_5)
                    )
                ),
                AvatarCategory(
                    //Mustaches
                    name = "Mustaches",
                    displayName = context.getString(R.string.categoryNameMustaches),
                    iconResId = R.drawable.ic_avatar_mustache,
                    layoutViewId = R.id.imvMustaches,
                    options = listOf(
                        AvatarOption("None", R.drawable.avatar_option_none),
                        AvatarOption("Mustache 1", R.drawable.avatar_mustache_1),
                        AvatarOption("Mustache 2", R.drawable.avatar_mustache_2),
                        AvatarOption("Mustache 3", R.drawable.avatar_mustache_3),
                        AvatarOption("Mustache 4", R.drawable.avatar_mustache_4),
                        AvatarOption("Mustache 5", R.drawable.avatar_mustache_5)
                    )
                ),
                AvatarCategory(
                    //Hair
                    name = "Hair",
                    displayName = context.getString(R.string.categoryNameHair),
                    iconResId = R.drawable.ic_avatar_hair,
                    layoutViewId = R.id.imvHair,
                    options = listOf(
                        AvatarOption("None", R.drawable.avatar_option_none),
                        AvatarOption("Female Brown Hair ", R.drawable.avatar_hair_female_brown),
                        AvatarOption(
                            "Female Brown Hair Dense",
                            R.drawable.avatar_hair_female_brown_dense
                        ),
                        AvatarOption(
                            "Female BrownRed Hair ",
                            R.drawable.avatar_hair_female_brownred
                        ),
                        AvatarOption(
                            "Female Blonde Hair Long",
                            R.drawable.avatar_hair_female_blonde_long
                        ),
                        AvatarOption(
                            "Female Black Hair Long",
                            R.drawable.avatar_hair_female_black_long
                        ),
                    )
                ),
                AvatarCategory(
                    //Mouth
                    name = "Mouth",
                    displayName = context.getString(R.string.categoryNameMouth),
                    iconResId = R.drawable.ic_avatar_mouth,
                    layoutViewId = R.id.imvMouth,
                    options = listOf(
                        AvatarOption("Smile 1", R.drawable.avatar_mouth_smile),
                    )
                ),
                AvatarCategory(
                    //Bow
                    name = "Bow",
                    displayName = context.getString(R.string.categoryNameBow),
                    iconResId = R.drawable.ic_avatar_bow,
                    layoutViewId = R.id.imvBow,
                    options = listOf(
                        AvatarOption("None", R.drawable.avatar_option_none),
                        AvatarOption("Bow Red", R.drawable.avatar_bow_red),
                        AvatarOption("Bow Green", R.drawable.avatar_bow_green),
                        AvatarOption("Bow Orange", R.drawable.avatar_bow_orange),
                        AvatarOption("Bow Pink", R.drawable.avatar_bow_pink),
                        AvatarOption("Bow Purple", R.drawable.avatar_bow_purple),
                        AvatarOption("Bow Sky", R.drawable.avatar_bow_sky),
                    )
                ),
                AvatarCategory(
                    //Shirt
                    name = "Shirt",
                    displayName = context.getString(R.string.categoryNameShirt),
                    iconResId = R.drawable.ic_avatar_shirt,
                    layoutViewId = R.id.imvShirt,
                    options = listOf(
                        AvatarOption("Shirt White", R.drawable.avatar_shirt_white),
                        AvatarOption("Shirt Green", R.drawable.avatar_shirt_green),
                        AvatarOption("Shirt Orange", R.drawable.avatar_shirt_orange),
                        AvatarOption("Shirt Pink", R.drawable.avatar_shirt_pink),
                        AvatarOption("Shirt Purple", R.drawable.avatar_shirt_purple),
                        AvatarOption("Shirt Sky", R.drawable.avatar_shirt_sky),
                        AvatarOption("Shirt White Rounded", R.drawable.avatar_shirt_white_rounded),
                        AvatarOption("Shirt Green Rounded", R.drawable.avatar_shirt_green_rounded),
                        AvatarOption(
                            "Shirt Orange Rounded",
                            R.drawable.avatar_shirt_orange_rounded
                        ),
                        AvatarOption("Shirt Pink Rounded", R.drawable.avatar_shirt_pink_rounded),
                        AvatarOption(
                            "Shirt Purple Rounded",
                            R.drawable.avatar_shirt_purple_rounded
                        ),
                        AvatarOption("Shirt Sky Rounded", R.drawable.avatar_shirt_sky_rounded),
                    ),
                ),
                AvatarCategory(
                    //Background
                    name = "Background",
                    displayName = context.getString(R.string.categoryNameBackground),
                    iconResId = R.drawable.ic_avatar_background,
                    layoutViewId = R.id.imvBackground,
                    options = listOf(
                        //AvatarOption("None", R.drawable.avatar_option_none),
                        AvatarOption("Background Sky", R.drawable.avatar_background_sky),
                        AvatarOption("Background Cream", R.drawable.avatar_background_cream),
                        AvatarOption("Background White", R.drawable.avatar_background_white),
                        AvatarOption("Background Pink", R.drawable.avatar_background_pink),
                        AvatarOption("Background Grey", R.drawable.avatar_background_grey),

                        )
                ),
            )

        }
    }


}
