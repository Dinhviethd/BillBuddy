package com.example.billbuddy.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billbuddy.data.model.Debt
import com.example.billbuddy.data.model.DebtStatus
import com.example.billbuddy.data.repo.DebtRepository
import com.example.billbuddy.data.repo.UserRepository
import com.example.billbuddy.utils.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebtViewModel @Inject constructor(
    private val debtRepository: DebtRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _debtsState = mutableStateOf<Resource<List<Debt>>>(Resource.Loading())
    val debtsState: State<Resource<List<Debt>>> = _debtsState

    private val _addDebtState = mutableStateOf<Resource<Unit>?>(null)
    val addDebtState: State<Resource<Unit>?> = _addDebtState

    private val _debtDetailState = mutableStateOf<Resource<Debt>>(Resource.Loading())
    val debtDetailState: State<Resource<Debt>> = _debtDetailState

    private val _partnerEmails = mutableStateMapOf<String, String>()
    val partnerEmails: Map<String, String> = _partnerEmails

    val currentUserId: String?
        get() = auth.currentUser?.uid

    init {
        loadDebts()
    }

    fun loadDebts() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            combine(
                debtRepository.getDebtsByCreditor(userId),
                debtRepository.getDebtsByDebtor(userId)
            ) { creditorResult, debtorResult ->
                when {
                    creditorResult is Resource.Loading || debtorResult is Resource.Loading ->
                        Resource.Loading()

                    creditorResult is Resource.Error ->
                        Resource.Error(creditorResult.message ?: "Lỗi tải khoản cho vay")

                    debtorResult is Resource.Error ->
                        Resource.Error(debtorResult.message ?: "Lỗi tải khoản đi vay")

                    else -> {
                        val combined = (creditorResult.data ?: emptyList()) +
                                (debtorResult.data ?: emptyList())
                        Resource.Success(combined.sortedByDescending { it.createdAt })
                    }
                }
            }.collect { result ->
                _debtsState.value = result
            }
        }
    }

    fun addDebtByEmail(
        debtorEmail: String,
        amount: Long,
        description: String,
        note: String,
        dueDate: Timestamp?
    ) {
        val creditorId = auth.currentUser?.uid ?: run {
            _addDebtState.value = Resource.Error("Bạn chưa đăng nhập")
            return
        }

        viewModelScope.launch {
            _addDebtState.value = Resource.Loading()

            // Bước 1: Tìm debtor theo email
            when (val userResult = userRepository.findUserByEmail(debtorEmail)) {
                is Resource.Error -> {
                    _addDebtState.value = Resource.Error(
                        userResult.message ?: "Không tìm thấy người dùng"
                    )
                    return@launch
                }
                is Resource.Success -> {
                    val debtorId = userResult.data?.documentId ?: run {
                        _addDebtState.value = Resource.Error("Không lấy được ID người dùng")
                        return@launch
                    }

                    // Bước 2: Không cho tự vay chính mình
                    if (debtorId == creditorId) {
                        _addDebtState.value = Resource.Error("Không thể tạo khoản nợ với chính mình")
                        return@launch
                    }

                    // Bước 3: Tạo và lưu debt
                    val debt = Debt(
                        creditorId = creditorId,
                        debtorId = debtorId,
                        amount = amount,
                        description = description,
                        note = note,
                        dueDate = dueDate,
                        status = DebtStatus.PENDING,
                        createdAt = Timestamp.now()
                    )

                    debtRepository.addDebt(debt).collect { result ->
                        _addDebtState.value = result
                    }
                }
                else -> Unit
            }
        }
    }

    fun loadPartnerEmail(debt: Debt, currentUserId: String) {
        val partnerId = if (debt.creditorId == currentUserId) debt.debtorId else debt.creditorId
        val debtId = debt.documentId

        // Đã có trong cache thì bỏ qua
        if (_partnerEmails.containsKey(debtId)) return

        viewModelScope.launch {
            when (val result = userRepository.findUserById(partnerId)) {
                is Resource.Success -> {
                    _partnerEmails[debtId] = result.data?.email ?: partnerId.take(8)
                }
                else -> {
                    _partnerEmails[debtId] = partnerId.take(8)
                }
            }
        }
    }

    fun updateDebtStatus(debtId: String, status: DebtStatus) {
        debtRepository.updateDebtStatus(debtId, status).launchIn(viewModelScope)
    }

    fun deleteDebt(debtId: String) {
        debtRepository.deleteDebt(debtId).launchIn(viewModelScope)
    }

    fun loadDebtById(debtId: String) {
        debtRepository.getDebtById(debtId).onEach { result ->
            _debtDetailState.value = result
        }.launchIn(viewModelScope)
    }

    fun resetAddDebtState() {
        _addDebtState.value = null
    }
}