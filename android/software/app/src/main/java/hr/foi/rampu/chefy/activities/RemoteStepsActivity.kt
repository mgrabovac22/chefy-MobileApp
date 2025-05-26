package hr.foi.rampu.chefy.activities

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import hr.foi.rampu.chefy.R
import hr.foi.rampu.chefy.ws.WsChefy
import hr.foi.rampu.chefy.ws.items.AvatarOptionItem
import hr.foi.rampu.chefy.ws.items.StepRemote
import hr.foi.rampu.chefy.ws.response.AvatarOptionResponseGet
import hr.foi.rampu.chefy.ws.response.GetStepsRemoteResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RemoteStepsActivity : AppCompatActivity() {

    private lateinit var allSteps : List<StepRemote>
    private var currentStage : Int = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val recipeId = intent.getIntExtra("recipe_id", -1)
        val recipeName = intent.getStringExtra("recipe_name")
        val recipeDescription = intent.getStringExtra("recipe_description")


        setContentView(R.layout.activity_remote_steps)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val tvHeader = findViewById<TextView>(R.id.tv_header)
        val tvRecipeDescription = findViewById<TextView>(R.id.tv_recipe_description)



        val btnBack = findViewById<ExtendedFloatingActionButton>(R.id.btn_back)
        val btnNext = findViewById<ExtendedFloatingActionButton>(R.id.btn_next)

        tvHeader.text = recipeName
        tvRecipeDescription.text = recipeDescription



        getAllStepsForRecipe(recipeId){success, allFetchedSteps ->
            allSteps = allFetchedSteps
            //Set up first step
            loadStage(1)
            hideProgressBar()
        }

        btnBack.setOnClickListener{
            handleBackPressed()
        }

        btnNext.setOnClickListener{
            handleNextPressed()
        }



    }

    private fun handleBackPressed() {
        when(currentStage){
            2 -> loadStage(1)
            3 -> loadStage(2)
            else -> onBackPressedDispatcher.onBackPressed()
        }
    }



    private fun handleNextPressed() {
        when(currentStage){
            1 -> loadStage(2)
            2 -> loadStage(3)
            else -> onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadStage(stage: Int) {
        val btnBack = findViewById<ExtendedFloatingActionButton>(R.id.btn_back)
        val btnNext = findViewById<ExtendedFloatingActionButton>(R.id.btn_next)

        currentStage = stage
        val tvHeaderStep = findViewById<TextView>(R.id.tv_header_step)
        val tvStepDescription = findViewById<TextView>(R.id.tv_step_description)
        val tvMakingTime = findViewById<TextView>(R.id.tv_making_time)
        val imvStepImage = findViewById<ImageView>(R.id.imv_step_image)
        val chefyEndingContainer = findViewById<LinearLayout>(R.id.chefyEndingContainer)

        val stepData = allSteps[stage-1]
        tvHeaderStep.text = formatStepHeader(stepData.stage)
        tvStepDescription.text = stepData.description
        tvMakingTime.text = convertTimeMinutes (stepData.making_time.toLong())

        //imvStepImage.setImageResource(R.drawable.placeholder)

        if(stepData.image_path != null){
            val imagePath = parseImagePath(stepData.image_path)
            Log.d("IMAGE DEBUG", imagePath);

            WsChefy.getPicasso(this)
                .load(imagePath)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(imvStepImage)
        }

        if(currentStage != 1){
            btnBack.setText("Prethodni korak")
        }
        else{
            btnBack.setText("Detalji")
        }

        if(currentStage != 3){
            btnNext.setText("Slijedeći korak")
            chefyEndingContainer.visibility = View.GONE
        }
        else{
            btnNext.setText("Završi")
            chefyEndingContainer.visibility = View.VISIBLE
        }
    }



    private fun hideProgressBar() {
        val progressBar = findViewById<ProgressBar>(R.id.pbLoading)
        progressBar.visibility = View.GONE

        val frameStep = findViewById<FrameLayout>(R.id.frame_step)
        frameStep.visibility = View.VISIBLE
    }


    private fun formatStepHeader(stage : Int) : String{

        val stageHeader = when (stage) {
            1 -> "Počnimo sa pripremom!"
            2 -> "Počnimo sa kuhanjem!"
            else -> "Počnimo sa finalnim detaljima!"
        }
        return stageHeader
    }

    private fun convertTimeMinutes(makingTime: Long): String {

        if (makingTime < 60) {
            return makingTime.toString() + "min"
        }
        else if (makingTime % 60 == 0.toLong()) {
            val hours = (makingTime / 60).toString()
            return hours + "h"
        }
        else {
            val hours = (makingTime / 60).toString()
            val minutes = (makingTime % 60).toString()
            return hours + "h " + minutes + "min"
        }

    }


    private fun parseImagePath(imagePath : String) : String{
        return WsChefy.IMAGE_BASE_URL + imagePath
    }






    private fun getAllStepsForRecipe(id_recipe : Int,callback: (Boolean, List<StepRemote>) -> Unit) {
        WsChefy.stepsService.getAllStepsForRecipe(id_recipe).enqueue(
            object : Callback<GetStepsRemoteResponse> {
                override fun onResponse(
                    call: Call<GetStepsRemoteResponse>,
                    response: Response<GetStepsRemoteResponse>
                ) {
                    if (response.isSuccessful) {
                        val allFetchedSteps  = response.body()?.steps ?: emptyList()

                        if(allFetchedSteps.isEmpty()){
                            callback(false, emptyList())
                        }else{
                            callback(false, allFetchedSteps)
                        }


                    } else {
                        //Log.e("TEST DEBUG", "Greška u odgovoru: ${response.code()}")
                        callback(false, emptyList())
                    }
                }

                override fun onFailure(call: Call<GetStepsRemoteResponse>, t: Throwable) {
                    callback(false, emptyList())
                }

            }
        )
    }
}