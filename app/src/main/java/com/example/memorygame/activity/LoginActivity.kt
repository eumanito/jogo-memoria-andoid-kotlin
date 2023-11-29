package com.example.memorygame.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.memorygame.R
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var email: EditText
    private lateinit var password: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth
        email = findViewById(R.id.editTextEmailLogin)
        password = findViewById(R.id.editTextPassWordLogin)
    }

    fun signUp(view: View?) {
        Intent(this, SignUpActivity::class.java).also {
            startActivity(it)
        }
    }
    fun loginAnimator(view: View?) {
        Intent(this, AnimatorActivity::class.java).also {
            startActivity(it)
        }
    }

    fun signIn(v: View?) {
        if (!email.text.toString().isEmpty() || !password.text.toString().isEmpty()) {
            auth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("LOGIN", "Login ok")
                        this.loginAnimator(v)
                    } else {
                        Log.w("LOGIN", "Erro no Login", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authenticação falhou.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            Toast.makeText(
                baseContext,
                "Informe o e-mail e a senha",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun logOut() {
        Firebase.auth.signOut()
        Intent(this, LoginActivity::class.java).also {
            startActivity(it)
        }
    }
}