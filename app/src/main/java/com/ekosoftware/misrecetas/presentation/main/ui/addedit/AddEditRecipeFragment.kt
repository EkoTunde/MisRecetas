package com.ekosoftware.misrecetas.presentation.main.ui.addedit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.data.network.RecipesDataSource
import com.ekosoftware.misrecetas.data.network.UsersDataSource
import com.ekosoftware.misrecetas.databinding.FragmentAddEditRecipeBinding
import com.ekosoftware.misrecetas.domain.model.Recipe
import com.ekosoftware.misrecetas.domain.network.RecipeRepoImpl
import com.ekosoftware.misrecetas.domain.network.UserRepoImpl
import com.ekosoftware.misrecetas.presentation.main.ui.addedit.adapters.IngredientRecyclerAdapter
import com.ekosoftware.misrecetas.presentation.main.ui.addedit.adapters.InstructionsRecyclerAdapter
import com.ekosoftware.misrecetas.presentation.main.ui.viewmodel.Event
import com.ekosoftware.misrecetas.presentation.main.ui.viewmodel.MainViewModel
import com.ekosoftware.misrecetas.presentation.main.ui.viewmodel.RecipeEvent
import com.ekosoftware.misrecetas.util.GlideApp
import com.ekosoftware.misrecetas.util.IngredientsInstructionsIItemTouchHelper
import com.ekosoftware.misrecetas.vo.VMFactory
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AddEditRecipeFragment : Fragment() {

    private var _binding: FragmentAddEditRecipeBinding? = null
    private val binding get() = _binding!!

    private var inputRecipe: Recipe? = null
    private var ingredients: List<String>? = null
    private var instructions: List<String>? = null

    private val ingredientsList = mutableListOf<String>()
    private val instructionsList = mutableListOf<String>()

    private lateinit var ingredientsAdapter: IngredientRecyclerAdapter
    private lateinit var instructionsAdapter: InstructionsRecyclerAdapter

    private val homeViewModel by activityViewModels<MainViewModel> {
        VMFactory(
            UserRepoImpl(UsersDataSource()),
            RecipeRepoImpl(RecipesDataSource())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        requireArguments().let {
            inputRecipe = it.getParcelable("recipe")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        _binding = FragmentAddEditRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        setUpToolbar()
        initIngredientsRecyclerView()
        initInstructionsRecyclerView()
        setUpButtons()
        fillFields()
    }

    private fun setUpToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbarAddEdit.setupWithNavController(navController, appBarConfiguration)

        // Handle menu item clicks - works different in Activity
        binding.toolbarAddEdit.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_item_save -> {
                    save()
                    true
                }
                else -> false
            }
        }
    }

    private fun setUpButtons() {
        binding.buttonAddIngredient.setOnClickListener { addEmptyIngredients(ingredientsList.size) }
        binding.buttonAddInstruction.setOnClickListener { addEmptyInstructions(instructionsList.size) }
    }

    private fun addEmptyIngredients(vararg indexes: Int) {
        indexes.forEach { index ->
            ingredientsList.add(index, "")
            ingredientsAdapter.apply {
                submitList(ingredientsList)
                notifyItemInserted(index)
            }
        }
    }

    private fun addEmptyInstructions(vararg indexes: Int) {
        indexes.forEach { index ->
            instructionsList.add(index, "")
            instructionsAdapter.apply {
                submitList(instructionsList)
                notifyItemInserted(index)
                setFocusableItems(index)
            }
            binding.ingredientsRecycler.smoothScrollToPosition(index)
        }
    }

    private val ingredientsInteraction = object : IngredientRecyclerAdapter.Interaction {
        override fun addLine(position: Int) {
            ingredientsList.add(position + 1, "")
            ingredientsAdapter.submitList(ingredientsList)
            ingredientsAdapter.notifyItemInserted(position + 1)
            binding.ingredientsRecycler.smoothScrollToPosition(position + 1)
        }

        override fun onDelete(position: Int) {
            ingredientsList.removeAt(position)
            ingredientsAdapter.submitList(ingredientsList)
            ingredientsAdapter.notifyItemRemoved(position)
            try {
                binding.ingredientsRecycler.smoothScrollToPosition(position + 1)
            } catch (e: Exception) {
                Log.d("DEBUG!!!", "onDelete: Position $position doesn't exist")
            }
        }

        override fun onFocus(position: Int) {
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            ingredientsAdapter.notifyItemMoved(fromPosition, toPosition)
            val fromItem = ingredientsList[fromPosition]
            ingredientsList.removeAt(fromPosition)
            ingredientsList.add(toPosition, fromItem)
            ingredientsAdapter.submitList(ingredientsList)
        }

        override fun onItemUpdated(position: Int, newText: String) {
            ingredientsList[position] = newText
            ingredientsAdapter.submitList(ingredientsList)
        }
    }

    private fun initIngredientsRecyclerView() {
        binding.ingredientsRecycler.apply {
            layoutManager = LinearLayoutManager(this@AddEditRecipeFragment.requireContext())
            ingredientsAdapter = IngredientRecyclerAdapter(ingredientsInteraction)
            adapter = ingredientsAdapter

            preserveFocusAfterLayout = true

            val ingredientsCallback = IngredientsInstructionsIItemTouchHelper(ingredientsAdapter)
            val itemTouchHelper = ItemTouchHelper(ingredientsCallback)
            ingredientsAdapter.setTouchHelper(itemTouchHelper)
            itemTouchHelper.attachToRecyclerView(this)
        }
    }

    private val instructionsInteraction = object : InstructionsRecyclerAdapter.Interaction {

        override fun addLine(position: Int, fromItemCurrentCursorIndex: Int) {
            instructionsAdapter.apply {

                // Allows only new item to gain focus when list is updated
                setFocusableItems(position + 1)

                // "From item" represents the item from which a new item will be inserted
                val fromItemOldText = instructionsList[position]

                val fromItemNewText = fromItemOldText.substring(0, fromItemCurrentCursorIndex) // Text until cursor
                instructionsList[position] = fromItemNewText // Updates "From item" value

                val newItemText = fromItemOldText.substring(fromItemCurrentCursorIndex) // Text from cursor onwards
                instructionsList.add(position + 1, newItemText) // add new item to list

                submitList(instructionsList) // Update adapter's list
                notifyItemChanged(position) // Notify adapter "From item" has changed
                notifyItemInserted(position + 1) // Notify adapter of new item

                // Forces following items to update so they accurately indicate the step (a.k.a. adapterPosition + 1) by the hint
                notifyItemRangeChanged(position + 1, instructionsList.size)
            }
            binding.instructionsRecycler.smoothScrollToPosition(position + 1)
        }

        override fun onDelete(position: Int) {

            instructionsList.removeAt(position) // Remove item from list
            instructionsAdapter.apply {
                submitList(instructionsList) // Update adapter's list
                notifyItemRemoved(position)

                // Forces following items to update so they accurately indicate the step (a.k.a. adapterPosition + 1) by the hint
                notifyItemRangeChanged(position, instructionsList.size)
            }
            try {
                binding.instructionsRecycler.smoothScrollToPosition(position)
            } catch (e: Exception) {
                Log.d("DEBUG!!!", "onDelete: Position $position doesn't exist")
            }
        }

        /* override fun onFocus(position: Int) {
         }*/

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            instructionsAdapter.notifyItemMoved(fromPosition, toPosition)
            val fromItem = instructionsList[fromPosition]
            instructionsList.removeAt(fromPosition)
            instructionsList.add(toPosition, fromItem)
            instructionsAdapter.submitList(instructionsList)
            if (fromPosition <= toPosition) {
                instructionsAdapter.notifyItemRangeChanged(fromPosition, toPosition - fromPosition + 1)
            } else {
                instructionsAdapter.notifyItemRangeChanged(toPosition, fromPosition - toPosition + 1)
            }
        }

        override fun onItemUpdated(position: Int, newText: String) {
            instructionsList[position] = newText
            instructionsAdapter.submitList(instructionsList)
        }
    }

    private fun initInstructionsRecyclerView() {
        binding.instructionsRecycler.apply {
            layoutManager = LinearLayoutManager(this@AddEditRecipeFragment.requireContext())
            instructionsAdapter =
                InstructionsRecyclerAdapter(this@AddEditRecipeFragment.requireContext(), instructionsInteraction)
            adapter = instructionsAdapter

            val instructionsCallback = IngredientsInstructionsIItemTouchHelper(instructionsAdapter)
            val itemTouchHelper = ItemTouchHelper(instructionsCallback)
            instructionsAdapter.setTouchHelper(itemTouchHelper)
            itemTouchHelper.attachToRecyclerView(this)
        }
    }

    private fun fillFields() {
        setImage()
        fillIngredients()
        fillInstructions()

        binding.apply {

            binding.txtName.apply {
                txtName.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                    binding.txtNameLayout.isEndIconVisible = hasFocus
                }
                setText(inputRecipe?.name ?: "")
            }

            binding.txtDescription.apply {
                onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                    binding.txtDescriptionLayout.isEndIconVisible = hasFocus
                }
                setText(inputRecipe?.description ?: "")
            }

            binding.txtTimeRequired.apply {
                onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                    binding.txtTimeRequiredLayout.isEndIconVisible = hasFocus
                }
                val timeRequired = if (inputRecipe?.timeRequired.toString() != "null") {
                    inputRecipe?.timeRequired.toString()
                } else {
                    ""
                }
                setText(timeRequired)
            }

            binding.txtServings.apply {
                onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                    binding.txtServingsLayout.isEndIconVisible = hasFocus
                }
                val servings = if (inputRecipe?.servings.toString() != "null") inputRecipe?.servings.toString() else ""
                setText(servings)
            }

            CoroutineScope(Dispatchers.Main).launch {
                binding.recipeImage.apply {
                    isFocusableInTouchMode = true
                    requestFocus()
                }
                binding.txtName.apply {
                    isFocusableInTouchMode = true
                    requestFocus()
                }
            }
        }
    }

    private fun setImage() {
        inputRecipe!!.imageUrl!!.let { image ->
            binding.recipePlaceholderImage.visibility = View.GONE
            binding.imageLayout.visibility = View.VISIBLE
            GlideApp.with(this@AddEditRecipeFragment)
                .load(image)
                .centerCrop()
                .into(binding.recipeImage)
            return
        }
        /*binding.recipePlaceholderImage.visibility = View.VISIBLE
        binding.imageLayout.visibility = View.GONE*/
    }

    private fun fillIngredients() {
        if (inputRecipe == null || inputRecipe!!.ingredients.isNullOrEmpty()) {
            addEmptyIngredients(0, 1)
        } else {
            ingredientsList.addAll(inputRecipe!!.ingredients!!)
            ingredientsAdapter.apply {
                submitList(ingredientsList)
                notifyDataSetChanged()
            }
        }
    }

    private fun fillInstructions() {
        if (inputRecipe == null || inputRecipe!!.instructions.isNullOrEmpty()) {
            addEmptyInstructions(0, 1)
        } else {
            instructionsList.addAll(inputRecipe!!.instructions!!)
            instructionsAdapter.apply {
                submitList(instructionsList)
                notifyDataSetChanged()
            }
        }
    }

    private fun save() {
        if (checkFields()) {
            val recipe = getFieldsValues()
            if (inputRecipe == null) {
                homeViewModel.registerEvent(
                    RecipeEvent(
                        recipe,
                        Event.ADD
                    )
                )
            } else {
                homeViewModel.registerEvent(
                    RecipeEvent(
                        recipe,
                        Event.UPDATE
                    )
                )
            }

        }
    }

    private fun checkFields(): Boolean {
        return binding.run {

            // If any of the textView which's content is compulsory to fill is empty, show error
            listOf(txtName, txtTimeRequired, txtServings).forEach {
                if (it.hasNoText()) {
                    it.error = errorCompulsoryField()
                    return@run true
                }
            }

            when {
                // If no ingredients have been added, show error
                ingredients.isNullOrEmpty() -> {
                    txtIngredientsTitle.error = errorCompulsoryField(ingredients = true)
                    return@run true
                }

                // If no instructions have been added, show error
                instructions.isNullOrEmpty() -> {
                    txtInstructionsTitle.error = errorCompulsoryField(instructions = true)
                    return@run true
                }
                else -> return@run false
            }
        }
    }

    private fun TextView.hasNoText(): Boolean = this.text.toString().isEmpty()

    private fun errorCompulsoryField(ingredients: Boolean = false, instructions: Boolean = false): String {
        return when {
            !ingredients && !instructions -> {
                requireContext().getString(R.string.compulsatory_field)
            }
            ingredients && !instructions -> {
                requireContext().getString(R.string.must_add_at_least_one_ingredient)
            }
            instructions && !ingredients -> {
                requireContext().getString(R.string.must_add_at_least_one_instruction)
            }
            else -> throw IllegalArgumentException("Ingredient and instructions can't be voth true.")
        }
    }

    private fun getFieldsValues(): Recipe {
        return Recipe(
            id = inputRecipe?.id,
            name = binding.txtName.text.toString(),
            description = binding.txtName.text.toString(),
            timeRequired = binding.txtTimeRequired.text.toString().toLong(),
            imageUrl = "",
            servings = binding.txtServings.text.toString().toLong(),

            creationDate = inputRecipe?.creationDate ?: Timestamp(Date()),
            creator = inputRecipe?.creator ?: homeViewModel.currentUser,
            isFavorite = false,
            keywords = inputRecipe?.keywords
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}