package hr.foi.rampu.chefy.ws.response

import hr.foi.rampu.chefy.ws.items.AvatarOptionItem

data class AvatarOptionResponseGet(
    var count: Int,
    val results: List<AvatarOptionItem>
)

data class AvatarOptionResponsePut(
    var success: Boolean,
    var error : String?,
    var message : String?,
)

data class AvatarOptionResponsePost(
    var success: Boolean,
)