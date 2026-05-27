package com.example.billbuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billbuddy.data.model.Expense
import com.example.billbuddy.data.model.ExpenseSplit
import com.example.billbuddy.data.model.Group
import com.example.billbuddy.data.model.Category
import com.example.billbuddy.data.repo.CategoryRepository
import com.example.billbuddy.data.repo.ExpenseRepository
import com.example.billbuddy.data.repo.ExpenseSplitRepository
import com.example.billbuddy.data.repo.GroupRepository
import com.example.billbuddy.data.repo.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.example.billbuddy.utils.Resource
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val expenseSplitRepository: ExpenseSplitRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth,
) : ViewModel() {

    private val _groups = MutableStateFlow<Resource<List<Group>>>(Resource.Loading())
    val groups: StateFlow<Resource<List<Group>>> = _groups.asStateFlow()

    private val _createGroupStatus = MutableStateFlow<Resource<Unit>?>(null)
    val createGroupStatus: StateFlow<Resource<Unit>?> = _createGroupStatus.asStateFlow()

    private val _joinGroupStatus = MutableStateFlow<Resource<Unit>?>(null)
    val joinGroupStatus: StateFlow<Resource<Unit>?> = _joinGroupStatus.asStateFlow()

    private val _splitExpenseStatus = MutableStateFlow<Resource<Unit>?>(null)
    val splitExpenseStatus: StateFlow<Resource<Unit>?> = _splitExpenseStatus.asStateFlow()

    private val _currentGroup = MutableStateFlow<Resource<Group>>(Resource.Loading())
    val currentGroup: StateFlow<Resource<Group>> = _currentGroup.asStateFlow()

    private val _leaveGroupStatus = MutableStateFlow<Resource<Unit>?>(null)
    val leaveGroupStatus: StateFlow<Resource<Unit>?> = _leaveGroupStatus.asStateFlow()

    private val _groupExpenses = MutableStateFlow<Resource<List<Expense>>>(Resource.Loading())
    val groupExpenses: StateFlow<Resource<List<Expense>>> = _groupExpenses.asStateFlow()

    private val _userSplits = MutableStateFlow<Resource<List<ExpenseSplit>>>(Resource.Loading())
    val userSplits: StateFlow<Resource<List<ExpenseSplit>>> = _userSplits.asStateFlow()

    private val _settledHistory = MutableStateFlow<Resource<List<ExpenseSplit>>>(Resource.Loading())
    val settledHistory: StateFlow<Resource<List<ExpenseSplit>>> = _settledHistory.asStateFlow()

    private val _memberEmails = MutableStateFlow<Map<String, String>>(emptyMap())
    val memberEmails: StateFlow<Map<String, String>> = _memberEmails.asStateFlow()

    private val _groupCategories = MutableStateFlow<Resource<List<Category>>>(Resource.Loading())
    val groupCategories: StateFlow<Resource<List<Category>>> = _groupCategories.asStateFlow()

    private val _settleStatus = MutableStateFlow<Resource<Unit>?>(null)
    val settleStatus: StateFlow<Resource<Unit>?> = _settleStatus.asStateFlow()

    private val _createCategoryStatus = MutableStateFlow<Resource<Unit>?>(null)
    val createCategoryStatus: StateFlow<Resource<Unit>?> = _createCategoryStatus.asStateFlow()

    init {
        observeGroups()
    }

    private fun observeGroups() {
        viewModelScope.launch {
            groupRepository.observeUserGroups().collect {
                _groups.value = it
            }
        }
    }

    fun createGroup(name: String, description: String, memberIds: List<String> = emptyList()) {
        viewModelScope.launch {
            val group = Group(name = name, description = description, memberIds = memberIds)
            groupRepository.createGroup(group).collect {
                _createGroupStatus.value = it
            }
        }
    }

    fun joinGroupByName(groupName: String) {
        viewModelScope.launch {
            groupRepository.joinGroupByName(groupName).collect {
                _joinGroupStatus.value = it
            }
        }
    }

    fun splitExpense(expense: Expense, splits: List<ExpenseSplit>) {
        viewModelScope.launch {
            expenseSplitRepository.splitExpense(expense, splits).collect {
                _splitExpenseStatus.value = it
            }
        }
    }

    fun splitExpenseEqually(expense: Expense, group: Group) {
        viewModelScope.launch {
            val members = group.memberIds
            if (members.isEmpty()) {
                _splitExpenseStatus.value = Resource.Error("Nhóm không có thành viên")
                return@launch
            }

            val splitAmount = expense.amount / members.size
            val splits = members.map { memberId ->
                ExpenseSplit(
                    amount = splitAmount,
                    description = expense.description,
                    paidBy = memberId,
                    groupId = group.documentId,
                    expenseId = expense.documentId
                )
            }

            expenseSplitRepository.splitExpense(expense, splits).collect {
                _splitExpenseStatus.value = it
            }
        }
    }

    fun resetStatus() {
        _createGroupStatus.value = null
        _joinGroupStatus.value = null
        _splitExpenseStatus.value = null
        _leaveGroupStatus.value = null
        _settleStatus.value = null
        _createCategoryStatus.value = null
    }

    fun getGroupById(groupId: String) {
        viewModelScope.launch {
            groupRepository.getGroupById(groupId).collect { resource ->
                _currentGroup.value = resource
                if (resource is Resource.Success) {
                    val group = resource.data!!
                    fetchMemberEmails(group.memberIds)
                    observeGroupExpenses(group.documentId)
                    observeUserSplits(group.documentId)
                    observeSettledHistory(group.documentId)
                    observeGroupCategories(group.documentId)
                }
            }
        }
    }

    private fun fetchMemberEmails(memberIds: List<String>) {
        viewModelScope.launch {
            val emails = _memberEmails.value.toMutableMap()
            memberIds.forEach { id ->
                if (!emails.containsKey(id)) {
                    val userRes = userRepository.findUserById(id)
                    if (userRes is Resource.Success) {
                        emails[id] = userRes.data?.email ?: id
                    } else {
                        emails[id] = id
                    }
                }
            }
            _memberEmails.value = emails
        }
    }

    private fun observeGroupExpenses(groupId: String) {
        viewModelScope.launch {
            expenseRepository.observeGroupExpenses(groupId).collect {
                _groupExpenses.value = it
            }
        }
    }

    private fun observeUserSplits(groupId: String) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            expenseSplitRepository.observeUserSplitsInGroup(uid, groupId).collect {
                _userSplits.value = it
            }
        }
    }

    private fun observeSettledHistory(groupId: String) {
        viewModelScope.launch {
            expenseSplitRepository.observeAllSettledSplitsInGroup(groupId).collect {
                _settledHistory.value = it
            }
        }
    }

    private fun observeGroupCategories(groupId: String) {
        viewModelScope.launch {
            categoryRepository.getCategories(groupId).collect {
                _groupCategories.value = it
            }
        }
    }

    fun settleSplit(split: ExpenseSplit) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            expenseSplitRepository.settleSplit(split.documentId).collect { resource ->
                _settleStatus.value = resource
                
                if (resource is Resource.Success) {
                    categoryRepository.getCategories(uid).collect { catResource ->
                        if (catResource is Resource.Success) {
                            val categories = catResource.data ?: emptyList()
                            val groupExpenseCat = categories.find { it.name == "Chi tiêu nhóm" }
                            
                            if (groupExpenseCat == null) {
                                val newCat = Category(name = "Chi tiêu nhóm", userId = uid)
                                categoryRepository.addCategory(newCat).collect { addCatRes ->
                                    if (addCatRes is Resource.Success) {
                                        createPersonalExpense(uid, split, "Chi tiêu nhóm")
                                    }
                                }
                            } else {
                                createPersonalExpense(uid, split, groupExpenseCat.documentId)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun createPersonalExpense(uid: String, split: ExpenseSplit, categoryId: String) {
        viewModelScope.launch {
            val personalExpense = Expense(
                amount = split.amount,
                description = "[Nhóm] ${split.description}",
                date = Timestamp.now(),
                categoryId = categoryId,
                userId = uid,
                createdAt = Timestamp.now()
            )
            expenseRepository.addExpense(personalExpense).collect {  }
        }
    }

    fun createGroupCategory(groupId: String, name: String) {
        viewModelScope.launch {
            val category = Category(name = name, userId = groupId)
            categoryRepository.addCategory(category).collect {
                _createCategoryStatus.value = it
            }
        }
    }

    fun leaveGroup(groupId: String) {
        viewModelScope.launch {
            groupRepository.leaveGroup(groupId).collect {
                _leaveGroupStatus.value = it
            }
        }
    }
}
