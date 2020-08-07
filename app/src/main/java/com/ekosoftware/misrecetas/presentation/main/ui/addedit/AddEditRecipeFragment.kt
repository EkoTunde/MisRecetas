package com.ekosoftware.misrecetas.presentation.main.ui.addedit

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.domain.model.Recipe
import com.ekosoftware.misrecetas.data.network.RecipesDataSource
import com.ekosoftware.misrecetas.data.network.UsersDataSource
import com.ekosoftware.misrecetas.databinding.FragmentAddEditRecipeBinding
import com.ekosoftware.misrecetas.domain.network.RecipeRepoImpl
import com.ekosoftware.misrecetas.domain.network.UserRepoImpl
import com.ekosoftware.misrecetas.presentation.main.ui.addedit.adapters.IngredientRecyclerAdapter
import com.ekosoftware.misrecetas.presentation.main.ui.addedit.adapters.InstructionsRecyclerAdapter
import com.ekosoftware.misrecetas.presentation.main.ui.viewmodel.Event
import com.ekosoftware.misrecetas.presentation.main.ui.viewmodel.MainViewModel
import com.ekosoftware.misrecetas.presentation.main.ui.viewmodel.RecipeEvent
import com.ekosoftware.misrecetas.util.IngredientsInstructionsIItemTouchHelper
import com.ekosoftware.misrecetas.vo.VMFactory
import com.google.firebase.Timestamp
import java.lang.Exception
import java.util.*

class AddEditRecipeFragment : Fragment() {

    private var _binding: FragmentAddEditRecipeBinding? = null
    private val binding get() = _binding!!

    private var inputRecipe: Recipe? = null
    private var ingredients: List<String>? = null
    private var instructions: List<String>? = null

    private val ingredientsList = mutableListOf<String>()
    private val instructionsList = mutableListOf<String>()

    private lateinit var ingredientsRecyclerAdapter: IngredientRecyclerAdapter
    private lateinit var instructionsRecyclerAdapter: InstructionsRecyclerAdapter

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
                    Toast.makeText(requireContext(), "Ahora?", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun setUpButtons() {
        binding.buttonAddIngredient.setOnClickListener {
            val index = when {
                ingredientsList.isEmpty() -> 0
                ingredientSelected != null -> ingredientSelected!! + 1
                else -> ingredientsList.size
            }
            addEmptyIngredients(index)
        }
        binding.buttonAddInstruction.setOnClickListener {
            val index = when {
                instructionsList.isEmpty() -> {
                    Log.d("DEBUG!!!", "setUpButtons: lista vacia, devolvemos 0")
                    0
                }
                instructionSelected != null -> {
                    Log.d(
                        "DEBUG!!!", "setUpButtons: instructionsSelected no es nulo, es: $instructionSelected, más uno es: " +
                                "${instructionSelected!! + 1}"
                    )
                    instructionSelected!! + 1
                }
                else -> {
                    Log.d("DEBUG!!!", "setUpButtons: se devuelve el tamaño de la lista: ${instructionsList.size}")
                    instructionsList.size
                }
            }
            addEmptyInstructions(index)
        }
    }

    private fun addEmptyIngredients(vararg indexes: Int) {
        indexes.forEach { index ->
            ingredientsList.add(index, "")
            ingredientsRecyclerAdapter.submitList(ingredientsList)
            ingredientsRecyclerAdapter.notifyItemInserted(index)
        }
    }

    private fun addEmptyInstructions(vararg indexes: Int) {
        indexes.forEach { index ->
            instructionsList.add(index, "")
            instructionsRecyclerAdapter.submitList(instructionsList)
            instructionsRecyclerAdapter.notifyItemInserted(index)
            binding.ingredientsRecycler.smoothScrollToPosition(index)
        }
    }

    private var ingredientSelected: Int? = null

    private val ingredientsInteraction = object : IngredientRecyclerAdapter.Interaction {
        override fun addLine(position: Int) {
            ingredientsList.add(position + 1, "")
            ingredientsRecyclerAdapter.submitList(ingredientsList)
            ingredientsRecyclerAdapter.notifyItemInserted(position + 1)
            binding.ingredientsRecycler.smoothScrollToPosition(position)
        }

        override fun onDelete(position: Int) {
            ingredientsList.removeAt(position)
            ingredientsRecyclerAdapter.submitList(ingredientsList)
            ingredientsRecyclerAdapter.notifyItemRemoved(position)
            try {
                binding.ingredientsRecycler.smoothScrollToPosition(position + 1)
            } catch (e: Exception) {
                Log.d("DEBUG!!!", "onDelete: Position $position doesn't exist")
            }
        }

        override fun onFocus(position: Int) {
            ingredientSelected = position
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            ingredientsRecyclerAdapter.notifyItemMoved(fromPosition, toPosition)
            val fromItem = ingredientsList[fromPosition]
            ingredientsList.removeAt(fromPosition)
            ingredientsList.add(toPosition, fromItem)
            ingredientsRecyclerAdapter.submitList(ingredientsList)
        }

        override fun onItemUpdated(position: Int, newText: String) {
            ingredientsList[position] = newText
            ingredientsRecyclerAdapter.submitList(ingredientsList)
        }
    }

    private fun initIngredientsRecyclerView() {
        binding.ingredientsRecycler.apply {
            layoutManager = LinearLayoutManager(this@AddEditRecipeFragment.requireContext())
            ingredientsRecyclerAdapter = IngredientRecyclerAdapter(ingredientsInteraction)
            adapter = ingredientsRecyclerAdapter

            val ingredientsCallback = IngredientsInstructionsIItemTouchHelper(ingredientsRecyclerAdapter)
            val itemTouchHelper = ItemTouchHelper(ingredientsCallback)
            ingredientsRecyclerAdapter.setTouchHelper(itemTouchHelper)
            itemTouchHelper.attachToRecyclerView(this)
        }
    }

    private var instructionSelected: Int? = null

    private val instructionsInteraction = object : InstructionsRecyclerAdapter.Interaction {
        override fun addLine(position: Int) {
            instructionsList.add(position + 1, "")
            instructionsRecyclerAdapter.submitList(instructionsList)
            instructionsRecyclerAdapter.notifyItemInserted(position + 1)
            instructionsRecyclerAdapter.notifyItemRangeChanged(position, instructionsList.size - position)
            binding.instructionsRecycler.smoothScrollToPosition(position)
        }

        override fun onDelete(position: Int) {
            instructionsList.removeAt(position)
            instructionsRecyclerAdapter.submitList(instructionsList)
            instructionsRecyclerAdapter.notifyItemRemoved(position)
            instructionsRecyclerAdapter.notifyItemRangeChanged(position, instructionsList.size - position)
            try {
                binding.instructionsRecycler.smoothScrollToPosition(position + 1)
            } catch (e: Exception) {
                Log.d("DEBUG!!!", "onDelete: Position $position doesn't exist")
            }
        }

        override fun onFocus(position: Int) {
            instructionSelected = position
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            instructionsRecyclerAdapter.notifyItemMoved(fromPosition, toPosition)
            val fromItem = instructionsList[fromPosition]
            instructionsList.removeAt(fromPosition)
            instructionsList.add(toPosition, fromItem)
            instructionsRecyclerAdapter.submitList(instructionsList)
            if (fromPosition <= toPosition) {
                instructionsRecyclerAdapter.notifyItemRangeChanged(fromPosition, toPosition - fromPosition + 1)
            } else {
                instructionsRecyclerAdapter.notifyItemRangeChanged(toPosition, fromPosition - toPosition + 1)
            }
        }

        override fun onItemUpdated(position: Int, newText: String) {
            instructionsList[position] = newText
            instructionsRecyclerAdapter.submitList(instructionsList)
        }
    }

    private fun initInstructionsRecyclerView() {
        binding.instructionsRecycler.apply {
            layoutManager = LinearLayoutManager(this@AddEditRecipeFragment.requireContext())
            instructionsRecyclerAdapter =
                InstructionsRecyclerAdapter(this@AddEditRecipeFragment.requireContext(), instructionsInteraction)
            adapter = instructionsRecyclerAdapter

            val instructionsCallback = IngredientsInstructionsIItemTouchHelper(instructionsRecyclerAdapter)
            val itemTouchHelper = ItemTouchHelper(instructionsCallback)
            instructionsRecyclerAdapter.setTouchHelper(itemTouchHelper)
            itemTouchHelper.attachToRecyclerView(this)
        }
    }

    private fun fillFields() {
        inputRecipe?.let {
            setImage()
            binding.txtName.setText(it.name)
            binding.txtDescription.setText(it.description)
            binding.txtTimeRequired.setText(it.timeRequired.toString())
            binding.txtServings.setText(it.servings.toString())
            fillIngredients()
            fillInstructions()
        }
    }

    private fun setImage() {
        inputRecipe?.imageUrl?.let { image ->
            binding.recipePlaceholderImage.visibility = View.GONE
            binding.imageLayout.visibility = View.VISIBLE
            Glide.with(requireContext()).load(image).into(binding.recipeImage)
            return
        }
        binding.recipePlaceholderImage.visibility = View.VISIBLE
        binding.imageLayout.visibility = View.GONE
    }

    private fun fillIngredients() = inputRecipe!!.ingredients?.let {
        if (it.isNotEmpty()) ingredientsList.addAll(it) else addEmptyIngredients(0, 1)
    }

    private fun fillInstructions() = inputRecipe!!.instructions?.let {
        if (it.isNotEmpty()) ingredientsList.addAll(it) else addEmptyInstructions(0, 1)
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

    private fun errorCompulsoryField(ingredients: Boolean = false, instructions: Boolean = false) =
        when {
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