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

package com.example.appperms.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.appperms.data.local.database.Permission
import com.example.appperms.data.local.database.PermissionDao
import javax.inject.Inject

interface PermissionRepository {
    val permissions: Flow<List<String>>

    suspend fun add(name: String)
}

class DefaultPermissionRepository @Inject constructor(
    private val permissionDao: PermissionDao
) : PermissionRepository {

    override val permissions: Flow<List<String>> =
        permissionDao.getPermissions().map { items -> items.map { it.name } }

    override suspend fun add(name: String) {
        permissionDao.insertPermission(Permission(name = name))
    }
}
