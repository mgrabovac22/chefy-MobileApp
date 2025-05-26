package hr.foi.rampu.chefy.fragments

import ProcedureStepViewModel
import RecipeViewModel
import StepViewModel
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import hr.foi.rampu.chefy.AddRecipeFragment
import hr.foi.rampu.chefy.R
import hr.foi.rampu.chefy.activities.AddRecipeActivity
import hr.foi.rampu.chefy.viewmodels.IngredientViewModel
import hr.foi.rampu.chefy.viewmodels.StepFinishViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ForthStepAddRecipeFragment : Fragment() {

    private lateinit var btnAddRecipePictureFinish: Button
    private lateinit var btnPrevious: Button
    private lateinit var btnSave: Button
    private lateinit var txtDescriptionFinish: EditText
    private var recipeDescriptionFinish: String = ""
    private lateinit var txtTimeFinish: EditText
    private lateinit var imgPicture: ImageView

    private var capturedImagePath: String? = null

    private lateinit var recipeViewModel: RecipeViewModel
    private lateinit var ingredientViewModel: IngredientViewModel
    private lateinit var stepViewModel: StepViewModel
    private lateinit var stepViewModelProcedure: ProcedureStepViewModel
    private lateinit var stepViewModelFinish: StepFinishViewModel

    private var saveRecipeListener: SaveRecipeListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_forth_step_add_recipe, container, false)

        btnAddRecipePictureFinish = view.findViewById(R.id.btnAddRecipePictureFinish)
        btnPrevious = view.findViewById(R.id.btnPrevious)
        btnSave = view.findViewById(R.id.btnSave)
        txtDescriptionFinish = view.findViewById(R.id.txtDescriptionFinish)
        txtTimeFinish = view.findViewById(R.id.txtTimeFinish)
        imgPicture = view.findViewById(R.id.imageView2)

        recipeViewModel = ViewModelProvider(requireActivity()).get(RecipeViewModel::class.java)
        ingredientViewModel = ViewModelProvider(requireActivity()).get(IngredientViewModel::class.java)
        stepViewModel = ViewModelProvider(requireActivity()).get(StepViewModel::class.java)
        stepViewModelProcedure = ViewModelProvider(requireActivity()).get(ProcedureStepViewModel::class.java)
        stepViewModelFinish = ViewModelProvider(requireActivity()).get(StepFinishViewModel::class.java)

        btnAddRecipePictureFinish.setOnClickListener {
            showImagePickerDialog()
        }

        btnPrevious.setOnClickListener {
            //saveUserInput()
            goToPreviousStep()
        }

        btnSave.setOnClickListener {
            if (validateInputs()) {
                saveUserInput()
                saveRecipeListener?.onSaveRecipe()
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtDescriptionFinish.setText(stepViewModelFinish.getDescriptionFinish())
        txtTimeFinish.setText(stepViewModelFinish.getMakingTimeFinish().toString())

        stepViewModelFinish.getImagePathFinish()?.let { path ->
            val bitmap = loadImageFromPath(path)
            bitmap?.let {
                imgPicture.setImageBitmap(bitmap)
                capturedImagePath = stepViewModelFinish.getImagePathFinish()
            }
        }
    }

    private fun goToPreviousStep() {
        val fragment = ThirdStepAddRecipeFragment()
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun saveRecipeAndNavigate() {
        val description = txtDescriptionFinish.text.toString()

        if (description.isEmpty()) {
            return
        }

        val fragment = AddRecipeFragment()
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()

        activity?.let {
            if (it is AddRecipeActivity) {
                it.finish()
            }
        }
    }
    private fun saveUserInput(){
        val finishTime = txtTimeFinish.text.toString().toInt()
        val description = txtDescriptionFinish.text.toString().trim()

        stepViewModelFinish.setDescriptionFinish(description)
        stepViewModelFinish.setMakingTimeFinish(finishTime)
        stepViewModelFinish.setPictureDataString(capturedImagePath!!)
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Izaberi iz galerije", "Otvori kameru")
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Dodaj sliku")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> pickImageFromGallery()
                1 -> captureImageFromCamera()
            }
        }
        builder.show()
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun captureImageFromCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        } else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        }
    }


    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val uri = result.data?.data
            uri?.let {
                val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, it)
                val savedPath = saveImageToInternalStorage(bitmap)
                savedPath?.let {
                    capturedImagePath = it
                    stepViewModelFinish.setPictureDataString(it)
                    imgPicture.setImageBitmap(bitmap)
                }
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val photo = result.data?.extras?.get("data") as? Bitmap
            if (photo != null) {
                capturedImagePath = saveImageToInternalStorage(photo)
                imgPicture.setImageBitmap(photo)
                val savedPath = saveImageToInternalStorage(photo)
                savedPath?.let {
                    capturedImagePath = it
                    stepViewModelFinish.setPictureDataString(it)
                    imgPicture.setImageBitmap(photo)
                }
            }
        }
    }


    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraLauncher.launch(intent)
            }
        }

    private fun validateInputs(): Boolean {

        recipeDescriptionFinish = txtDescriptionFinish.text.toString().trim()
        if (recipeDescriptionFinish.isEmpty()) {
            txtDescriptionFinish.error = "Unesite opis pripreme"
            return false
        }

        val preparationTime = txtTimeFinish.text.toString().trim()
        if (preparationTime.isEmpty() || preparationTime.toInt()==0) {
            txtTimeFinish.error = "Unesite vrijeme pripreme"
            return false
        }

        if ( stepViewModelFinish.getImagePathFinish()== null || capturedImagePath.isNullOrEmpty()) {
            btnAddRecipePictureFinish.error = "Morate unijeti sliku"
            return false
        }

        return true
    }

    private fun loadImageFromPath(path: String): Bitmap? {
        return BitmapFactory.decodeFile(path)
    }
    private fun saveImageToInternalStorage(bitmap: Bitmap): String? {
        val directory = File(requireContext().filesDir, "uploadedImages")
        if (!directory.exists()) {
            directory.mkdir()
        }

        val fileName = "${System.currentTimeMillis()}.jpg"
        val file = File(directory, fileName)

        return try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.flush()
            outputStream.close()
            file.absolutePath
        } catch (e: IOException) {
            null
        }
    }

    interface SaveRecipeListener {
        fun onSaveRecipe()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SaveRecipeListener) {
            saveRecipeListener = context
        } else {
            throw RuntimeException("$context must implement SaveRecipeListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        saveRecipeListener = null
    }
}
