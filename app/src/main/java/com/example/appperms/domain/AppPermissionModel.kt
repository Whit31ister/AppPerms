package com.example.appperms.domain

enum class RiskTier {
    CRITICAL,
    DANGEROUS,
    NORMAL
}

data class AppPermissionModel(
    val name: String,
    val tier: RiskTier,
    val isGranted: Boolean
)
