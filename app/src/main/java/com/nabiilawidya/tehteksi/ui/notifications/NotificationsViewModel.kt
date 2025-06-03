package com.nabiilawidya.tehteksi.ui.notifications

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nabiilawidya.tehteksi.data.User

class NotificationsViewModel : ViewModel() {


    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                doc.toObject(User::class.java)?.let {
                    _user.value = it
                } ?: Log.e("ProfileViewModel", "User data is null")

            }
            .addOnFailureListener { e ->
                Log.e("ProfileViewModel", "Error fetching user: ${e.message}")
            }
    }

    fun logout() {
        auth.signOut()
    }
}