package com.ekosoftware.misrecetas.presentation.main.ui.addedit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.ekosoftware.misrecetas.data.network.RecipesDataSource
import com.ekosoftware.misrecetas.data.network.UploadImageWorker.Companion.KEY_IMAGE_URI
import com.ekosoftware.misrecetas.data.network.UploadImageWorker.Companion.KEY_OUTPUT_DOWNLOAD_IMAGE_URI
import com.ekosoftware.misrecetas.data.network.UploadImageWorker.Companion.Progress
import com.ekosoftware.misrecetas.databinding.FragmentAddEditRecipeBinding
import com.ekosoftware.misrecetas.domain.model.Recipe
import com.ekosoftware.misrecetas.domain.network.RecipeRepoImpl
import com.ekosoftware.misrecetas.presentation.main.ui.addedit.adapters.SublistInteraction
import com.ekosoftware.misrecetas.presentation.main.ui.addedit.adapters.SublistRecyclerAdapter
import com.ekosoftware.misrecetas.presentation.main.ui.addedit.adapters.Type
import com.ekosoftware.misrecetas.presentation.main.ui.viewmodel.Event
import com.ekosoftware.misrecetas.presentation.main.ui.viewmodel.MainVMFactory
import com.ekosoftware.misrecetas.presentation.main.ui.viewmodel.MainViewModel
import com.ekosoftware.misrecetas.util.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.theartofdev.edmodo.cropper.CropImage
import java.util.*

class AddEditRecipeFragment : Fragment() {

    companion object {
        const val SAVE = 0
        const val UPDATE = 1
    }

    private var _binding: FragmentAddEditRecipeBinding? = null
    private val binding get() = _binding!!

    private var receivedRecipe: Recipe? = null
    private var ingredientsList = mutableListOf<String>()
    private var instructionsList = mutableListOf<String>()
    lateinit var ingredientsAdapter: SublistRecyclerAdapter
    lateinit var instructionsAdapter: SublistRecyclerAdapter

    private var imageHolder: Uri? = null
    private var imageUUID: String? = null

    private val mainViewModel: MainViewModel by activityViewModels {
        MainVMFactory(
            requireActivity().application,
            RecipeRepoImpl(RecipesDataSource())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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

        subscribeObservers()
    }

    // Adding/Editing fragments implement a different toolbar
    private fun initToolbar() {
        val appBarConfiguration = AppBarConfiguration(findNavController().graph)
        binding.toolbarAddEdit.setupWithNavController(findNavController(), appBarConfiguration)

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
        if (receivedRecipe != null) {
            mainViewModel.startNetworkOperation(Event.UPDATE, recipe)
        } else {
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
        imageUrl = receivedRecipe?.imageUrl,
        imageUUID = receivedRecipe?.imageUUID ?: imageUUID,
        ingredients = ingredientsList,
        instructions = instructionsList
    )

    private fun initButtons() = binding.run {
        buttonAddIngredient.setOnClickListener {
            addEmptyItems(ingredientsList, ingredientsAdapter, ingredientsList.size)
        }
        buttonAddInstruction.setOnClickListener {
            addEmptyItems(instructionsList, instructionsAdapter, instructionsList.size)
        }
        listOf(noImageAddedText, buttonEditImage, recipeImage).forEach { it.setOnClickListener { showImageOptionsDialog() } }
        /*noImageAddedText.setOnClickListener { showImageOptionsDialog() }
        buttonEditImage.setOnClickListener { showImageOptionsDialog() }
        recipeImage.setOnClickListener { showImageOptionsDialog() }*/
    }

    private fun addEmptyItems(list: MutableList<String>, adapter: SublistRecyclerAdapter, vararg indexes: Int) {
        indexes.forEach { index ->
            list.add(index, "")
            adapter.setFocusableItems(index)
        }
        adapter.apply {
            submitList(list)
            notifyItemRangeInserted(indexes[0], indexes.size)
        }
    }

    private fun clearList(list: MutableList<String>, adapter: SublistRecyclerAdapter) {
        list.clear()
        adapter.submitList(list)
        adapter.notifyDataSetChanged()
    }

    private fun showImageOptionsDialog() =
        ImageOptionsBottomSheetDialog(bottomSheetListener).show(requireActivity().supportFragmentManager, "imageOptionsDialog")

    private val bottomSheetListener = object : ImageOptionsBottomSheetDialog.BottomSheetListener {
        override fun onOptionSelected(action: Int) {
            if (action == ImageOptionsBottomSheetDialog.EDIT) editImage()
            else { // Delete image
                GlideApp.with(this@AddEditRecipeFragment)
                    .load(ContextCompat.getDrawable(requireContext(), R.drawable.non_image_placeholder)).into(binding.recipeImage)
                binding.recipeImage.setImageDrawable(null)
                binding.buttonEditImage.hide()
                binding.noImageAddedText.show()
                imageHolder = null
                mainViewModel.setNewImageUri(null)
            }
        }
    }

    // Launch image selection
    private fun editImage() = CropImage.activity().start(requireActivity(), this@AddEditRecipeFragment)

    // To handle ImageCropperActivity's results
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                val result = CropImage.getActivityResult(data)
                val resultUri = result.uri
                GlideApp.with(this).load(resultUri).centerCrop().into(binding.recipeImage)
                imageUUID = receivedRecipe?.imageUUID ?: UUID.randomUUID().toString()
                mainViewModel.uploadImage(resultUri, imageUUID!!)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Snackbar.make(binding.parentLayout, R.string.error_getting_image, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry) { editImage() }
                    .show()
            }
        }
    }

    private fun subscribeObservers() {
        mainViewModel.selectedRecipe.observe(viewLifecycleOwner, selectedRecipeObserver())
        mainViewModel.uploadImageWork.observe(viewLifecycleOwner, uploadImageObserver())
    }

    private fun selectedRecipeObserver(): Observer<Recipe?> {
        return Observer {
            receivedRecipe = it
            setInfo(it)
            setIngredients(it?.ingredients)
            setInstructions(it?.instructions)
            binding.recipeImage.setImage(it?.imageUrl)
        }
    }

    private fun uploadImageObserver(): Observer<WorkInfo> {
        return Observer { workInfo ->
            if (workInfo != null && workInfo.state.isFinished) {
                hideImageProcessing()
                workInfo.outputData.run {
                    getString(KEY_IMAGE_URI)?.let { originalUri ->
                        imageHolder = Uri.parse(originalUri)
                    }
                    getString(KEY_OUTPUT_DOWNLOAD_IMAGE_URI)?.let { downloadUrl ->
                        receivedRecipe?.imageUrl = downloadUrl
                    }
                    binding.buttonEditImage.visibility = View.VISIBLE
                }
            } else {
                val progress = workInfo.progress
                val value = progress.getInt(Progress, 0)
                showImageProcessing(value)
            }
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

    private fun ImageView.setImage(imageUrl: String?) = imageUrl?.let {
        GlideApp.with(requireContext()).load(it).centerCrop().into(this)
        hideImageProcessing()
        binding.noImageAddedText.visibility = View.GONE
        binding.buttonEditImage.visibility = View.VISIBLE
    }

    private fun initIngredientsRecyclerView() {
        binding.ingredientsRecycler.layoutManager = LinearLayoutManager(requireContext())
        ingredientsAdapter = SublistRecyclerAdapter(requireContext(), Type.INGREDIENTS, ingredientsInteraction)
        binding.ingredientsRecycler.adapter = ingredientsAdapter
        setAdapter(binding.ingredientsRecycler, ingredientsAdapter)
        addEmptyItems(ingredientsList, ingredientsAdapter, 0, 1)
    }

    private val ingredientsInteraction by lazy {
        object : SublistInteraction {

            override fun addLine(position: Int, fromItemCurrentCursorIndex: Int) = ingredientsAdapter.addLine(
                ingredientsList, position, fromItemCurrentCursorIndex, binding.ingredientsRecycler
            )

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
        addEmptyItems(instructionsList, instructionsAdapter, 0, 1)
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

    private fun SublistRecyclerAdapter.addLine(sublist: MutableList<String>, position: Int, fromItemCurrentCursorIndex: Int, recyclerView: RecyclerView) {
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
        binding.txtDescription.setText(it)
        binding.txtDescriptionLayout.isEndIconVisible = false
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
            clearList(ingredientsList, ingredientsAdapter)
            ingredientsList.addAll(it)
            ingredientsAdapter.submitList(ingredientsList)
            ingredientsAdapter.notifyDataSetChanged()
            return
        }
        /*if (ingredients.isNullOrEmpty()) addEmptyItems(ingredientsList, ingredientsAdapter, 0, 1)
        else {
            ingredientsList.addAll(ingredients)
            ingredientsAdapter.submitList(ingredientsList)
            ingredientsAdapter.notifyDataSetChanged()
        }*/
    }

    private fun setInstructions(instructions: List<String>?) {
        instructions?.let {
            clearList(instructionsList, instructionsAdapter)
            instructionsList.addAll(it)
            instructionsAdapter.submitList(instructionsList)
            instructionsAdapter.notifyDataSetChanged()
            return
        }
        /*if (instructions.isNullOrEmpty()) addEmptyItems(instructionsList, instructionsAdapter, 0, 1)
        else {
            instructionsList.addAll(instructions)
            instructionsAdapter.submitList(instructionsList)
            instructionsAdapter.notifyDataSetChanged()
        }*/
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
            ingredientsList.isNullOrEmpty() -> errorMessage(Type.INSTRUCTIONS).publishSnackbar()
            // If no instructions have been added, show error Snackbar
            instructionsList.isNullOrEmpty() -> errorMessage(Type.INSTRUCTIONS).publishSnackbar()
            else -> true
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

    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyboard()
        ingredientsList.clear()
        instructionsList.clear()
    }
}