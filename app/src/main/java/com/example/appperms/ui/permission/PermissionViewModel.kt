/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.appperms.ui.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.appperms.data.PermissionRepository
import com.example.appperms.ui.permission.PermissionUiState.Error
import com.example.appperms.ui.permission.PermissionUiState.Loading
import com.example.appperms.ui.permission.PermissionUiState.Success
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val permissionRepository: PermissionRepository
) : ViewModel() {

    val uiState: StateFlow<PermissionUiState> = permissionRepository
        .permissions.map<List<String>, PermissionUiState>(::Success)
        .catch { emit(Error(it)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Loading)

    fun addPermission(name: String) {
        viewModelScope.launch {
            permissionRepository.add(name)
        }
    }
}

sealed interface PermissionUiState {
    object Loading : PermissionUiState
    data class Error(val throwable: Throwable) : PermissionUiState
    data class Success(val data: List<String>) : PermissionUiState
}
