package android.example.tinkoffproject.channels.ui

import android.app.AlertDialog
import android.app.Dialog
import android.example.tinkoffproject.R
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar

class AddChannelDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView = requireActivity().layoutInflater.inflate(R.layout.add_channel_dialog, null)
        val channelNameInput = dialogView.findViewById<EditText>(R.id.input_channel_name)
        val channelDescriptionInput =
            dialogView.findViewById<EditText>(R.id.input_channel_description)
        return AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.add_channel_dialog_title))
            .setView(dialogView)
            .setPositiveButton(R.string.create_channel_button) { _, _ ->
                if (channelNameInput.text.isNotBlank()) {
                    (requireParentFragment() as MainChannelsFragment).createChannel(
                        channelNameInput.text.trim().toString(),
                        channelDescriptionInput.text.trim().toString().ifBlank { null }
                    )
                } else Snackbar.make(
                    requireParentFragment().requireView(),
                    "Channel name is empty!",
                    Snackbar.LENGTH_SHORT
                ).apply {
                    setTextColor(Color.WHITE)
                    setBackgroundTint(Color.RED)
                }.show()
            }
            .setNegativeButton(R.string.cancel_dialog_button) { dialog, _ ->
                dialog.cancel()
            }
            .create()
    }
}