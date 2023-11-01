package com.example.memorygame.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.memorygame.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun signUp(view: View?) {
        Intent(this, SignUpActivity::class.java).also {
            startActivity(it)
        }
    }
}