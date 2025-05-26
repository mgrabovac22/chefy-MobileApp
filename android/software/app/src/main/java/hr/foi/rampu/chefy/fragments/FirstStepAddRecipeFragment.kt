package hr.foi.rampu.chefy.fragments

import RecipeViewModel
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import hr.foi.rampu.chefy.MainActivity
import hr.foi.rampu.chefy.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FirstStepAddRecipeFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var btnAddRecipePicture: Button
    private lateinit var btnNext: Button
    private lateinit var cmbRecipeCategory: Spinner
    private lateinit var txtRecipeName: EditText
    private lateinit var txtRecipeDescription: EditText
    private lateinit var txtRecipeTime: EditText

    private lateinit var viewModel: RecipeViewModel

    private var capturedImagePath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_first_step_add_recipe, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(RecipeViewModel::class.java)

        imageView = view.findViewById(R.id.imageView2)
        btnAddRecipePicture = view.findViewById(R.id.btnAddRecipePicture)
        btnNext = view.findViewById(R.id.btnNext)
        cmbRecipeCategory = view.findViewById(R.id.cmbRecipeCategory)
        txtRecipeName = view.findViewById(R.id.txtRecipeName)
        txtRecipeDescription = view.findViewById(R.id.txtRecipeDescription)
        txtRecipeTime = view.findViewById(R.id.txtRecipeTime)

        btnAddRecipePicture.setOnClickListener {
            showImagePickerDialog()
        }

        btnNext.setOnClickListener {
            if (validateInputs()) {
                saveUserInput()
                goToSecondStep()
            }
        }

        txtRecipeName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.setRecipeName(s.toString().trim())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        txtRecipeDescription.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.setDescription(s.toString().trim())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        txtRecipeTime.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.setMakingTime(s.toString().trim())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val categories = listOf("Doručak", "Ručak", "Večera", "Desert", "Grickalice", "Prilog", "Salata")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cmbRecipeCategory.adapter = adapter

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                openMainActivity()
            }
        })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtRecipeName.setText(viewModel.getRecipeName())
        txtRecipeDescription.setText(viewModel.getDescription())
        txtRecipeTime.setText(viewModel.getMakingTime())

        viewModel.getPicturePath()?.let { path ->
            val bitmap = loadImageFromPath(path)
            bitmap?.let {
                capturedImagePath = viewModel.getPicturePath()
                imageView.setImageBitmap(it)
            }
        }
    }

    private fun openMainActivity() {
        val intent = Intent(requireActivity(), MainActivity::class.java).apply {
            putExtra("open_fragment", "Add")
        }
        startActivity(intent)
    }

    private fun validateInputs(): Boolean {
        if (txtRecipeName.text.toString().trim().isEmpty()) {
            txtRecipeName.error = "Unesite naziv recepta"
            return false
        }
        if (txtRecipeDescription.text.toString().trim().isEmpty()) {
            txtRecipeDescription.error = "Unesite opis recepta"
            return false
        }
        if (txtRecipeTime.text.toString().trim().isEmpty()) {
            txtRecipeTime.error = "Unesite vrijeme pripreme"
            return false
        }
        if (viewModel.getPicturePath() == null) {
            btnAddRecipePicture.error = "Morate dodati sliku"
            return false
        }
        return true
    }

    private fun saveUserInput() {
        viewModel.setRecipeName(txtRecipeName.text.toString().trim())
        viewModel.setDescription(txtRecipeDescription.text.toString().trim())
        viewModel.setMakingTime(txtRecipeTime.text.toString().trim())
        viewModel.setCategory(cmbRecipeCategory.selectedItem.toString())
        viewModel.setPicturePath(capturedImagePath!!)
        Log.d("CapturedImagePath", capturedImagePath ?: "Nema putanje")
    }


    private fun goToSecondStep() {
        val fragment = SecondStepAddRecipeFragment()
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

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
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
                    viewModel.setPicturePath(it)
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val photo = result.data?.extras?.get("data") as? Bitmap
            if (photo != null) {
                capturedImagePath = saveImageToInternalStorage(photo)
                imageView.setImageBitmap(photo)
                val savedPath = saveImageToInternalStorage(photo)
                savedPath?.let {
                    capturedImagePath = it
                    viewModel.setPicturePath(it)
                    imageView.setImageBitmap(photo)
                }
            }
        }
    }


    private fun saveImageToInternalStorage(bitmap: Bitmap): String? {
        val directory = File(requireContext().filesDir, "uploadedImages")
        if (!directory.exists()) {
            directory.mkdir()
        }

        val fileName = "${System.currentTimeMillis()}.jpg"
        Log.d("ime datoteke", fileName)
        val file = File(directory, fileName)
        Log.d("cijeli file", file.toString())

        return try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.flush()
            outputStream.close()
            Log.d("ImageSave", "Slika spremljena na: ${file.absolutePath}")
            file.absolutePath
        } catch (e: IOException) {
            Log.e("ImageSaveError", "Greška prilikom čuvanja slike", e)
            null
        }
    }

    private fun loadImageFromPath(path: String): Bitmap? {
        return BitmapFactory.decodeFile(path)
    }

    private fun captureImageFromCamera() {
        Log.d("Camera Permission", "Checking camera permission...")
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            Log.d("Camera Permission", "Permission not granted, requesting...")
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        } else {
            Log.d("Camera Permission", "Permission granted, launching camera...")
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        }

    }

}
