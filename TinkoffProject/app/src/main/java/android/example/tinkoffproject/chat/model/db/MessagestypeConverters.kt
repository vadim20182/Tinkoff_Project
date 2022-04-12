package android.example.tinkoffproject.chat.model.db

import androidx.room.TypeConverter

class ReactionsConverter {
    @TypeConverter
    fun fromReactions(reactions: MutableMap<String, Int>): String {
        return reactions.toString()
    }

    @TypeConverter
    fun toReactions(data: String): MutableMap<String, Int> {
        return if (data != "{}") data.trim('{', '}').split(", ").associate {
            val (left, right) = it.split('=')
            left to right.toInt()
        }.toMutableMap() else mutableMapOf()
    }
}

class SelectedReactionsConverter {
    @TypeConverter
    fun fromSelectedReactions(selectedReactions: MutableMap<String, Boolean>): String {
        return selectedReactions.toString()
    }

    @TypeConverter
    fun toSelectedReactions(data: String): MutableMap<String, Boolean> {
        return if (data != "{}") data.trim('{', '}').split(", ").associate {
            val (left, right) = it.split('=')
            left to right.toBoolean()
        }.toMutableMap() else mutableMapOf()
    }
}

//class MessageRemoteKeysConverter {
//    @TypeConverter
//    fun fromRemoteKeys(messageRemoteKeysEntity: MessageRemoteKeysEntity): String {
//        return "${messageRemoteKeysEntity.messageId},${messageRemoteKeysEntity.nextKey},${messageRemoteKeysEntity.prevKey}}"
//    }
//
//    @TypeConverter
//    fun toRemoteKeys(data: String): MessageRemoteKeysEntity {
//        return data.split(',').
//    }
//}