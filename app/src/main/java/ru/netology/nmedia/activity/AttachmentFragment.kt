package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.databinding.FragmentAttachmentBinding
import ru.netology.nmedia.dto.Converter
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.getParcelableCompat
import ru.netology.nmedia.util.loadAttachment

@AndroidEntryPoint
class AttachmentFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        (activity as AppCompatActivity).supportActionBar?.hide()

        val postArg = arguments?.getParcelableCompat<Post>("key")

        val binding = FragmentAttachmentBinding.inflate(layoutInflater)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                    (activity as AppCompatActivity).supportActionBar?.show()
                }
            }
        )

        binding.apply {
            topAppBar.title = "1 of 1"
            shares.text = "0"
            comments.text = "0"
            preview.loadAttachment("http://10.0.2.2:9999/media/${postArg?.attachment?.url}")
        }

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
            (activity as AppCompatActivity).supportActionBar?.show()
        }


        binding.likes.text = postArg?.let { Converter.convertNumber(it.likes) }
        binding.likes.isChecked = postArg?.likedByMe?:false


        return binding.root
    }
}