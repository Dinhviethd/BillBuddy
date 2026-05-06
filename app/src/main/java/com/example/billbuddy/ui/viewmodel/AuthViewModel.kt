package com.example.billbuddy.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billbuddy.data.repo.AuthRepository
import com.example.billbuddy.utils.Resource
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = mutableStateOf<Resource<FirebaseUser>?>(null)
    val authState: State<Resource<FirebaseUser>?> = _authState

    val currentUser: FirebaseUser?
        get() = authRepository.currentUser

    fun login(email: String, pass: String) {
        authRepository.login(email, pass).onEach { result ->
            _authState.value = result
        }.launchIn(viewModelScope)
    }
    fun register(email: String, pass: String){
        authRepository.register(email, pass). onEach{
            result -> _authState.value = result
        }.launchIn(viewModelScope)
    }
    fun logout() {
        authRepository.logout()
        _authState.value = null
    }
}
