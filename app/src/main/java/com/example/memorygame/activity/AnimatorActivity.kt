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
import org.w3c.dom.Text

class AnimatorActivity : AppCompatActivity() {
    private lateinit var textViewActiveUser: TextView
    private lateinit var textViewLogOut: TextView

    private lateinit var timeTextView: TextView
    private lateinit var stopwatch: Stopwatch

    private lateinit var frontAnimation: AnimatorSet
    private lateinit var backAnimation: AnimatorSet
    private var isFront = true

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

        val scale = applicationContext.resources.displayMetrics.density

        val cardFront = findViewById<TextView>(R.id.card_front)
        cardFront.cameraDistance = 8000 * scale
        frontAnimation = AnimatorInflater.loadAnimator(applicationContext, R.animator.frontanimator) as AnimatorSet

        val cardBack = findViewById<TextView>(R.id.card_back)
        cardBack.cameraDistance = 8000 * scale
        backAnimation = AnimatorInflater.loadAnimator(applicationContext, R.animator.backanimator) as AnimatorSet

        findViewById<TextView>(R.id.timeView)
        timeTextView = findViewById(R.id.timeView)
        stopwatch = Stopwatch(timeTextView)
        stopwatch.start()

        val pointTextView = findViewById<TextView>(R.id.pointTextView)
        var points = 0

        val flipCard = View.OnClickListener {
            isFront = if (isFront) {

                // Perform the flip animation from front to back
                frontAnimation.setTarget(cardFront)
                backAnimation.setTarget(cardBack)

                frontAnimation.start()
                backAnimation.start()

                false
            } else {
                stopwatch.stop()

                points++
                val pointText = "Point: $points"
                pointTextView.text = pointText

                // Perform the flip animation from back to front
                frontAnimation.setTarget(cardBack)
                backAnimation.setTarget(cardFront)

                backAnimation.start()
                frontAnimation.start()

                true
            }
        }

        cardFront.setOnClickListener(flipCard)
        cardBack.setOnClickListener(flipCard)
    }
}

