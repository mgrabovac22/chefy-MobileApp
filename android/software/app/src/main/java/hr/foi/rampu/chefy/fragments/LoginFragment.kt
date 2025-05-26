package hr.foi.rampu.chefy

import android.content.Context
import android.content.Intent
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
import hr.foi.rampu.chefy.activities.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class LoginFragment : Fragment() {

    private lateinit var emailEditText: EditText
    private lateinit var lozinkaEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var showPasswordCheckBox: CheckBox

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        emailEditText = view.findViewById(R.id.EmailTxt)
        lozinkaEditText = view.findViewById(R.id.LozinkaTxt)
        loginButton = view.findViewById(R.id.LoginButton)
        registerButton = view.findViewById(R.id.RegisterButton)
        showPasswordCheckBox = view.findViewById(R.id.checkbox_show_password)

        loginButton.setOnClickListener {
            var email = emailEditText.text.toString().trim()
            var password = lozinkaEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(activity, "Molimo unesite email i lozinku!", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, password)
            }
        }

        registerButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        showPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            togglePasswordVisibility(isChecked)
        }

        return view
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

    private fun loginUser(email: String, password: String) {
        val loginUrl = "http://157.230.8.219/chefy/login.php"
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connection = URL(loginUrl).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")

                val auth = "smartcoders:y39T>("
                val encodedAuth = Base64.encodeToString(auth.toByteArray(), Base64.NO_WRAP)
                connection.setRequestProperty("Authorization", "Basic $encodedAuth")

                connection.doOutput = true

                val jsonRequest = JSONObject().apply {
                    put("username", email)
                    put("password", password)
                }

                val outputStream = OutputStreamWriter(connection.outputStream)
                outputStream.write(jsonRequest.toString())
                outputStream.flush()

                val responseCode = connection.responseCode
                val responseMessage = connection.inputStream.bufferedReader().use { it.readText() }

                withContext(Dispatchers.Main) {
                    if (responseCode == 200) {
                        val jsonResponse = JSONObject(responseMessage)
                        if (jsonResponse.getBoolean("success")) {
                            val user = jsonResponse.getJSONObject("user")
                            val userId = user.getString("id")

                            val sharedPreferences =
                                requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
                            with(sharedPreferences.edit()) {
                                putString("user_id", userId)
                                putString("user_email", user.getString("email"))
                                apply()
                            }

                            (activity as? LoginActivity)?.saveSession()

                            fetchUserDetails(userId, "http://157.230.8.219/chefy/users.php")
                        } else {
                            Toast.makeText(activity, "Pogrešan email ili lozinka!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(activity, "Greška u prijavi: $responseCode", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchUserDetails(userId: String, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty(
                    "Authorization",
                    "Basic " + Base64.encodeToString("smartcoders:y39T>(".toByteArray(), Base64.NO_WRAP)
                )
                connection.setRequestProperty("user_id", userId)

                val responseCode = connection.responseCode
                val responseMessage = connection.inputStream.bufferedReader().use { it.readText() }

                withContext(Dispatchers.Main) {
                    if (responseCode == 200) {
                        val userDetails = JSONObject(responseMessage)
                        val sharedPreferences =
                            requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
                        with(sharedPreferences.edit()) {
                            putString("user_firstName", userDetails.getString("firstName"))
                            putString("user_lastName", userDetails.getString("lastName"))
                            apply()
                        }
                        navigateToMainActivity()
                    } else {
                        Toast.makeText(activity, "Greška prilikom dohvaćanja podataka: $responseCode", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToMainActivity() {

        val intent = Intent(activity, MainActivity::class.java)
        val deepLinkRemoteRecipeId = arguments?.getString("deepLinkRemoteRecipeId")

        if (deepLinkRemoteRecipeId != null) {
            intent.putExtra("deepLinkRemoteRecipeId", deepLinkRemoteRecipeId)
        }

        startActivity(intent)
        activity?.finish()
    }
}
