package tj.personal.thecompass.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import tj.personal.thecompass.R
import kotlin.math.ln

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
            val latitudeEditText = dialog.findViewById<EditText>(R.id.latitude_edit_text)
            val longitudeEditText = dialog.findViewById<EditText>(R.id.longitude_edit_text)
            val confirmBtn = dialog.findViewById<Button>(R.id.confirm_button)
            confirmBtn.setOnClickListener {
                //try
                //todo add minus
                val lat : Double? = latitudeEditText.text.toString().toDouble()
                val lng : Double? = longitudeEditText.text.toString().toDouble()
                if (lat != null && lng != null && isValidLatLng(lat, lng)) {
                    listener.onDialogConfirmClick(lat, lng)
                    dialog.cancel()
                } else {
                    //todo add smaller red text

//                    latitudeEditText.setText("Value must be <-90; 90>")
//                    longitudeEditText.setText("Value must be <-180; 180>")
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

//    private fun isInputValid(lat: Double?, lng: Double?) : Boolean {
//        return lat != null && lng != null && isValidLatLng(lat, lng)
//
//    }

    private fun isValidLatLng(lat: Double, lng: Double): Boolean {
        if (lat < -90 || lat > 90) {
            return false
        } else if (lng < -180 || lng > 180) {
            return false
        }
        return true
    }
}