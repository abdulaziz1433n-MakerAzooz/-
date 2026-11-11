package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    // userId is stored as a regular Firestore field in users/{uid}; do not mark it @DocumentId.
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val khatmahGoal: String = "ختمة كل شهر (جزء يومياً)",
    val dailyReadingGoalMinutes: Long = 30L,
    val currentStreakDays: Long = 1L,
    val totalMinutesRead: Long = 0L,
    val totalPagesRead: Long = 0L,
    val lastReadMushafId: String = "",
    val lastReadMushafTitle: String = "",
    val lastReadPage: Long = 1L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CloudBookmark(
    @DocumentId val id: String = "",
    val userId: String = "",
    val mushafId: String = "",
    val mushafTitle: String = "",
    val pageNumber: Long = 1L,
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
