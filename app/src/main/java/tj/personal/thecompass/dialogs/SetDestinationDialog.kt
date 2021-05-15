package tj.personal.thecompass.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.set_destination_dialog.*
import tj.personal.thecompass.R
import java.lang.IllegalStateException

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
            val confirmBtn = dialog.findViewById<Button>(R.id.confirm_button)
            confirmBtn.setOnClickListener {
                listener.onDialogConfirmClick(latitudeEditText.text.toString())
                dialog.cancel()
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
        fun onDialogConfirmClick(latitude: String)
    }

}