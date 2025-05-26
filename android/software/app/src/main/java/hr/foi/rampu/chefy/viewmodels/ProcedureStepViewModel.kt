import androidx.lifecycle.ViewModel

class ProcedureStepViewModel : ViewModel() {

    private var descriptionProcedure = ""
    private var makingTimeProcedure = 0
    private var imagePathProcedure = ""

    fun setDescriptionProcedure(description: String) {
        descriptionProcedure = description
    }

    fun setMakingTimeProcedure(time: Int) {
        makingTimeProcedure = time
    }

    fun setPictureDataString(path: String) {
        imagePathProcedure = path
    }

    fun getDescriptionProcedure(): String = descriptionProcedure
    fun getMakingTimeProcedure(): Int = makingTimeProcedure
    fun getImagePathProcedure(): String? = imagePathProcedure

}



