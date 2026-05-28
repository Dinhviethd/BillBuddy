package com.example.billbuddy.data.repo

import com.example.billbuddy.data.model.Group
import com.example.billbuddy.utils.Resource
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun createGroup(group: Group): Flow<Resource<Unit>>
    fun joinGroupByName(groupName: String): Flow<Resource<Unit>>
    fun observeUserGroups(): Flow<Resource<List<Group>>>
    fun getGroupById(groupId: String): Flow<Resource<Group>>
    fun leaveGroup(groupId: String): Flow<Resource<Unit>>
}
