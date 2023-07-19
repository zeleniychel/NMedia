package ru.netology.nmedia.dto

class NumberFormat {
    fun convertNumber (number: Int): String {
        return when {
            number >= 1000000 -> "${number / 1000000}.${number / 100000 % 10}M"
            number >= 10000 -> "${number / 1000}K"
            number >= 1100 -> "${number / 1000}.${number / 100 % 10}K"
            number >= 1000 -> "${number/1000}К"
            else -> number.toString()
        }
    }
}