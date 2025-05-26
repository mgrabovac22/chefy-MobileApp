package hr.foi.rampu.chefy

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import hr.foi.rampu.chefy.fragments.AboutUsFragment
import hr.foi.rampu.chefy.fragments.HelpFragment
import hr.foi.rampu.chefy.fragments.PrivacyPolicyFragment


class SettingsPreferencesFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var notificationsPreference: SwitchPreferenceCompat

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.fragment_settings, rootKey)

        notificationsPreference = findPreference("notifications")!!

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        notificationsPreference.isChecked = sharedPreferences.getBoolean("notifications", true)

        notificationsPreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                manageNotifications(newValue as Boolean)
                true
            }

        val aboutUsPreference = findPreference<Preference>("about_us")
        aboutUsPreference?.setOnPreferenceClickListener {
            replaceFragment(AboutUsFragment())
            true
        }

        val privacyPreference = findPreference<Preference>("privacy")
        privacyPreference?.setOnPreferenceClickListener {
            replaceFragment(PrivacyPolicyFragment())
            true
        }

        val helpPreference = findPreference<Preference>("help")
        helpPreference?.setOnPreferenceClickListener {
            replaceFragment(HelpFragment())
            true
        }

        val easterEggPref = findPreference<Preference>("easter_egg")
        easterEggPref?.setOnPreferenceClickListener {
            playSound()
            showEasterEggDialog()
            true
        }

    }

    private fun manageNotifications(isEnabled: Boolean) {
        val sharedPrefs = requireContext().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putBoolean("notifications_enabled", isEnabled)
            apply()
        }

        if (isEnabled) {
            if (!isNotificationEnabled()) {
                requestNotificationPermission()
            }
        } else {
            disableNotifications()
            openAppNotificationSettings()
        }
    }

    private fun isNotificationEnabled(): Boolean {
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.areNotificationsEnabled()
        } else {
            true
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), 1)
            }
        }
    }

    private fun disableNotifications() {
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    private fun openAppNotificationSettings() {
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
        } else {
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
        }
        startActivity(intent)
    }


    private fun playSound() {
        val mediaPlayer = MediaPlayer.create(requireContext(), R.raw.cat)
        mediaPlayer.start()

        mediaPlayer.setOnCompletionListener { mp: MediaPlayer ->
            mp.release()
        }
    }

    private fun playSadSound() {
        val mediaPlayer = MediaPlayer.create(requireContext(), R.raw.sad_trombone)
        mediaPlayer.start()

        mediaPlayer.setOnCompletionListener { mp: MediaPlayer ->
            mp.release()
        }
    }

    private fun showEasterEggDialog() {
        var okValue = false
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Čestitamo!")
            .setMessage("Otkrili ste tajnu postavku! Nažalost, nemamo ništa korisno za ponuditi, ali svejedno ste super. 😊")
            .setPositiveButton("OK") { dialog, _ ->
                okValue = true
                playSadSound()
                dialog.dismiss()
            }
            .setNegativeButton("Pokušaj opet") { dialog, _ ->
                okValue = false
                playSound()
            }
            .setCancelable(false)
            .create()

        dialog.setOnDismissListener {
            if(okValue){
                dialog.dismiss()
            }
            else{
                dialog.show()
            }

        }
        dialog.show()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key != null) {
            when (key) {
                "notifications" -> {
                    val areNotificationsEnabled = sharedPreferences?.getBoolean("notifications", true) ?: true
                    manageNotifications(areNotificationsEnabled)
                }
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.settings_container, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}
