package android.example.tinkoffproject.chat.common.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.example.tinkoffproject.R
import android.example.tinkoffproject.chat.channel.ui.ChannelChatFragment
import android.example.tinkoffproject.chat.common.ui.requireIntArgs
import android.example.tinkoffproject.chat.common.ui.requireStringArgs
import android.example.tinkoffproject.chat.topic.ui.TopicChatFragment
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar

class EditMessageDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView =
            requireActivity().layoutInflater.inflate(R.layout.edit_message_dialog, null)
        val editMessageInput = dialogView.findViewById<EditText>(R.id.edit_message)
        editMessageInput.setText(
            requireStringArgs(
                ARG_OLD_MESSAGE,
                this
            )
        )
        return AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.edit_message_dialog_title))
            .setView(dialogView)
            .setPositiveButton(
                R.string.edit_message_dialog_confirm,
                DialogInterface.OnClickListener { _, _ ->
                    if (editMessageInput.text.isNotBlank()) {
                        val parent = requireParentFragment()
                        if (editMessageInput.text.trim().toString() != requireStringArgs(
                                ARG_OLD_MESSAGE,
                                this
                            )
                        )
                            when (parent) {
                                is ChannelChatFragment -> parent.editMessage(
                                    editMessageInput.text.trim().toString(),
                                    requireIntArgs(ARG_MSG_ID, this)
                                )
                                is TopicChatFragment->parent.editMessage(
                                    editMessageInput.text.trim().toString(),
                                    requireIntArgs(ARG_MSG_ID, this)
                                )
                            }
                    } else Snackbar.make(
                        requireParentFragment().requireView(),
                        "New message is empty!",
                        Snackbar.LENGTH_SHORT
                    ).apply {
                        setTextColor(Color.WHITE)
                        setBackgroundTint(Color.RED)
                    }.show()
                })
            .setNegativeButton(
                R.string.cancel_dialog_button,
                DialogInterface.OnClickListener { dialog, _ ->
                    dialog.cancel()
                })
            .create()
    }

    companion object {
        const val ARG_OLD_MESSAGE = "old message"
        const val ARG_MSG_ID = "message id"
    }
}