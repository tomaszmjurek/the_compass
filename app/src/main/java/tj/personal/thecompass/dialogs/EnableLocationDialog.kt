package tj.personal.thecompass.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.DialogFragment

class EnableLocationDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage("Your location settings is set to Off, Please enable location to use this application")
            .setPositiveButton("Settings") { _,_ ->
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(myIntent)
            }
            .setNegativeButton("Cancel") { _,_ ->
//                dialog.cancel()
            }
            .setCancelable(false)
            .create()

    companion object {
        const val TAG = "EnableLocationDialog"
    }
}