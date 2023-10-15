package ru.netology.nmedia.util

import android.widget.ImageView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R

fun ImageView.load(url:String){
    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.ic_load_24)
        .error(R.drawable.ic_error_24)
        .timeout(30_0000)
        .circleCrop()
        .into(this)
}
fun ImageView.loadAttachment(url:String){
    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.ic_load_24)
        .error(R.drawable.ic_error_24)
        .timeout(30_0000)
        .into(this)
}