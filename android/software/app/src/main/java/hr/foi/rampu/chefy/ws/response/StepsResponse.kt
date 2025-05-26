package hr.foi.rampu.chefy.ws.response

import hr.foi.rampu.chefy.ws.items.StepRemote

data class GetStepsRemoteResponse(
    val id_recipe: Int,
    val steps: List<StepRemote>
)

data class MyStepInsertResponse(
    var success: Boolean
)
