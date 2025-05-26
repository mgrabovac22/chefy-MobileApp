package hr.foi.rampu.chefy.viewmodels

import androidx.lifecycle.ViewModel

class StepFinishViewModel : ViewModel() {
    private var descriptionFinish = ""
    private var makingTimeFinish = 0
    private var imagePathFinish = ""

    fun setDescriptionFinish(description: String) {
        descriptionFinish = description
    }

    fun setMakingTimeFinish(time: Int) {
        makingTimeFinish = time
    }

    fun setPictureDataString(path: String) {
        imagePathFinish = path
    }

    fun getDescriptionFinish(): String = descriptionFinish
    fun getMakingTimeFinish(): Int = makingTimeFinish
    fun getImagePathFinish(): String? = imagePathFinish
}