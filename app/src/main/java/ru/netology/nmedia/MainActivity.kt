package ru.netology.nmedia

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.AndroidUtils.focusAndShowKeyboard
import ru.netology.nmedia.viewmodel.PostViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel: PostViewModel by viewModels()
        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
                viewModel.shareById(post.id)
            }
        })
        binding.list.adapter = adapter
        viewModel.data.observe(this) { posts ->
            adapter.submitList(posts) {
                binding.list.smoothScrollToPosition(0)
            }

        }

        viewModel.edited.observe(this) { post ->
            if (post.id !== 0L) {
                binding.content.setText(post.content)
                binding.content.focusAndShowKeyboard()
                binding.editor.editingContent.text = post.content
                binding.editor.editGroup.visibility = View.VISIBLE
            }
        }

        binding.save.setOnClickListener {
            val text = binding.content.text.toString()
            if (text.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_content, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.changeContentAndSave(text)
            binding.editor.editingContent.text = ""
            binding.editor.editGroup.visibility = View.GONE
            binding.content.setText("")
            binding.content.clearFocus()
            AndroidUtils.hideKeyboard(it)
        }

        binding.editor.cancelInput.setOnClickListener {
            viewModel.clearTextInput()
            binding.editor.editingContent.text = ""
            binding.editor.editGroup.visibility = View.GONE
            binding.content.setText("")
            binding.content.clearFocus()
            AndroidUtils.hideKeyboard(it)
        }
    }
}