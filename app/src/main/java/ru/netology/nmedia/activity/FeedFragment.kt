package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.EditorFragment.Companion.content
import ru.netology.nmedia.activity.EditorFragment.Companion.url
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostLoadingStateAdapter
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth


    private val viewModel: PostViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        val binding = FragmentFeedBinding.inflate(layoutInflater)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            }
        )

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(R.id.action_feedFragment_to_editorFragment,
                    Bundle().apply {
                        content = post.content
                        url = post.attachment?.url
                    })
            }

            override fun onAttachment(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_attachmentFragment,
                    bundleOf("key" to post)
                )
            }

            override fun onPost(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_postFragment,
                    bundleOf("key" to post)
                )
            }

            override fun onLike(post: Post) {
                if (appAuth.authStateFlow.value.id == 0L) {
                    LoginDialog().show(childFragmentManager, "")
                } else {
                    viewModel.likeById(post)
                }
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
            }
        })

        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PostLoadingStateAdapter{ adapter.retry() },
            footer = PostLoadingStateAdapter{adapter.retry()}
        )

        val authViewModel by viewModels<AuthViewModel>()

        lifecycleScope.launch {
            authViewModel.data.collectLatest {
                adapter.refresh()
            }
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swiperefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction("Retry") { adapter.refresh() }
                    .show()
            }
        }

        lifecycleScope.launch {
            viewModel.data.collectLatest {
                binding.list.smoothScrollToPosition(0)
                adapter.submitData(it)
            }
        }

        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest {
                binding.swiperefresh.isRefreshing = it.refresh is LoadState.Loading
                        || it.append is LoadState.Loading
                        || it.prepend is LoadState.Loading
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.newerCount.collect{
                    if (it > 0) {
                        binding.loadNewPosts.text = resources.getString(R.string.load_new_posts, it)
                        binding.loadNewPosts.show()
                    }
                }
            }
        }

        binding.loadNewPosts.setOnClickListener {
            adapter.refresh()
            viewModel.changeIsHiddenFlag()
            binding.list.smoothScrollToPosition(0)
            binding.loadNewPosts.hide()
        }

        binding.swiperefresh.setOnRefreshListener {
            adapter.refresh()
        }

        binding.fab.setOnClickListener {
            if (appAuth.authStateFlow.value.id == 0L) {
                LoginDialog().show(childFragmentManager, "")
            } else {
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            }
        }
        return binding.root
    }
}

