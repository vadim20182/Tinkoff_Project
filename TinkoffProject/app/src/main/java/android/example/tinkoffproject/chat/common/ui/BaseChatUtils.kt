package android.example.tinkoffproject.chat.common.ui

import android.app.Activity
import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.example.tinkoffproject.R
import android.example.tinkoffproject.chat.channel.presentation.elm.ChannelChatEvent
import android.example.tinkoffproject.chat.channel.ui.ChannelChatFragment
import android.example.tinkoffproject.chat.common.ui.dialogs.ChangeTopicForMessageDialogFragment
import android.example.tinkoffproject.chat.common.ui.dialogs.DeleteMessageDialogFragment
import android.example.tinkoffproject.chat.common.ui.dialogs.EditMessageDialogFragment
import android.example.tinkoffproject.chat.topic.presentation.elm.ChatEvent
import android.example.tinkoffproject.chat.topic.ui.TopicChatFragment
import android.example.tinkoffproject.message.customviews.FlexBoxLayout
import android.example.tinkoffproject.message.customviews.ReactionCustomView
import android.example.tinkoffproject.network.NetworkCommon
import android.example.tinkoffproject.utils.EMOJI_MAP
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isEmpty
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
import okhttp3.Credentials


fun requireStringArgs(argKey: String, fragment: Fragment): String {
    return fragment.requireArguments().getString(
        argKey
    ) ?: throw NullPointerException("No arguments with such key passed")
}

fun requireStringArrayArgs(argKey: String, fragment: Fragment): Array<out String> {
    return fragment.requireArguments().getStringArray(
        argKey
    ) ?: throw NullPointerException("No arguments with such key passed")
}

fun requireIntArgs(argKey: String, fragment: Fragment): Int {
    return fragment.requireArguments().getInt(
        argKey
    )
}

fun downloadFile(link: String, context: Context) {
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE)
            as DownloadManager
    val request = DownloadManager.Request(Uri.parse(link))
        .setAllowedOverMetered(true)
        .addRequestHeader(
            "Authorization",
            Credentials.basic(NetworkCommon.EMAIL, NetworkCommon.API_KEY)
        )
        .setDescription("Downloading")
        .setTitle("Zulip file")
        .setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            link.substringAfterLast("/")
        )
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
    downloadManager.enqueue(request)
}

fun showMessageBottomSheetDialog(
    message: UiModel.MessageItem,
    fragment: Fragment,
    topicsForChannel: Array<String>
) {
    object : BottomSheetDialog(fragment.requireContext()) {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val bottomSheetFlexBoxLayout =
                findViewById<FlexBoxLayout>(R.id.bottom_sheet_message_flexbox)!!
            val addReactionButton =
                bottomSheetFlexBoxLayout.findViewById<ShapeableImageView>(R.id.bottom_sheet_message_add_reaction)
            val editMessageButton =
                bottomSheetFlexBoxLayout.findViewById<ShapeableImageView>(R.id.bottom_sheet_message_edit)
            val changeTopicButton =
                bottomSheetFlexBoxLayout.findViewById<ShapeableImageView>(R.id.bottom_sheet_message_change_topic)
            val copyMessageButton =
                bottomSheetFlexBoxLayout.findViewById<ShapeableImageView>(R.id.bottom_sheet_message_copy)
            val deleteMessageButton =
                bottomSheetFlexBoxLayout.findViewById<ShapeableImageView>(R.id.bottom_sheet_message_delete)
            addReactionButton.setOnClickListener {
                showReactionBottomSheetDialog(message.messageId, fragment)
                cancel()
            }

            editMessageButton.setOnClickListener {
                if (message.isMyMessage) {
                    val editMessageDialog by lazy { EditMessageDialogFragment() }
                    val arguments = Bundle().apply {
                        putString(
                            EditMessageDialogFragment.ARG_OLD_MESSAGE,
                            message.messageText
                        )
                        putInt(EditMessageDialogFragment.ARG_MSG_ID, message.messageId)
                    }
                    editMessageDialog.arguments = arguments

                    fragment.childFragmentManager.beginTransaction()
                        .add(editMessageDialog, "edit dialog")
                        .commit()
                    cancel()
                } else Toast.makeText(
                    fragment.context,
                    "Can't edit other people's messages!",
                    Toast.LENGTH_SHORT
                ).show()
            }

            changeTopicButton.setOnClickListener {
                if (message.isMyMessage) {
                    val changeTopicDialog by lazy { ChangeTopicForMessageDialogFragment() }
                    val arguments = Bundle().apply {
                        putString(
                            ChangeTopicForMessageDialogFragment.ARG_OLD_TOPIC,
                            message.topicName
                        )
                        putInt(
                            ChangeTopicForMessageDialogFragment.ARG_MSG_ID,
                            message.messageId
                        )
                        putStringArray(
                            ChangeTopicForMessageDialogFragment.ARG_TOPICS,
                            topicsForChannel
                        )
                    }
                    changeTopicDialog.arguments = arguments

                    fragment.childFragmentManager.beginTransaction()
                        .add(changeTopicDialog, "change topic dialog")
                        .commit()
                    cancel()
                } else Toast.makeText(
                    fragment.context,
                    "Can't edit other people's messages!",
                    Toast.LENGTH_SHORT
                ).show()
            }

            copyMessageButton.setOnClickListener {
                val clipboard =
                    ContextCompat.getSystemService(context, ClipboardManager::class.java)
                val clip = ClipData.newPlainText("Message text", message.messageText)
                clipboard?.setPrimaryClip(clip)
                Toast.makeText(fragment.context, "Copied", Toast.LENGTH_SHORT).show()
                cancel()
            }

            deleteMessageButton.setOnClickListener {
                if (message.isMyMessage) {
                    val deleteMessageDialog by lazy { DeleteMessageDialogFragment() }
                    val arguments = Bundle().apply {
                        putInt(DeleteMessageDialogFragment.ARG_MSG_ID, message.messageId)
                    }
                    deleteMessageDialog.arguments = arguments

                    fragment.childFragmentManager.beginTransaction()
                        .add(deleteMessageDialog, "delete dialog")
                        .commit()
                    cancel()
                } else Toast.makeText(
                    fragment.context,
                    "Can't edit other people's messages!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }.apply {
        setContentView(R.layout.message_bottom_sheet_layout)
    }.show()
}

fun showReactionBottomSheetDialog(messageId: Int, fragment: Fragment) {
    object : BottomSheetDialog(fragment.requireContext()) {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val bottomSheetFlexBoxLayout =
                findViewById<FlexBoxLayout>(R.id.bottom_sheet_emojis)
            if (bottomSheetFlexBoxLayout?.isEmpty() == true) {
                for (key in EMOJI_MAP.keys)
                    bottomSheetFlexBoxLayout.addView((LayoutInflater.from(context)
                        .inflate(
                            R.layout.emoji_item,
                            null
                        ) as ReactionCustomView).apply {
                        isSimpleEmoji = true
                        setEmojiNameAndCode(key, EMOJI_MAP[key]!!)
                    })
                for (child in bottomSheetFlexBoxLayout.children)
                    child.setOnClickListener {
                        with(child as ReactionCustomView) {
                            when (fragment) {
                                is ChannelChatFragment -> fragment.storeHolder.store.accept(
                                    ChannelChatEvent.Ui.AddReaction(
                                        this.pair.first,
                                        messageId
                                    )
                                )
                                is TopicChatFragment -> fragment.storeHolder.store.accept(
                                    ChatEvent.Ui.AddReaction(
                                        this.pair.first,
                                        messageId
                                    )
                                )
                            }
                        }
                        cancel()
                    }
            }
        }
    }.apply {
        setContentView(R.layout.reactions_bottom_sheet_dialog_layout)
    }.show()
}

class OpenDoc : ActivityResultContract<Array<String>, Uri>() {
    override fun createIntent(context: Context, input: Array<String>?): Intent =
        Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .putExtra(Intent.EXTRA_MIME_TYPES, input)
            .setType("*/*");

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
    }
}