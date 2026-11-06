package com.example.data.auth

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Keeps Firebase authentication state and supports signing out.
 * Google sign-in has been removed; the app's email-based account flow remains separate.
 */
class AuthManager(context: Context) {

    private val auth: FirebaseAuth = Firebase.auth

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    fun signOut(scope: CoroutineScope, onComplete: () -> Unit) {
        auth.signOut()
        _currentUser.value = null
        scope.launch {
            onComplete()
        }
    }
}
