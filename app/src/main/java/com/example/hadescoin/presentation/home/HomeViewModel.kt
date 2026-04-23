package com.example.hadescoin.presentation.home

import androidx.lifecycle.ViewModel
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
	val user: AppUser? = null
)

class HomeViewModel(
	authRepository: AuthRepository
) : ViewModel() {

	private val _uiState = MutableStateFlow(HomeUiState(user = authRepository.currentUser()))
	val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}
