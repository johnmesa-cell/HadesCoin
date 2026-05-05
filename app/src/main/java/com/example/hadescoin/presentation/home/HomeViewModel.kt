package com.example.hadescoin.presentation.home

import androidx.lifecycle.ViewModel
import com.example.hadescoin.domain.model.AppUser

data class HomeUiState(
	val user: AppUser? = null
)

class HomeViewModel : ViewModel()
