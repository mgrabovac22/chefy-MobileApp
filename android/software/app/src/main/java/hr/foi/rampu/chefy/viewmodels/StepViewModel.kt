import androidx.lifecycle.ViewModel

class StepViewModel : ViewModel() {

    private var descriptionPreparation = ""
    private var makingTimePreparation = 0
    private var imagePathPreparation = ""

    fun setDescriptionPreparation(description: String) {
        descriptionPreparation = description
    }

    fun setMakingTimePreparation(time: Int) {
        makingTimePreparation = time
    }

    fun setPictureDataString(data: String) {
        imagePathPreparation = data
    }

    fun getDescriptionPreparation(): String = descriptionPreparation
    fun getMakingTimePreparation(): Int = makingTimePreparation
    fun getImagePathPreparation(): String? = imagePathPreparation
}
