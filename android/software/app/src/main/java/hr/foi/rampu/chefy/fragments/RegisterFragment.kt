package hr.foi.rampu.chefy

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import hr.foi.rampu.chefy.ws.response.AvatarOptionCategoriesResponseGet
import hr.foi.rampu.chefy.ws.items.AvatarOptionCategoryItem
import hr.foi.rampu.chefy.ws.items.AvatarOptionItem
import hr.foi.rampu.chefy.ws.response.AvatarOptionResponsePost
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

class RegisterFragment : Fragment() {

    private lateinit var imeEditText: EditText
    private lateinit var prezimeEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var lozinkaEditText: EditText
    private lateinit var registerButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_register, container, false)

        imeEditText = rootView.findViewById(R.id.RegisterImeTxt)
        prezimeEditText = rootView.findViewById(R.id.RegisterPrezimeTxt)
        emailEditText = rootView.findViewById(R.id.RegisterEmailTxt)
        lozinkaEditText = rootView.findViewById(R.id.RegisterLozinkaTxt)
        registerButton = rootView.findViewById(R.id.RegisterConfirmButton)
        val showPasswordCheckbox = rootView.findViewById<CheckBox>(R.id.checkbox_show_password_register)


        showPasswordCheckbox.setOnCheckedChangeListener { _, isChecked ->
            togglePasswordVisibility(isChecked)
        }
        registerButton.setOnClickListener {
            val ime = imeEditText.text.toString().trim()
            val prezime = prezimeEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val lozinka = lozinkaEditText.text.toString().trim()

            if (ime.isEmpty() || prezime.isEmpty() || email.isEmpty() || lozinka.isEmpty()) {
                Toast.makeText(activity, "Molimo unesite sva polja", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(ime, prezime, email, lozinka)
            }
        }

        rootView.findViewById<Button>(R.id.buttonVratiNaLogin).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return rootView
    }

    private fun registerUser(firstName: String, lastName: String, email: String, password: String) {
        val url = "http://157.230.8.219/chefy/users.php"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")

                val auth = "smartcoders:y39T>("
                val encodedAuth = Base64.encodeToString(auth.toByteArray(), Base64.NO_WRAP)
                connection.setRequestProperty("Authorization", "Basic $encodedAuth")

                connection.doOutput = true

                val jsonRequest = JSONObject().apply {
                    put("register", true)
                    put("username", email.split("@")[0])
                    put("password", password)
                    put("email", email)
                    put("firstName", firstName)
                    put("lastName", lastName)
                }

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

                withContext(Dispatchers.Main) {
                    if (responseCode == 200) {
                        val jsonResponse = JSONObject(responseMessage)
                        if (jsonResponse.getBoolean("success")) {
                            val userId = jsonResponse.getInt("user_id")
                            Toast.makeText(activity, "Registracija uspješna!", Toast.LENGTH_SHORT).show()
                            Log.d("DEBUG REGISTER", "userId: " + userId.toString())
                            fillDefaultAvatarOptionsData(userId){ sucess ->

                                if(!sucess){
                                    Toast.makeText(requireContext(), "Failed to insert default avatar options!", Toast.LENGTH_SHORT).show()
                                }
                                else{
                                    val loginFragment = LoginFragment()
                                    parentFragmentManager.beginTransaction()
                                        .replace(R.id.fragment_container, loginFragment)
                                        .commit()
                                }
                            }

                        } else {
                            Toast.makeText(activity, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(activity, "Greška u registraciji: $responseCode", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun togglePasswordVisibility(show: Boolean) {
        val inputType = if (show) {
            android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        lozinkaEditText.inputType = inputType
        lozinkaEditText.setSelection(lozinkaEditText.text.length)
    }
    private fun getDefaultAvatarOptions(): MutableMap<String, String> {
        val defaultOptions = mutableMapOf<String, String>()
        defaultOptions["Background"] = "Background Sky"
        defaultOptions["Skin"] = "Skin 3"
        defaultOptions["Hat"] = "White Hat"
        defaultOptions["Eyes"] = "Blue Eyes"
        defaultOptions["Nose"] = "Nose 1"
        defaultOptions["Mustaches"] = "Mustache 1"
        defaultOptions["Hair"] = "None"
        defaultOptions["Mouth"] = "Smile 1"
        defaultOptions["Bow"] = "Bow Red"
        defaultOptions["Shirt"] = "Shirt White Rounded"
        return defaultOptions
    }

    private fun fillDefaultAvatarOptionsData(userId: Int, callback: (Boolean) -> Unit) {
        fetchAvatarOptionCategories{ avatarOptionCategories, success ->

            if(!success){
                callback(false)
            }

            try {
                val avatarOptionItemsList : MutableList<AvatarOptionItem> = mutableListOf()

                avatarOptionCategories.forEach{
                    val defaultOptions = getDefaultAvatarOptions()
                    val avatarOptionItem = AvatarOptionItem(userId,it.id,defaultOptions[it.name]!!)
                    avatarOptionItemsList.add(avatarOptionItem)
                }
                postAvatarOptions(avatarOptionItemsList){
                    callback(true)
                }
            } catch (e: Exception){
                    Log.d("INSERT DEFAULT AVATAR OPTIONS DEBUG", "Desila se greška: " + e)
                callback(false)
            }
        }
    }

    private fun postAvatarOptions(avatarOption: List<AvatarOptionItem>, callback:(Boolean) -> Unit) {
        WsChefy.avatarOptionService.insertAvatarOptions(avatarOption).enqueue(
            object : Callback<AvatarOptionResponsePost> {
                override fun onResponse(
                    call: Call<AvatarOptionResponsePost>,
                    response: Response<AvatarOptionResponsePost>
                ) {
                    if (response.isSuccessful) {

                        Log.d("AvatarOptionUpdate", "Avatar opcija uspješno dodana!")
                        callback(true);
                    } else {


                        Log.e("AvatarOptionUpdate", "Greška pri dodavanju avatar opcije: ${response.code()}")
                        callback(false);
                    }
                }

                override fun onFailure(call: Call<AvatarOptionResponsePost>, t: Throwable) {


                    Log.e("AvatarOptionUpdate", "Greška: ${t.message}")
                    callback(false);
                }
            }
        )
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
}
