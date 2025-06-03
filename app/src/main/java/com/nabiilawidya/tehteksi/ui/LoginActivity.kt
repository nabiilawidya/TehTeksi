package com.nabiilawidya.tehteksi.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nabiilawidya.tehteksi.databinding.ActivityLoginBinding
import com.nabiilawidya.tehteksi.databinding.LayoutLoginBinding
import com.nabiilawidya.tehteksi.databinding.LayoutRegisterBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginBinding: LayoutLoginBinding
    private lateinit var registerBinding: LayoutRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginBinding = LayoutLoginBinding.bind(binding.layoutLogin.root)
        registerBinding = LayoutRegisterBinding.bind(binding.layoutRegister.root)
        auth = FirebaseAuth.getInstance()

        loginBinding.cirLoginButton.setOnClickListener {
            val email = loginBinding.editTextEmail.text.toString().trim()
            val password = loginBinding.editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password harus diisi!", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, password)
            }
        }

        loginBinding.forgotPassword.setOnClickListener {
            val email = loginBinding.editTextEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Masukkan email untuk reset password", Toast.LENGTH_SHORT).show()
            } else {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Cek email Anda untuk reset password", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Gagal mengirim email: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

        registerBinding.btnRegister.setOnClickListener {
            val name = registerBinding.editTextName.text.toString().trim()
            val phone = registerBinding.editTextTelp.text.toString().trim()
            val email = registerBinding.editTextEmail.text.toString().trim()
            val password = registerBinding.editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Nama, Nomor Telepon, Email, dan password harus diisi!", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(email, password)
            }
        }

        loginBinding.signupText.setOnClickListener {
            toggleLoginRegister(showLogin = false)
        }


        registerBinding.signintext.setOnClickListener {
            toggleLoginRegister(showLogin = true)
        }
    }

    private fun toggleLoginRegister(showLogin: Boolean) {
        binding.layoutLogin.root.visibility = if (showLogin) View.VISIBLE else View.GONE
        binding.layoutRegister.root.visibility = if (showLogin) View.GONE else View.VISIBLE
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Login gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    val name = registerBinding.editTextName.text.toString().trim()
                    val phone = registerBinding.editTextTelp.text.toString().trim()

                    val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                        Date()
                    )

                    val userData = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "phone" to phone,
                        "createdAt" to currentTime
                    )

                    if (userId != null) {
                        firestore.collection("users").document(userId).set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registrasi berhasil dan data disimpan!", Toast.LENGTH_SHORT).show()
                                toggleLoginRegister(showLogin = true)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Gagal simpan data: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }

                } else {
                    Toast.makeText(this, "Registrasi gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

}