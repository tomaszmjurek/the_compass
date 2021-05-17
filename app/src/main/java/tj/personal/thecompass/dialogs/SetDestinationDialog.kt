package tj.personal.thecompass.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import tj.personal.thecompass.R

class SetDestinationDialog : DialogFragment() {
    private val TAG = SetDestinationDialog::class.java.simpleName
    private lateinit var listener : SetDestinationDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
       return activity?.let {
           val builder = AlertDialog.Builder(it)
           val inflater = requireActivity().layoutInflater
           builder.setView(inflater.inflate(R.layout.set_destination_dialog, null))
           builder.create()
       } ?: throw IllegalStateException("ERROR")
    }

    override fun onStart() {
        super.onStart()
        dialog?.let { dialog ->
            dialog.setCanceledOnTouchOutside(false)
            val latitudeET = dialog.findViewById<EditText>(R.id.latitude_edit_text)
            val longitudeET = dialog.findViewById<EditText>(R.id.longitude_edit_text)
            val confirmBtn = dialog.findViewById<Button>(R.id.confirm_button)
            confirmBtn.setOnClickListener {
                //try
                //todo add minus
                val lat : Double? = latitudeET.text.toString().toDouble()
                val lng : Double? = longitudeET.text.toString().toDouble()
                if (lat != null && lng != null && isValidLatLng(lat, lng)) {
                    listener.onDialogConfirmClick(lat, lng)
                    dialog.cancel()
                } else {
                    val inputErrorTV = dialog.findViewById<TextView>(R.id.input_error_1)
                    val inputError2TV = dialog.findViewById<TextView>(R.id.input_error_2)
                    inputErrorTV.text = "Value must be between -90 and 90"
                    inputError2TV.text = "Value must be between -180 and 180"
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as SetDestinationDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }

    interface SetDestinationDialogListener {
        fun onDialogConfirmClick(lat: Double, lng: Double)
    }

    private fun isValidLatLng(lat: Double, lng: Double): Boolean {
        if (lat < -90 || lat > 90) {
            return false
        } else if (lng < -180 || lng > 180) {
            return false
        }
        return true
    }
}