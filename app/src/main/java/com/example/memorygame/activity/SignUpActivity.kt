package com.example.memorygame.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.memorygame.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var email: EditText
    private lateinit var password: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = Firebase.auth
        email = findViewById(R.id.editTextEmail)
        password = findViewById(R.id.editTextPassWord)
    }

    fun registerUser(v: View?) {
        auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(
                            baseContext,
                            "Cadastrado! Redirecionando...",
                            Toast.LENGTH_SHORT
                    ).show()

                    Thread.sleep(2000)

                    Intent(this, LoginActivity::class.java).also {
                        startActivity(it)
                    }
                } else {
                    Toast.makeText(
                            baseContext,
                            "Cadastro falhou.",
                            Toast.LENGTH_SHORT
                    ).show()
                    Log.w("CADASTRO", "createUserWithEmail:failure", task.exception)
                }
            }
    }
}