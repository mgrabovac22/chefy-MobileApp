package hr.foi.rampu.chefy.fragments

import ProcedureStepViewModel
import android.Manifest
import android.app.Activity
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
import hr.foi.rampu.chefy.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ThirdStepAddRecipeFragment : Fragment() {

    private lateinit var btnAddRecipePicture: Button
    private lateinit var btnPrevious: Button
    private lateinit var btnNext: Button

    private lateinit var txtDescription: EditText
    private lateinit var txtTimePreparation: EditText
    private lateinit var imgPicture: ImageView

    private var recipeDescription: String = ""

    private var capturedImagePath: String? = null


    private lateinit var viewModel: ProcedureStepViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_third_step_add_recipe, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(ProcedureStepViewModel::class.java)

        imgPicture = view.findViewById(R.id.imageView2)

        btnAddRecipePicture = view.findViewById(R.id.btnAddRecipePicture)
        btnPrevious = view.findViewById(R.id.btnPrevious)
        btnNext = view.findViewById(R.id.btnNext)

        txtDescription = view.findViewById(R.id.txtDescription)
        txtTimePreparation = view.findViewById(R.id.txtTimePreparation)

        btnAddRecipePicture.setOnClickListener {
            showImagePickerDialog()
        }

        btnPrevious.setOnClickListener {
            //saveUserInput()
            goToPreviousStep()
        }

        btnNext.setOnClickListener {
            if(validateInputs()){
                saveUserInput()
                goToNextStep()
            }
        }

        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtDescription.setText(viewModel.getDescriptionProcedure())
        txtTimePreparation.setText(viewModel.getMakingTimeProcedure().toString())

        viewModel.getImagePathProcedure()?.let { path ->
            val bitmap = loadImageFromPath(path)
            bitmap?.let {
                capturedImagePath = viewModel.getImagePathProcedure()
                imgPicture.setImageBitmap(it)
            }
        }

    }

    private fun goToPreviousStep() {
        val fragment = SecondStepAddRecipeFragment()
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun goToNextStep() {
        val fragment = ForthStepAddRecipeFragment()
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
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
                    viewModel.setPictureDataString(it)
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
                    viewModel.setPictureDataString(it)
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


    private fun loadImageFromPath(path: String): Bitmap? {
        return BitmapFactory.decodeFile(path)
    }

    private fun saveUserInput(){
        val preparationTime = txtTimePreparation.text.toString().toInt()
        val description = txtDescription.text.toString().trim()

        viewModel.setDescriptionProcedure(description)
        viewModel.setMakingTimeProcedure(preparationTime)
        viewModel.setPictureDataString(capturedImagePath!!)
    }

    private fun validateInputs(): Boolean {

        recipeDescription = txtDescription.text.toString().trim()
        if (recipeDescription.isEmpty()) {
            txtDescription.error = "Unesite opis pripreme"
            return false
        }

        val preparationTime = txtTimePreparation.text.toString().trim()
        if (preparationTime.isEmpty() || preparationTime.toInt()==0) {
            txtTimePreparation.error = "Unesite vrijeme pripreme"
            return false
        }

        if (viewModel.getImagePathProcedure() == null || capturedImagePath.isNullOrEmpty()) {
            btnAddRecipePicture.error = "Morate unijeti sliku"
            return false
        }

        return true
    }

}
