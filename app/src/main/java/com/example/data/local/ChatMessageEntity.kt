package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val role: String, // "USER" or "MODEL"
    val text: String,
    val imageUri: String? = null,
    val imageBase64: String? = null,
    val generatedImageUrl: String? = null,
    val videoUrl: String? = null,
    val videoAspectRatio: String? = null, // "16:9" or "9:16"
    val citationsJson: String? = null,
    val searchQueriesJson: String? = null,
    val mapsDataJson: String? = null,
    val audioUri: String? = null,
    val isTranscribedAudio: Boolean = false,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val feedback: Int = 0 // 0 = none, 1 = up, -1 = down
)
