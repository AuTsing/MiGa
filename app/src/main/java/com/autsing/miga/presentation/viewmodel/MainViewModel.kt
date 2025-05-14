package com.autsing.miga.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autsing.miga.presentation.util.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MainUiState(
    val loading: Boolean = true,
    val auth: String? = null,
)

class MainViewModel : ViewModel() {

    var uiState: MainUiState by mutableStateOf(MainUiState())
        private set

    init {
        loadAuth()
    }

    private fun loadAuth() = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            val auth = FileUtil.instance.readJson("auth.json").getOrThrow()

            withContext(Dispatchers.Main) {
                uiState = uiState.copy(auth = auth)
            }
        }.also {
            withContext(Dispatchers.Main) {
                uiState = uiState.copy(loading = false)
            }
        }
    }
}
