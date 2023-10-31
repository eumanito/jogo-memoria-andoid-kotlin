package com.example.memorygame.activity

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.memorygame.R

class AnimatorActivity : AppCompatActivity() {

    lateinit var frontAnimation: AnimatorSet
    lateinit var backAnimation: AnimatorSet
    var isFront = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.oneflipcard)

        var scale = applicationContext.resources.displayMetrics.density

        val cardFront = findViewById<TextView>(R.id.card_front)
        val cardBack = findViewById<TextView>(R.id.card_back)
        val flip = findViewById<Button>(R.id.flip_btn) as Button

        cardFront.cameraDistance = 8000 * scale
        cardBack.cameraDistance = 8000 * scale

        frontAnimation = AnimatorInflater.loadAnimator(applicationContext, R.animator.frontanimator) as AnimatorSet
        backAnimation = AnimatorInflater.loadAnimator(applicationContext, R.animator.backanimator) as AnimatorSet

        flip.setOnClickListener{
            if(isFront)
            {
                frontAnimation.setTarget(cardFront);
                backAnimation.setTarget(cardBack);
                frontAnimation.start()
                backAnimation.start()
                isFront = false

            }
            else
            {
                frontAnimation.setTarget(cardBack)
                backAnimation.setTarget(cardFront)
                backAnimation.start()
                frontAnimation.start()
                isFront =true

            }
        }

        val flipCard = View.OnClickListener {
            if (isFront) {
                // Perform the flip animation from front to back
                frontAnimation.setTarget(cardFront)
                backAnimation.setTarget(cardBack)
                frontAnimation.start()
                backAnimation.start()
                isFront = false
            } else {
                // Perform the flip animation from back to front
                frontAnimation.setTarget(cardBack)
                backAnimation.setTarget(cardFront)
                backAnimation.start()
                frontAnimation.start()
                isFront = true
            }
        }

        cardFront.setOnClickListener(flipCard)
        cardBack.setOnClickListener(flipCard)
    }
}

