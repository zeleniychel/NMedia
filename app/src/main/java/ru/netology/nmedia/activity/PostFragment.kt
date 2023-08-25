package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.EditorFragment.Companion.content
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostViewHolder
import ru.netology.nmedia.databinding.FragmentCardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.getParcelableCompat
import ru.netology.nmedia.viewmodel.PostViewModel


class PostFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

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

            override fun onPlay(post: Post) {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(post.videoUrl)
                )
                val packageManager = activity?.packageManager ?: return
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                }
            }

            override fun onPost(post: Post) {}

            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
                findNavController().navigateUp()
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
                viewModel.shareById(post.id)


            }
        })

        holder.bind(postArg ?: Post())
        viewModel.data.observe(viewLifecycleOwner) {
            holder.bind(it.find { (id) -> id == postArg?.id } ?: Post())
        }

        return binding.root
    }
}