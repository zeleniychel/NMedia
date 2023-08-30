package ru.netology.nmedia.util

import java.text.SimpleDateFormat
import java.util.Date

object DateUtil {
    fun getCurrentDate(): String = SimpleDateFormat("dd.MM.yyyy HH:mm").format(Date())


}