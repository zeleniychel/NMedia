package ru.netology.nmedia.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.dto.Attachment

object Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromAttachment(attachment: Attachment?): String? {
        return gson.toJson(attachment)
    }

    @TypeConverter
    fun toAttachment(attachmentString: String?): Attachment? {
        if (attachmentString == null) {
            return null
        }
        val type = object : TypeToken<Attachment>() {}.type
        return gson.fromJson(attachmentString, type)
    }
}