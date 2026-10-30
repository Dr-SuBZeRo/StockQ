package com.stockq.data.repository

import com.stockq.core.database.StockQDatabase
import com.stockq.core.database.entity.BusinessProfile
import kotlinx.coroutines.flow.Flow

class BusinessProfileRepository(private val database: StockQDatabase) {

    private val profileDao = database.businessProfileDao()

    fun getProfile(): Flow<BusinessProfile?> = profileDao.getProfileFlow()

    suspend fun getProfileDirect(): BusinessProfile? = profileDao.getProfile()

    suspend fun updateProfile(profile: BusinessProfile) {
        profileDao.insertOrUpdate(profile.copy(id = 1L, updatedAt = System.currentTimeMillis()))
    }
}

