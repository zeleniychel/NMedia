package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.EditorFragment.Companion.content
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostViewHolder
import ru.netology.nmedia.databinding.FragmentCardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.getParcelableCompat
import ru.netology.nmedia.viewmodel.PostViewModel


@AndroidEntryPoint
class PostFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        val postArg = arguments?.getParcelableCompat<Post>("key")

        val binding = FragmentCardPostBinding.inflate(layoutInflater)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            }
        )

        val holder = PostViewHolder(binding.singlePost, object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(
                    R.id.action_postFragment_to_editorFragment,
                    Bundle().apply {
                        content = post.content
                    })

            }

            override fun onAttachment(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_attachmentFragment,
                    bundleOf("key" to post)
                )
            }

            override fun onPost(post: Post) {}

            override fun onLike(post: Post) {
                viewModel.likeById(post)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
                findNavController().navigateUp()
            }

            override fun onShare(post: Post) {
            }
        })

        holder.bind(postArg ?: Post())

//        lifecycleScope.launch {
//            viewModel.data.collectLatest {
//            holder.bind(it.posts.find { (id) -> id == postArg?.id } ?: Post())
//        }

        return binding.root
    }
}