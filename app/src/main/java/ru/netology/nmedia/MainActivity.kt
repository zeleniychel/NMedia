package ru.netology.nmedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Converter
import ru.netology.nmedia.viewmodel.PostViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val viewModel by viewModels<PostViewModel>()

        viewModel.data.observe(this) { post ->
            with(binding) {
                author.text = post.author
                published.text = post.published
                content.text = post.content
                likesCount.text = Converter.convertNumber(post.likesCount)
                sharesCount.text = Converter.convertNumber(post.sharesCount)
                viewsCount.text = Converter.convertNumber(post.viewsCount)

                likes.setImageResource(if (post.likeByMe) R.drawable.ic_baseline_favorite_red_24 else R.drawable.ic_favorite_border_24)
                likesCount.text = Converter.convertNumber(post.likesCount)
                sharesCount.text = Converter.convertNumber(post.sharesCount)
            }
        }

        binding.likes.setOnClickListener {
            viewModel.like()
        }

        binding.share.setOnClickListener {
            viewModel.share()
        }
    }
}