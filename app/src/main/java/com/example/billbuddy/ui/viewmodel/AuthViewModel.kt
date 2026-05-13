package com.example.billbuddy.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billbuddy.data.model.User
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

    private val _userData = mutableStateOf<Resource<User>?>(null)
    val userData: State<Resource<User>?> = _userData

    private val _updateState = mutableStateOf<Resource<Unit>?>(null)
    val updateState: State<Resource<Unit>?> = _updateState

    init {
        currentUser?.let { 
            getUserData(it.uid)
        }
    }

    val currentUser: FirebaseUser?
        get() = authRepository.currentUser

    fun getUserData(uid: String) {
        authRepository.getUserData(uid).onEach { result ->
            _userData.value = result
        }.launchIn(viewModelScope)
    }

    fun updateProfile(user: User) {
        authRepository.updateUserProfile(user).onEach { result ->
            _updateState.value = result
            if (result is Resource.Success) {
                getUserData(user.documentId)
            }
        }.launchIn(viewModelScope)
    }

    fun login(email: String, pass: String) {
        authRepository.login(email, pass).onEach { result ->
            _authState.value = result
            if (result is Resource.Success) {
                result.data?.let { user ->
                    getUserData(user.uid)
                }
            }
        }.launchIn(viewModelScope)
    }
    fun register(email: String, pass: String){
        authRepository.register(email, pass).onEach { result ->
            _authState.value = result
            if (result is Resource.Success) {
                result.data?.let { user ->
                    getUserData(user.uid)
                }
            }
        }.launchIn(viewModelScope)
    }
    fun logout() {
        authRepository.logout()
        _authState.value = null
    }
}
