package hr.foi.rampu.chefy.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import hr.foi.rampu.chefy.R

class HelpFragment : Fragment() {

    private var alertDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_help, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton: View = view.findViewById(R.id.btnBack)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val btnHelp = view.findViewById<Button>(R.id.btnContactSupport)
        btnHelp.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.help_popup, null)

            val dialogBuilder = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)

            alertDialog = dialogBuilder.create()
            alertDialog?.show()

            val btnClose = dialogView.findViewById<Button>(R.id.btnClosePop)
            btnClose.setOnClickListener {
                dismissPopup()
            }
        }
    }

    private fun dismissPopup() {
        alertDialog?.dismiss()
    }
}
