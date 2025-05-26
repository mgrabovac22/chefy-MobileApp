package hr.foi.rampu.chefy.fragments

import StepViewModel
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import hr.foi.rampu.chefy.R
import hr.foi.rampu.chefy.data.db.RecipesDatabase
import hr.foi.rampu.chefy.models.recipe.Ingredients
import hr.foi.rampu.chefy.models.recipe.Units
import hr.foi.rampu.chefy.viewmodels.IngredientViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SecondStepAddRecipeFragment : Fragment() {

    private lateinit var btnAddRecipePicturePrepration: Button
    private lateinit var btnPrevious: Button
    private lateinit var imgPicture: ImageView
    private lateinit var btnNext: Button
    private lateinit var ingredientSearchBox: AutoCompleteTextView
    private lateinit var spnUnit: Spinner
    private lateinit var btnAddIngredient: Button
    private lateinit var txtTimePreparation: EditText
    private lateinit var txtDescriptionPreparation: EditText

    private var capturedImagePath: String? = null

    private lateinit var viewModel: StepViewModel
    private lateinit var ingredientViewModel: IngredientViewModel

    private var ingredientsList: List<Ingredients> = listOf()
    private var unitsList: List<Units> = listOf()

    private val ingredientNamesList = mutableListOf<String>()
    private val ingredientQuantitiesList = mutableListOf<String>()
    private val ingredientUnitsList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_second_step_add_recipe, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(StepViewModel::class.java)
        ingredientViewModel = ViewModelProvider(requireActivity()).get(IngredientViewModel::class.java)

        btnAddRecipePicturePrepration = view.findViewById(R.id.btnAddRecipePicturePrepration)
        btnPrevious = view.findViewById(R.id.btnPrevious)
        btnNext = view.findViewById(R.id.btnNext)
        btnAddIngredient = view.findViewById(R.id.btnAddIngredients)
        ingredientSearchBox = view.findViewById(R.id.txtIngredient)
        spnUnit = view.findViewById(R.id.spnUnit)
        txtTimePreparation = view.findViewById(R.id.txtTimePreparation)
        txtDescriptionPreparation = view.findViewById(R.id.txtDescriptionPreparation)
        imgPicture = view.findViewById(R.id.imageView2)

        CoroutineScope(Dispatchers.IO).launch {
            val db = RecipesDatabase.getInstance()

            ingredientsList = db.daoIngredient.getAllIngredients()
            unitsList = db.daoUnits.getAllUnits()

            withContext(Dispatchers.Main) {
                setupAutoCompleteTextView()
                setupUnitSpinner()
            }
        }

        btnAddRecipePicturePrepration.setOnClickListener {
            showImagePickerDialog()
        }

        btnPrevious.setOnClickListener {
           // saveUserInput()
            goToPreviousStep()
        }

        btnNext.setOnClickListener {
            if (validateInputs()) {
                saveUserInput()
                goToNextStep()
            }
        }

        btnAddIngredient.setOnClickListener {
            addNewIngredient()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtDescriptionPreparation.setText(viewModel.getDescriptionPreparation())
        txtTimePreparation.setText(viewModel.getMakingTimePreparation().toString())

        viewModel.getImagePathPreparation()?.let { path ->
            if (path.isNotEmpty()) {
                val bitmap = loadImageFromPath(path)
                bitmap?.let {
                    imgPicture.setImageBitmap(bitmap)
                    capturedImagePath = path
                }
            }
        }


        populateIngredients()
    }


    private fun populateIngredients() {
        val ingredients = ingredientViewModel.getIngredients()

        for (ingredient in ingredients) {
            val ingredientId = ingredient[0] as Int
            val quantity = ingredient[1] as String
            val unitId = ingredient[2] as Int

            val ingredientName = ingredientsList.firstOrNull { it.id == ingredientId }?.name ?: "Nepoznato"
            val unitName = unitsList.firstOrNull { it.id == unitId }?.name ?: "Nepoznato"

            addIngredientToUI(ingredientName, quantity, unitName)

            ingredientNamesList.add(ingredientName)
            ingredientQuantitiesList.add(quantity)
            ingredientUnitsList.add(unitName)

        }
    }

    private fun setupAutoCompleteTextView() {
        val ingredientNames = ingredientsList.map { it.name }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, ingredientNames)
        ingredientSearchBox.setAdapter(adapter)

        ingredientSearchBox.setThreshold(1)
    }

    private fun setupUnitSpinner() {
        val unitNames = unitsList.map { it.name }

        val unitAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, unitNames)
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spnUnit.adapter = unitAdapter
    }

    private fun goToPreviousStep() {
        val fragment = FirstStepAddRecipeFragment()
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)  // This prevents immediate backstack popping
        transaction.commit()
    }


    private fun goToNextStep() {
        val fragment = ThirdStepAddRecipeFragment()
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()

        //parentFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)  // This prevents immediate backstack popping
        transaction.commit()
    }

    private fun saveUserInput(){
        val preparationTime = txtTimePreparation.text.toString().toInt()
        val description = txtDescriptionPreparation.text.toString().trim()

        viewModel.setDescriptionPreparation(description)
        viewModel.setMakingTimePreparation(preparationTime)
        viewModel.setPictureDataString(capturedImagePath!!)
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
                val savedPath = saveImageToInternalStorage(photo)
                Log.d("ImageSave", "Captured Image Path: $savedPath")

                if (savedPath != null) {
                    capturedImagePath = savedPath
                    viewModel.setPictureDataString(savedPath) // Check if this triggers a fragment reset
                    imgPicture.setImageBitmap(photo)
                } else {
                    Log.e("SecondStepAddRecipe", "Error: Image not saved")
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

    private fun loadImageFromPath(path: String): Bitmap? {
        val file = File(path)
        return if (file.exists()) {
            BitmapFactory.decodeFile(path)
        } else {
            Log.e("SecondStepAddRecipe", "Image file not found: $path")
            null
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): String? {
        val directory = File(requireContext().filesDir, "uploadedImages")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, "${System.currentTimeMillis()}.jpg")
        return try {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }
            if (file.exists()) {
                Log.d("SecondStepAddRecipe", "Saved image path: ${file.absolutePath}")
                file.absolutePath
            } else {
                Log.e("SecondStepAddRecipe", "Failed to save image")
                null
            }
        } catch (e: IOException) {
            Log.e("SecondStepAddRecipe", "Error saving image", e)
            null
        }
    }


    private fun addNewIngredient() {
        val quantity = view?.findViewById<EditText>(R.id.txtQuantity)?.text.toString()
        val ingredientName = ingredientSearchBox.text.toString()
        val unitName = spnUnit.selectedItem.toString()

        if (ingredientNamesList.contains(ingredientName)) {
            return
        }

        val ingredient = ingredientsList.firstOrNull { it.name.equals(ingredientName, ignoreCase = true) }

        if (quantity.isNotEmpty() && ingredientName.isNotEmpty()) {
            if (ingredient == null) {
                return
            }

            val ingredientId = requireNotNull(ingredient?.id) { "Sastojak nije pronađen!" }
            val unitId = unitsList.firstOrNull { it.name == unitName }?.id ?: 0

            ingredientViewModel.setIngredient(ingredientId, quantity, unitId)

            ingredientNamesList.add(ingredientName)
            ingredientQuantitiesList.add(quantity)
            ingredientUnitsList.add(unitName)

            addIngredientToUI(ingredientName, quantity, unitName)
        } else {
            ingredientSearchBox.error = "Unesite sve podatke"
        }
    }

    private fun addIngredientToUI(name: String, quantity: String, unit: String) {
        val ingredientsListView = view?.findViewById<LinearLayout>(R.id.ingredientsListLayout)

        val newIngredientView = LayoutInflater.from(requireContext()).inflate(R.layout.ingredient_item_view, null)
        val ingredientNameTextView = newIngredientView.findViewById<TextView>(R.id.txtIngredientName)
        val quantityTextView = newIngredientView.findViewById<TextView>(R.id.txtQuantity)
        val unitTextView = newIngredientView.findViewById<TextView>(R.id.txtUnit)
        val deleteButton = newIngredientView.findViewById<Button>(R.id.btnDeleteIngredient)

        ingredientNameTextView.text = name
        quantityTextView.text = quantity
        unitTextView.text = unit

        deleteButton.setOnClickListener {
            deleteIngredient(name)
            ingredientsListView?.removeView(newIngredientView)
        }

        ingredientsListView?.addView(newIngredientView)
    }


    private fun deleteIngredient(name: String) {
        val index = ingredientNamesList.indexOf(name)
        if (index >= 0) {
            ingredientNamesList.removeAt(index)
            ingredientQuantitiesList.removeAt(index)
            ingredientUnitsList.removeAt(index)

            ingredientViewModel.removeIngredientAt(index)
        }
    }

    private fun validateInputs(): Boolean {
        if (ingredientNamesList.isEmpty()) {
            btnAddIngredient.error = "Unesite barem 1 sastojak"
            return false
        }

        val descriptionText = txtDescriptionPreparation.text.toString().trim()
        if(descriptionText.isEmpty()){
            txtDescriptionPreparation.error = "Unesite opis pripreme"
            return false
        }

        val preparationTime = txtTimePreparation.text.toString().trim()
        if (preparationTime.isEmpty() || preparationTime.toInt()==0) {
            txtTimePreparation.error = "Unesite vrijeme pripreme"
            return false
        }

        if (viewModel.getImagePathPreparation() == null || capturedImagePath.isNullOrEmpty()) {
            btnAddRecipePicturePrepration.error = "Morate unijeti sliku"
            return false
        }

        return true
    }

}

