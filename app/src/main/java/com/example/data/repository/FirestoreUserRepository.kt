package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreUserRepository(private val context: Context) {

    // Mandatory: Initialize Firestore with custom DB ID from firebase_applet_config.xml
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(
        context.getString(R.string.firestore_database_id)
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("يجب تسجيل الدخول بحساب Google أولاً للوصول إلى السحابة.")
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> =
        db.collection("users").document(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(UserProfile::class.java)
                } else {
                    null
                }
            }

    suspend fun saveUserProfile(profile: UserProfile) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)

        val data = mapOf(
            "userId" to uid,
            "displayName" to profile.displayName,
            "email" to profile.email,
            "photoUrl" to profile.photoUrl,
            "khatmahGoal" to profile.khatmahGoal,
            "dailyReadingGoalMinutes" to profile.dailyReadingGoalMinutes,
            "currentStreakDays" to profile.currentStreakDays,
            "totalMinutesRead" to profile.totalMinutesRead,
            "totalPagesRead" to profile.totalPagesRead,
            "lastReadMushafId" to profile.lastReadMushafId,
            "lastReadMushafTitle" to profile.lastReadMushafTitle,
            "lastReadPage" to profile.lastReadPage,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("FirestoreUserRepo", "فشل حفظ الملف الشخصي", e)
        }
    }

    suspend fun syncReadingProgress(
        userId: String,
        page: Int,
        mushafTitle: String,
        mushafId: String,
        minutesSpent: Int = 0
    ) {
        if (auth.currentUser?.uid != userId) return
        val docRef = db.collection("users").document(userId)

        try {
            val updates = mutableMapOf<String, Any>(
                "lastReadPage" to page.toLong(),
                "lastReadMushafId" to mushafId,
                "lastReadMushafTitle" to mushafTitle,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            if (minutesSpent > 0) {
                updates["totalMinutesRead"] = FieldValue.increment(minutesSpent.toLong())
            }
            updates["totalPagesRead"] = FieldValue.increment(1L)

            docRef.set(updates, SetOptions.merge()).await()

            // Also record a log in reading_history subcollection
            val historyRef = docRef.collection("reading_history").document()
            val logData = mapOf(
                "id" to historyRef.id,
                "userId" to userId,
                "mushafId" to mushafId,
                "pageNumber" to page.toLong(),
                "durationMinutes" to minutesSpent.toLong(),
                "timestamp" to FieldValue.serverTimestamp()
            )
            historyRef.set(logData).await()
        } catch (e: Exception) {
            Log.e("FirestoreUserRepo", "فشل مزامنة تقدم القراءة في السحابة", e)
        }
    }

    suspend fun updateKhatmahGoal(userId: String, newGoal: String) {
        if (auth.currentUser?.uid != userId) return
        val docRef = db.collection("users").document(userId)
        try {
            docRef.update(
                mapOf(
                    "khatmahGoal" to newGoal,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            Log.e("FirestoreUserRepo", "فشل تحديث هدف الختمة", e)
        }
    }

    suspend fun updateDailyReadingGoal(userId: String, minutes: Long) {
        if (auth.currentUser?.uid != userId) return
        val docRef = db.collection("users").document(userId)
        try {
            docRef.update(
                mapOf(
                    "dailyReadingGoalMinutes" to minutes,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            Log.e("FirestoreUserRepo", "فشل تحديث وقت القراءة اليومي", e)
        }
    }
}
