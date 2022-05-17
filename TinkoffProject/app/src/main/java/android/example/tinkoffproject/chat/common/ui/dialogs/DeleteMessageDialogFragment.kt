package android.example.tinkoffproject.chat.common.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.example.tinkoffproject.R
import android.example.tinkoffproject.chat.channel.ui.ChannelChatFragment
import android.example.tinkoffproject.chat.common.ui.requireIntArgs
import android.example.tinkoffproject.chat.topic.ui.TopicChatFragment
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class DeleteMessageDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_message_dialog_title))
            .setPositiveButton(
                R.string.delete_message_dialog_confirm,
                DialogInterface.OnClickListener { _, _ ->
                    when (val parent = requireParentFragment()) {
                        is ChannelChatFragment -> parent.deleteMessage(
                            requireIntArgs(EditMessageDialogFragment.ARG_MSG_ID, this)
                        )
                        is TopicChatFragment -> parent.deleteMessage(
                            requireIntArgs(EditMessageDialogFragment.ARG_MSG_ID, this)
                        )
                    }
                })
            .setNegativeButton(
                R.string.delete_message_dialog_decline,
                DialogInterface.OnClickListener { dialog, _ ->
                    dialog.cancel()
                })
            .create()
    }

    companion object {
        const val ARG_MSG_ID = "message id"
    }
}