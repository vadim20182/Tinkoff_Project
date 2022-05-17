package android.example.tinkoffproject.chat.common.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
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

class ChangeTopicForMessageDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView =
            requireActivity().layoutInflater.inflate(R.layout.change_topic_dialog, null)
        val topicInput = dialogView.findViewById<EditText>(R.id.change_topic)
        return AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.change_topic_for_message_dialog_title))
            .setView(dialogView)
            .setPositiveButton(R.string.change_topic_for_message_confirm) { _, _ ->
                if (topicInput.text.isNotBlank()) {
                    val parent = requireParentFragment()
                    if (topicInput.text.trim().toString() != requireStringArgs(
                            ARG_OLD_TOPIC,
                            this
                        )
                    )
                        when (parent) {
                            is ChannelChatFragment -> parent.changeTopicForMessage(
                                topicInput.text.trim().toString(),
                                requireIntArgs(ARG_MSG_ID, this)
                            )
                            is TopicChatFragment -> parent.changeTopicForMessage(
                                topicInput.text.trim().toString(),
                                requireIntArgs(ARG_MSG_ID, this)
                            )
                        }
                } else Snackbar.make(
                    requireParentFragment().requireView(),
                    "Topic is empty!",
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

    companion object {
        const val ARG_OLD_TOPIC = "old topic"
        const val ARG_MSG_ID = "message id"
    }
}