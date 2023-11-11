package com.example.memorygame.activity

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.example.memorygame.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AnimatorActivity : AppCompatActivity() {
    private lateinit var textViewActiveUser: TextView
    private lateinit var timeTextView: TextView
    private lateinit var stopwatch: Stopwatch
    private var isFront = true
    private var points = 0;

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.oneflipcard)

        textViewActiveUser = findViewById(R.id.textViewActiveUser)
        textViewActiveUser.text = ""

        val user = Firebase.auth.currentUser
        if (user != null) {
            textViewActiveUser.text = user.email
        }

        findViewById<TextView>(R.id.timeView)
        timeTextView = findViewById(R.id.timeView)
        stopwatch = Stopwatch(timeTextView)
        stopwatch.start()

        val card1 = setupCard(1, 1)
    }

    fun setupCard(cardFrontIndex: Int, cardBackIndex: Int) {
        val scale = applicationContext.resources.displayMetrics.density
        val pointTextView = findViewById<TextView>(R.id.pointTextView)

        val cardFrontId = "card_front" + cardFrontIndex
        val cardFront = findViewById<TextView>(resources.getIdentifier(cardFrontId, "id", packageName))
        cardFront.cameraDistance = 8000 * scale
        val frontAnimation = AnimatorInflater.loadAnimator(applicationContext, R.animator.frontanimator) as AnimatorSet

        val cardBackId = "card_back" + cardFrontIndex
        val cardBack = findViewById<TextView>(resources.getIdentifier(cardBackId, "id", packageName))
        cardBack.cameraDistance = 8000 * scale
        val backAnimation = AnimatorInflater.loadAnimator(applicationContext, R.animator.backanimator) as AnimatorSet

        val flipCard = View.OnClickListener {
            if (isFront) {
                // Perform the flip animation from front to back
                frontAnimation.setTarget(cardFront)
                backAnimation.setTarget(cardBack)

                frontAnimation.start()
                backAnimation.start()
            } else {

                val pts = this.points + 1
                this.points = pts
                val pointText = "Point: $pts"
                pointTextView.text = pointText

                // Perform the flip animation from back to front
                frontAnimation.setTarget(cardBack)
                backAnimation.setTarget(cardFront)

                backAnimation.start()
                frontAnimation.start()
            }
            isFront = !isFront
        }

        cardFront.setOnClickListener(flipCard)
        cardBack.setOnClickListener(flipCard)
    }
}

