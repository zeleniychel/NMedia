package ru.netology.nmedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.NumberFormat
import ru.netology.nmedia.dto.Post

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val post = Post(
            1L,
            "Нетология. Университет интернет-профессий будущего",
            "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
            "21 мая в 18:36",
            false,
            999,
            1099,
            123
        )
        with (binding) {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            likesCount.text = NumberFormat().convertNumber(post.likesCount)
            sharesCount.text = NumberFormat().convertNumber(post.sharesCount)
            viewsCount.text = NumberFormat().convertNumber(post.viewsCount)



            if (post.likeByMe) {
                likes.setImageResource(R.drawable.ic_baseline_favorite_red_24)
            }

            likes.setOnClickListener {
                post.likeByMe = !post.likeByMe
                if (post.likeByMe) post.likesCount++ else post.likesCount--
                likes.setImageResource(
                    if (post.likeByMe) R.drawable.ic_baseline_favorite_red_24 else R.drawable.ic_favorite_border_24
                )
                likesCount.text = NumberFormat().convertNumber(post.likesCount)
            }
            share.setOnClickListener {
                post.sharesCount++
                sharesCount.text = NumberFormat().convertNumber(post.sharesCount)
            }
        }
    }
}