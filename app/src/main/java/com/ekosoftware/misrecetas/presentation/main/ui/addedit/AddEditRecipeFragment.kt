package com.ekosoftware.misrecetas.presentation.main.ui.addedit

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.data.network.UploadImageWorker.Companion.KEY_OUTPUT_DOWNLOAD_IMAGE_URI
import com.ekosoftware.misrecetas.data.network.UploadImageWorker.Companion.Progress
import com.ekosoftware.misrecetas.databinding.FragmentAddEditRecipeBinding
import com.ekosoftware.misrecetas.domain.model.Recipe
import com.ekosoftware.misrecetas.presentation.main.ui.addedit.TwoOptionsBottomSheetDialog.Companion.OPTION_1
import com.ekosoftware.misrecetas.presentation.main.ui.addedit.TwoOptionsBottomSheetDialog.Companion.OPTION_2
import com.ekosoftware.misrecetas.presentation.main.ui.viewmodel.Event
import com.ekosoftware.misrecetas.presentation.main.ui.viewmodel.MainViewModel
import com.ekosoftware.misrecetas.util.GlideApp
import com.ekosoftware.misrecetas.util.SublistItemTouchHelper
import com.ekosoftware.misrecetas.util.hideKeyboard
import com.ekosoftware.misrecetas.util.showKeyboard
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.util.*

class AddEditRecipeFragment : Fragment() {

    private var _binding: FragmentAddEditRecipeBinding? = null
    private val binding get() = _binding!!

    private var receivedRecipe: Recipe? = null
    private var ingredientsList = mutableListOf<String>()
    private var instructionsList = mutableListOf<String>()
    lateinit var ingredientsAdapter: SublistRecyclerAdapter
    lateinit var instructionsAdapter: SublistRecyclerAdapter

    private var imageHolder: Uri? = null
    private var imageUUID: String? = null

    private val mainViewModel by activityViewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            receivedRecipe = it.getParcelable("recipe")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentAddEditRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        // Init UI related components and they listeners
        initToolbar()
        initIngredientsRecyclerView()
        initInstructionsRecyclerView()
        initButtons()

        populateUI()
        //subscribeSelectedRecipeObserver()
    }

    // Adding/Editing fragment implement a different toolbar
    private fun initToolbar() {
        val appBarConfiguration = AppBarConfiguration(findNavController().graph)
        binding.toolbarAddEdit.setupWithNavController(findNavController(), appBarConfiguration)
        binding.toolbarAddEdit.title = requireContext().getString(R.string.add_recipe)

        // Handle menu item clicks - works different than in an Activity
        binding.toolbarAddEdit.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_item_save -> {
                    if (areFieldsComplete()) save(currentRecipe())
                    true
                }
                else -> false
            }
        }
    }

    private fun save(recipe: Recipe) {
        hideKeyboard()
        if (receivedRecipe != null) { // When Fragment was created to edit a Recipe
            if (recipe != receivedRecipe) {
                mainViewModel.showRecipeDetails(recipe) // Will update details fragment
                mainViewModel.startNetworkOperation(Event.UPDATE, recipe)
            }
        } else { // When adding a new recipe
            mainViewModel.startNetworkOperation(Event.ADD, recipe)
        }
        findNavController().navigateUp()
    }

    private fun currentRecipe() = Recipe(
        name = binding.txtName.text.toString(),
        description = binding.txtDescription.text.toString(),
        timeRequired = binding.txtTimeRequired.text.toString().toLong(),
        servings = binding.txtServings.text.toString().toLong(),
        isFavorite = false,
        id = receivedRecipe?.id,
        imageUrl = imageHolder?.toString() ?: receivedRecipe?.imageUrl,
        imageUUID = receivedRecipe?.imageUUID ?: imageUUID,
        ingredients = ingredientsList,
        instructions = instructionsList,
        creator = receivedRecipe?.creator,
        creationDate = receivedRecipe?.creationDate
    )

    private fun initButtons() = binding.run {
        buttonAddIngredient.setOnClickListener { addEmptyItems(ingredientsList, ingredientsAdapter, ingredientsList.size) }
        buttonAddInstruction.setOnClickListener { addEmptyItems(instructionsList, instructionsAdapter, instructionsList.size) }
        listOf(noImageAddedText, buttonEditImage, recipeImage).forEach {
            it.setOnClickListener { requireContext().showImageOptionsDialog() }
        }
    }

    private fun populateUI() {
        setIngredients(receivedRecipe?.ingredients)
        setInstructions(receivedRecipe?.instructions)
        setInfo(receivedRecipe)
        binding.recipeImage.setImage(receivedRecipe)
        fixFocus()
    }

    // Scrolls up to recipe's image, and focus on name's EditText
    private fun fixFocus() = CoroutineScope(Main).launch {
        binding.recipeImage.requestFocus()
        binding.txtName.requestFocus()
        binding.txtName.setText(binding.txtName.text.toString()/*binding.txtName.text?.append("")*/)
        binding.scrollView.post { binding.scrollView.scrollTo(0, 0) }
        showKeyboard()
    }

    private fun addEmptyItems(list: MutableList<String>, adapter: SublistRecyclerAdapter, vararg indexes: Int) {
        indexes.forEach { index ->
            list.add(index, "")
            adapter.setFocusableItems(index)
        }
        adapter.apply {
            submitList(list)
            notifyDataSetChanged()
        }
    }

    /*private fun clearList(list: MutableList<String>, adapter: SublistRecyclerAdapter) {
        list.clear()
        adapter.submitList(list)
        adapter.notifyDataSetChanged()
    }*/

    private fun Context.showImageOptionsDialog() {
        val details = DialogDetails(
            null,
            getString(R.string.add_edit_image_option),
            R.drawable.ic_outline_camera_alt_24,
            getString(R.string.delete_image_option),
            R.drawable.ic_outline_delete_outline_24
        )
        TwoOptionsBottomSheetDialog(details).apply {
            setOnBottomSheetListener(object : TwoOptionsBottomSheetDialog.BottomSheetListener {
                override fun onOptionSelected(action: Int) {
                    when (action) {
                        OPTION_1 -> editImage()
                        OPTION_2 -> deleteImage()
                    }
                }
            })
        }.show(requireActivity().supportFragmentManager, "imageOptionsDialog")
    }

    // Launch image selection
    private fun editImage() = CropImage.activity().start(requireActivity(), this@AddEditRecipeFragment)

    private fun deleteImage() {
        // Clear image from UI
        GlideApp.with(this@AddEditRecipeFragment)
            .load(ContextCompat.getDrawable(requireContext(), R.drawable.non_image_placeholder)).into(binding.recipeImage)

        // Informs ViewModel
        imageUUID ?: receivedRecipe?.imageUUID?.let { mainViewModel.deleteImage(receivedRecipe?.id, it) }

        //binding.recipeImage.setImageDrawable(null)
        binding.buttonEditImage.visibility = View.GONE
        binding.noImageAddedText.visibility = View.VISIBLE
        imageHolder = null
    }

    // To handle ImageCropperActivity's results
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                CropImage.getActivityResult(data).uri.let { resultUri ->
                    GlideApp.with(this).load(resultUri).centerCrop().into(binding.recipeImage)
                    subscribeUploadImageWorkObserver()
                    imageUUID = receivedRecipe?.imageUUID ?: UUID.randomUUID().toString()
                    mainViewModel.uploadImage(resultUri, imageUUID!!)
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Snackbar.make(binding.parentLayout, R.string.error_getting_image, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry) { editImage() }
                    .show()
            }
        }
    }

    private fun subscribeUploadImageWorkObserver() =
        mainViewModel.uploadImageWork.observe(viewLifecycleOwner, uploadImageObserver())

    private var imageUploading = false

    private fun uploadImageObserver(): Observer<WorkInfo> = Observer { workInfo ->
        if (workInfo != null && workInfo.state.isFinished) {
            hideImageProcessing()
            workInfo.outputData.run {
                getString(KEY_OUTPUT_DOWNLOAD_IMAGE_URI)?.let { downloadUrl -> imageHolder = Uri.parse(downloadUrl) }
                binding.buttonEditImage.visibility = View.VISIBLE
                imageUploading = false
            }
        } else {
            val progress = workInfo.progress
            val value = progress.getInt(Progress, 0)
            imageUploading = value in 0..99
            showImageProcessing(value)
        }
    }

    private fun showImageProcessing(value: Int) = binding.apply {
        noImageAddedText.visibility = View.GONE
        recipeImage.foreground = ContextCompat.getDrawable(requireContext(), R.drawable.filter)
        progressBarImageLoading.visibility = View.VISIBLE
        progressBarImageLoading.progress = value
        buttonEditImage.visibility = View.GONE
    }

    private fun hideImageProcessing() = binding.apply {
        recipeImage.foreground = null
        progressBarImageLoading.visibility = View.GONE
        progressBarImageLoading.progress = 0
    }

    private fun ImageView.setImage(recipe: Recipe?) = recipe?.imageUrl?.takeIf { it.isNotEmpty() }?.let {
        GlideApp.with(requireContext()).load(it).centerCrop().into(this)
        updateUIForImageSet()
    }

    private fun updateUIForImageSet() {
        hideImageProcessing()
        binding.noImageAddedText.visibility = View.GONE
        binding.buttonEditImage.visibility = View.VISIBLE
    }

    private fun initIngredientsRecyclerView() {
        binding.ingredientsRecycler.layoutManager = LinearLayoutManager(requireContext())
        ingredientsAdapter = SublistRecyclerAdapter(requireContext(), Type.INGREDIENTS, ingredientsInteraction)
        binding.ingredientsRecycler.adapter = ingredientsAdapter
        setAdapter(binding.ingredientsRecycler, ingredientsAdapter)
    }

    private val ingredientsInteraction by lazy {
        object : SublistInteraction {

            override fun addLine(position: Int, fromItemCurrentCursorIndex: Int) =
                ingredientsAdapter.addLine(ingredientsList, position, fromItemCurrentCursorIndex, binding.ingredientsRecycler)

            override fun onDelete(position: Int) = ingredientsAdapter.deleteItem(ingredientsList, position)

            override fun onMoved(fromPosition: Int, toPosition: Int) = ingredientsAdapter.moveItems(
                ingredientsList, fromPosition, toPosition
            )

            override fun onItemUpdated(position: Int, newText: String) {
                ingredientsList[position] = newText
                ingredientsAdapter.submitList(ingredientsList)
            }
        }
    }

    private fun initInstructionsRecyclerView() {
        binding.instructionsRecycler.layoutManager = LinearLayoutManager(requireContext())
        instructionsAdapter = SublistRecyclerAdapter(requireContext(), Type.INSTRUCTIONS, instructionsInteraction)
        binding.instructionsRecycler.adapter = instructionsAdapter
        setAdapter(binding.instructionsRecycler, instructionsAdapter)
    }

    private val instructionsInteraction by lazy {
        object : SublistInteraction {

            override fun addLine(position: Int, fromItemCurrentCursorIndex: Int) = instructionsAdapter.addLine(
                instructionsList, position, fromItemCurrentCursorIndex, binding.instructionsRecycler
            )

            override fun onDelete(position: Int) = instructionsAdapter.deleteItem(instructionsList, position)

            override fun onMoved(fromPosition: Int, toPosition: Int) = instructionsAdapter.moveItems(
                instructionsList, fromPosition, toPosition
            )

            override fun onItemUpdated(position: Int, newText: String) {
                instructionsList[position] = newText
                instructionsAdapter.submitList(instructionsList)
            }
        }
    }

    private fun setAdapter(recyclerView: RecyclerView, sublistRecyclerAdapter: SublistRecyclerAdapter) {
        val sublistCallback = SublistItemTouchHelper(sublistRecyclerAdapter)
        val itemTouchHelper = ItemTouchHelper(sublistCallback)
        sublistRecyclerAdapter.setTouchHelper(itemTouchHelper)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun SublistRecyclerAdapter.addLine(
        sublist: MutableList<String>, position: Int, fromItemCurrentCursorIndex: Int,
        recyclerView: RecyclerView
    ) {
        // Allows only new item to gain focus when list is updated
        setFocusableItems(position + 1)

        // "From item" represents the item from which a new item will be inserted
        val fromItemOldText = sublist[position]

        val fromItemNewText = fromItemOldText.substring(0, fromItemCurrentCursorIndex) // Text until cursor
        sublist[position] = fromItemNewText // Updates "From item" value

        val newItemText = fromItemOldText.substring(fromItemCurrentCursorIndex) // Text from cursor onwards
        sublist.add(position + 1, newItemText) // add new item to list

        notifyAddLine(sublist, position, sublist.size)
        recyclerView.smoothScrollToPosition(position + 1)
    }

    private fun SublistRecyclerAdapter.deleteItem(sublist: MutableList<String>, position: Int) {
        sublist.removeAt(position) // Remove item from list
        notifyOnDelete(sublist, position)
    }

    private fun SublistRecyclerAdapter.moveItems(sublist: MutableList<String>, fromPosition: Int, toPosition: Int) {
        notifyItemMoved(fromPosition, toPosition)
        val fromItem = sublist[fromPosition]
        sublist.removeAt(fromPosition)
        sublist.add(toPosition, fromItem)
        notifyItemsMoved(sublist, fromPosition, toPosition)
    }

    private fun setInfo(recipe: Recipe?) {
        recipe?.publishName()
        recipe?.publishDescription()
        recipe?.publishTimeRequired()
        recipe?.publishServings()
    }

    private fun Recipe.publishName() = this.name?.let {
        binding.txtName.setText(it)
        binding.txtNameLayout.isEndIconVisible = false
    }

    private fun Recipe.publishDescription() = this.description?.let {
        binding.txtDescription.setText(it)
        binding.txtDescriptionLayout.isEndIconVisible = false
    }

    private fun Recipe.publishTimeRequired() = this.timeRequired?.let {
        val timeInTxt = if (it <= 0) "" else it.toString()
        binding.txtTimeRequired.setText(timeInTxt)
        binding.txtTimeRequiredLayout.isEndIconVisible = false
    }

    private fun Recipe.publishServings() = this.servings?.let {
        val servingsInText = if (it <= 0) "" else it.toString()
        binding.txtServings.setText(servingsInText)
        binding.txtServingsLayout.isEndIconVisible = false
    }

    private fun setIngredients(ingredients: List<String>?) {
        ingredients?.let {
            //clearList(ingredientsList, ingredientsAdapter)
            ingredientsList.addAll(it)
            ingredientsAdapter.submitList(ingredientsList)
            ingredientsAdapter.notifyDataSetChanged()
            return
        }
        addEmptyItems(ingredientsList, ingredientsAdapter, 0, 1)
        binding.recipeImage.requestFocus()
        binding.txtName.setText(binding.txtName.text.toString())
        binding.txtName.requestFocus()
    }

    private fun setInstructions(instructions: List<String>?) {
        instructions?.let {
            //clearList(instructionsList, instructionsAdapter)
            Log.d(TAG, "setInstructions: ADDING")
            instructionsList.addAll(it)
            instructionsAdapter.submitList(instructionsList)
            instructionsAdapter.notifyDataSetChanged()
            return
        }
        addEmptyItems(instructionsList, instructionsAdapter, 0, 1)
        binding.recipeImage.requestFocus()
        binding.txtName.setText(binding.txtName.text.toString())
        binding.txtName.requestFocus()
    }

    private fun areFieldsComplete(): Boolean {
        // If any of the textView which's content is compulsory to fill is empty, show error
        for (textInputEditText: TextInputEditText in listOf(
            binding.txtName,
            binding.txtDescription,
            binding.txtTimeRequired,
            binding.txtServings
        )) {
            if (textInputEditText.text.toString().isEmpty()) {
                textInputEditText.error = errorMessage(null)
                return false
            }
        }
        return when {
            // If no ingredients have been added, show error Snackbar
            ingredientsList.isNullOrEmpty() -> errorMessage(Type.INGREDIENTS).publishSnackbar()
            // If no instructions have been added, show error Snackbar
            instructionsList.isNullOrEmpty() -> errorMessage(Type.INSTRUCTIONS).publishSnackbar()
            imageUploading -> {
                requireContext().showCancelImageUploadAndSaveOptionDialog()
                false
            }
            else -> true
        }
    }

    private fun Context.showCancelImageUploadAndSaveOptionDialog() {
        DialogDetails(
            title = getString(R.string.cancel_image_upload_and_continue),
            optionOne = getString(R.string.continue_),
            optionOneResId = R.drawable.ic_baseline_done_24,
            optionTwo = getString(R.string.cancel),
            optionTwoResId = R.drawable.ic_outline_clear_24
        ).let { details ->
            TwoOptionsBottomSheetDialog(details).let { dialog ->
                dialog.setOnBottomSheetListener(object : TwoOptionsBottomSheetDialog.BottomSheetListener {
                    override fun onOptionSelected(action: Int) {
                        when (action) {
                            OPTION_1 -> {
                                imageUploading = false
                                mainViewModel.cancelImageUpload()
                                save(currentRecipe())
                            }
                        }
                    }
                })
                dialog.show(requireActivity().supportFragmentManager, "cancel image upload and save")
            }
        }
    }

    private fun errorMessage(type: Type?): String = when (type) {
        Type.INGREDIENTS -> requireContext().getString(R.string.must_add_at_least_one_ingredient)
        Type.INSTRUCTIONS -> requireContext().getString(R.string.must_add_at_least_one_instruction)
        null -> requireContext().getString(R.string.compulsatory_field) // message for TextInputEditTexts
    }

    private fun String.publishSnackbar(): Boolean {
        Snackbar.make(binding.parentLayout, this, Snackbar.LENGTH_LONG).show()
        return false
    }

    private val TAG = "AddEditRecipeFragment"
    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyboard()
        _binding = null // To avoid leaks
        Log.d(TAG, "onDestroyView: $ingredientsList")
        //ingredientsList.clear()
        //instructionsList.clear()
    }
}