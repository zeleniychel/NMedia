package ru.netology.nmedia.activity

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentEditorBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.util.loadAttachment
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class EditorFragment : Fragment() {


    companion object {
        var Bundle.content by StringArg
        var Bundle.url by StringArg
    }

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private val photoResultContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data ?: return@registerForActivityResult
                val file = uri.toFile()

                viewModel.setPhoto(uri, file)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentEditorBinding.inflate(layoutInflater)

        viewModel.photo.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.imageContainer.isGone = true
                return@observe
            }
            binding.imageContainer.isVisible = true
            binding.preview.setImageURI(it.uri)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.draft = binding.edit.text.toString()
                    findNavController().navigateUp()
                }
            }
        )

        binding.edit.setText(arguments?.content)
        binding.preview.loadAttachment("http://10.0.2.2:9999/media/${arguments?.url}")

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.save_post, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        viewModel.changeContentAndSave(binding.edit.text.toString())
                        AndroidUtils.hideKeyboard(requireView())
                        findNavController().navigateUp()
                        true
                    }

                    else -> false
                }
        }, viewLifecycleOwner)


        binding.takePhoto.setOnClickListener {
            ImagePicker.Builder(this)
                .crop()
                .cameraOnly()
                .maxResultSize(2048, 2048)
                .createIntent(photoResultContract::launch)
        }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.Builder(this)
                .crop()
                .galleryOnly()
                .maxResultSize(2048, 2048)
                .createIntent(photoResultContract::launch)
        }

        binding.cancel.setOnClickListener {
            viewModel.clearEdit()
            findNavController().navigateUp()
        }

        binding.remove.setOnClickListener {
            viewModel.setPhoto(null, null)
        }

        return binding.root
    }
}