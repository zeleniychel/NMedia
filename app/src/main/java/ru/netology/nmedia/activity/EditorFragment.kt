package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.databinding.FragmentEditorBinding
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

class EditorFragment : Fragment() {

    companion object {
        var Bundle.content by StringArg
    }

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentEditorBinding.inflate(layoutInflater)


        binding.edit.setText(arguments?.content)

        binding.cancel.setOnClickListener {
            viewModel.clearEdit()
            findNavController().navigateUp()
        }
        binding.ok.setOnClickListener {
            if (!binding.edit.text.isNullOrBlank()) {
                val content = binding.edit.text.toString()
                viewModel.changeContentAndSave(content)
                findNavController().navigateUp()
            }
            viewModel.clearEdit()
        }
        viewModel.toastMessage.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(),it, Toast.LENGTH_LONG).show()
        }
        return binding.root
    }
}