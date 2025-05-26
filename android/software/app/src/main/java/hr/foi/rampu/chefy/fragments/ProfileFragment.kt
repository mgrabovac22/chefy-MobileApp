package hr.foi.rampu.chefy

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import hr.foi.rampu.chefy.activities.AvatarEditorActivity
import hr.foi.rampu.chefy.activities.LoginActivity
import hr.foi.rampu.chefy.models.avatar.AvatarCategoriesData
import hr.foi.rampu.chefy.models.avatar.AvatarCategory
import hr.foi.rampu.chefy.models.avatar.AvatarOption
import hr.foi.rampu.chefy.ws.response.AvatarOptionCategoriesResponseGet
import hr.foi.rampu.chefy.ws.items.AvatarOptionCategoryItem
import hr.foi.rampu.chefy.ws.items.AvatarOptionItem
import hr.foi.rampu.chefy.ws.response.AvatarOptionResponseGet
import hr.foi.rampu.chefy.ws.WsChefy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ProfileFragment : Fragment() {

    private lateinit var passwordFieldsContainer: LinearLayout
    private lateinit var currentPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmNewPasswordEditText: EditText
    private lateinit var changePasswordButton: Button
    private lateinit var confirmPasswordButton: Button
    private lateinit var showPasswordCheckBox: CheckBox
    private var user_id : Int? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val nameTextView: TextView = view.findViewById(R.id.tv_name)
        val lastnameTextView: TextView = view.findViewById(R.id.tv_lastname)
        val emailTextView: TextView = view.findViewById(R.id.tv_email)
        val avatarButton: Button = view.findViewById(R.id.btn_avatar)
        val logoutButton: Button = view.findViewById(R.id.btn_logout)
        val avatarContainer: CardView = view.findViewById(R.id.avatarContainer)


        passwordFieldsContainer = view.findViewById(R.id.password_fields_container)
        currentPasswordEditText = view.findViewById(R.id.et_current_password)
        newPasswordEditText = view.findViewById(R.id.et_new_password)
        confirmNewPasswordEditText = view.findViewById(R.id.et_confirm_new_password)
        changePasswordButton = view.findViewById(R.id.btn_change_password)
        confirmPasswordButton = view.findViewById(R.id.btn_confirm_password)
        showPasswordCheckBox = view.findViewById(R.id.checkbox_show_password)

        val sharedPreferences = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("user_firstName", "Nepoznato ime")
        val lastname = sharedPreferences.getString("user_lastName", "Nepoznato prezime")
        val email = sharedPreferences.getString("user_email", "Nepoznat email")

        nameTextView.text = "$name"
        lastnameTextView.text = "$lastname"
        emailTextView.text = "$email"


        var user_id_string = sharedPreferences.getString("user_id", null)
        user_id = user_id_string?.toInt()
        loadSavedAvatarOptions(requireContext())




        /*avatarButton.setOnClickListener {
            openAvatarActivity()
        }*/
        avatarContainer.setOnClickListener{
            openAvatarActivity()
        }


        logoutButton.setOnClickListener {
            logoutUser()
        }

        changePasswordButton.setOnClickListener {
            togglePasswordFields()
        }

        confirmPasswordButton.setOnClickListener {
            validatePasswordChange()
        }

        showPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            togglePasswordVisibility(isChecked)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadSavedAvatarOptions(requireContext())
    }

    private fun togglePasswordFields() {
        passwordFieldsContainer.visibility =
            if (passwordFieldsContainer.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    private fun togglePasswordVisibility(show: Boolean) {
        val inputType = if (show) android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        else android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        currentPasswordEditText.inputType = inputType
        newPasswordEditText.inputType = inputType
        confirmNewPasswordEditText.inputType = inputType


        currentPasswordEditText.setSelection(currentPasswordEditText.text.length)
        newPasswordEditText.setSelection(newPasswordEditText.text.length)
        confirmNewPasswordEditText.setSelection(confirmNewPasswordEditText.text.length)
    }

    private fun validatePasswordChange() {
        clearErrorMessages()

        val currentPassword = currentPasswordEditText.text.toString().trim()
        val newPassword = newPasswordEditText.text.toString().trim()
        val confirmNewPassword = confirmNewPasswordEditText.text.toString().trim()

        var hasError = false

        if (currentPassword.isEmpty()) {
            currentPasswordEditText.error = "Molimo unesite trenutnu lozinku"
            hasError = true
        }

        if (newPassword.isEmpty()) {
            newPasswordEditText.error = "Molimo unesite novu lozinku"
            hasError = true
        }

        if (confirmNewPassword.isEmpty()) {
            confirmNewPasswordEditText.error = "Molimo ponovite novu lozinku"
            hasError = true
        }

        if (newPassword != confirmNewPassword) {
            confirmNewPasswordEditText.error = "Lozinke se ne podudaraju"
            hasError = true
        }

        if (newPassword == currentPassword) {
            newPasswordEditText.error = "Nova lozinka mora biti drugačija od trenutne"
            hasError = true
        }

        if (!hasError) {
            changePassword()
        }
    }

    private fun clearErrorMessages() {
        currentPasswordEditText.error = null
        newPasswordEditText.error = null
        confirmNewPasswordEditText.error = null
    }
    private fun openAvatarActivity() {
        val intent = Intent(requireActivity(), AvatarEditorActivity::class.java)
        startActivity(intent)
    }

    private fun logoutUser() {
        val sharedPreferences = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
    private fun changePassword() {
        val sharedPreferences = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)?.toIntOrNull()

        if (userId == null) {
            Toast.makeText(requireContext(), "Korisnički ID nije pronađen", Toast.LENGTH_SHORT).show()
            return
        }

        val currentPassword = currentPasswordEditText.text.toString().trim()
        val newPassword = newPasswordEditText.text.toString().trim()
        val confirmNewPassword = confirmNewPasswordEditText.text.toString().trim()

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Molimo unesite sva polja", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmNewPassword) {
            Toast.makeText(requireContext(), "Lozinke se ne podudaraju", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "http://157.230.8.219/chefy/change_password.php"
        val jsonRequest = JSONObject().apply {
            put("user_id", userId)
            put("current_password", currentPassword)
            put("new_password", newPassword)
        }


        val auth = "smartcoders:y39T>("
        val encodedAuth = android.util.Base64.encodeToString(auth.toByteArray(), android.util.Base64.NO_WRAP)

        Log.d("DEBUG", "JSON za slanje: $jsonRequest")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Authorization", "Basic $encodedAuth")
                connection.doOutput = true

                val outputStream = OutputStreamWriter(connection.outputStream)
                outputStream.write(jsonRequest.toString())
                outputStream.flush()

                val responseCode = connection.responseCode
                val responseStream = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val responseMessage = responseStream.bufferedReader().use { it.readText() }

                Log.d("DEBUG", "Odgovor sa servera: $responseMessage")

                withContext(Dispatchers.Main) {
                    val jsonResponse = JSONObject(responseMessage)
                    if (jsonResponse.has("success")) {
                        Toast.makeText(requireContext(), "Lozinka uspješno promijenjena!", Toast.LENGTH_SHORT).show()
                        passwordFieldsContainer.visibility = View.GONE
                        currentPasswordEditText.text.clear()
                        newPasswordEditText.text.clear()
                        confirmNewPasswordEditText.text.clear()
                    } else {
                        Toast.makeText(requireContext(), jsonResponse.getString("error"), Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ERROR", "Greška prilikom promjene lozinke: ${e.message}")
                    Toast.makeText(requireContext(), "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }




    private fun loadSavedAvatarOptions(context: Context){
        val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

        val isAvatarFetched = sharedPreferences.contains("avatarFetch")
        if (isAvatarFetched) {

            AvatarCategoriesData.buildInstance(context)
            val avatarCategories = AvatarCategoriesData.getInstance()

            val allEntries = sharedPreferences.all
            val avatarEntries = allEntries.filter { it.key.startsWith("avatar-") }
            avatarEntries.forEach { entry ->
                val categoryName = entry.key.split("-").last()
                val category : AvatarCategory? = avatarCategories.find { it.name == categoryName}
                val optionName = entry.value.toString()
                if(category != null){
                    val avatarOption : AvatarOption? = category.options.find { it.name == optionName}
                    if(avatarOption != null)
                        updateAvatarPreview(category, avatarOption)
                }
            }

            showAvatarContainer()

        } else {
            loadSavedAvatarOptionsFromDB(context)
        }
    }

    private fun showAvatarContainer() {
        val avatarContainer = view?.findViewById<CardView>(R.id.avatarContainer)
        avatarContainer?.visibility = View.VISIBLE
    }


    private fun loadSavedAvatarOptionsFromDB(context: Context){

        fetchAvatarOptionCategories{ avatarOptionCategories, success ->

            if(!success || user_id == null){
                return@fetchAvatarOptionCategories
            }

            fetchAvatarOptionsByUserId(user_id!!) { userAvatarOptions, success ->

                if(!success){
                    return@fetchAvatarOptionsByUserId
                }

                val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

                AvatarCategoriesData.buildInstance(context)
                val avatarCategories = AvatarCategoriesData.getInstance()

                userAvatarOptions.forEach { option ->
                    val categoryName =
                        avatarOptionCategories.find { category -> category.id == option.id_avatarOptionCategory }?.name
                    val optionName = option.name;
                    if (categoryName != null) {

                        val category : AvatarCategory? = avatarCategories.find { it.name == categoryName}
                        if(category != null){
                            val avatar_option : AvatarOption? = category.options.find { it.name == optionName}
                            if(avatar_option != null){
                                updateAvatarPreview(category, avatar_option)
                                saveAvatarOptionToSharedPreferences(categoryName,optionName, sharedPreferences)
                            }
                        }
                    }
                }

                showAvatarContainer()
            }
        }
    }

    private fun saveAvatarOptionToSharedPreferences(
        categoryName: String,
        optionName: String,
        sharedPreferences: SharedPreferences
    ) {

        val editor = sharedPreferences.edit()

        var key = "avatar-" + categoryName
        editor.putString(key, optionName)

        editor.putBoolean("avatarFetch", true)

        editor.apply()
    }



    private fun fetchAvatarOptionCategories(callback: (List<AvatarOptionCategoryItem>, Boolean) -> Unit) {
        WsChefy.avatarOptionCategoryService.getAvatarOptionCategories().enqueue(
            object : Callback<AvatarOptionCategoriesResponseGet> {
                override fun onResponse(
                    call: Call<AvatarOptionCategoriesResponseGet>,
                    response: Response<AvatarOptionCategoriesResponseGet>
                ) {
                    if (response.isSuccessful) {
                        val avatarOptionCategories  = response.body()?.results ?: emptyList()
                        if(avatarOptionCategories.isNotEmpty()){
                            callback(avatarOptionCategories, true)
                        }
                        else{
                            callback(emptyList(), false)
                        }

                        Log.d("AvatarCategories", "Dohvaćene kategorije: $avatarOptionCategories")
                    } else {
                        Log.e("AvatarCategories", "Greška u odgovoru: ${response.code()}")
                        callback(emptyList(), false)
                    }
                }

                override fun onFailure(call: Call<AvatarOptionCategoriesResponseGet>, t: Throwable) {

                    Log.e("AvatarCategories", "Greška u dohvaćanju podataka: ${t.message}")
                    callback(emptyList(), false)
                }
            }
        )
    }

    private fun fetchAvatarOptionsByUserId(idUser : Int,callback: (List<AvatarOptionItem>, Boolean) -> Unit) {
        WsChefy.avatarOptionService.getAvatarOptionsByUserId(idUser.toString()).enqueue(
            object : Callback<AvatarOptionResponseGet> {
                override fun onResponse(
                    call: Call<AvatarOptionResponseGet>,
                    response: Response<AvatarOptionResponseGet>
                ) {
                    if (response.isSuccessful) {
                        val avatarOptions  = response.body()?.results ?: emptyList()

                        Log.d("AvatarOptions", "Dohvaćene opcije: $avatarOptions")
                        callback(avatarOptions, true)
                    } else {
                        Log.e("AvatarOptions", "Greška u odgovoru: ${response.code()}")
                        callback(emptyList(), false)
                    }
                }

                override fun onFailure(call: Call<AvatarOptionResponseGet>, t: Throwable) {
                    Log.e("AvatarOptions", "Greška u dohvaćanju podataka: ${t.message}")
                    callback(emptyList(), false)
                }

            }
        )
    }

    private fun updateAvatarPreview(selectedCategory: AvatarCategory, selectedOption: AvatarOption) {
        val imageViewLayer = view?.findViewById<ImageView>(selectedCategory.layoutViewId)
        imageViewLayer?.setImageResource(selectedOption.imageResId)
        imageViewLayer?.visibility = if (selectedOption.imageResId != R.drawable.avatar_option_none) View.VISIBLE else View.INVISIBLE
    }
}

