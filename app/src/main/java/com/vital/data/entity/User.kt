package com.vital.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Entity(tableName = "users")
@Serializable
data class User(
    @PrimaryKey
    val id: String, // UUID
    val name: String,
    val email: String,
    val themeColor: String = "fudPink", // Theme color key
    val trueBlack: Boolean = true,
    val pinHash: String? = null, // Argon2 hash
    val biometricEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false
) {
    companion object {
        fun create(name: String, email: String): User {
            return User(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                email = email
            )
        }
    }
}