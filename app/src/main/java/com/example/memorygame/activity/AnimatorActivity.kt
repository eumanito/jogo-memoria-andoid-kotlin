package com.example.memorygame.activity

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.memorygame.R
import com.example.memorygame.R.*
import com.example.memorygame.services.CharacterService
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch


class AnimatorActivity : AppCompatActivity() {

    private lateinit var timeTextView: TextView
    private lateinit var stopwatch: Stopwatch

    private lateinit var frontAnimation: AnimatorSet
    private lateinit var backAnimation: AnimatorSet
    private var isFront = true

    private lateinit var gridLayout: GridLayout

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.oneflipcard)

        // Initialize views
        timeTextView = findViewById(id.timeView)
        stopwatch = Stopwatch(timeTextView)
        stopwatch.start()

        val pointTextView = findViewById<TextView>(id.pointTextView)
        var points = 0

        // Initialize the GridLayout
        gridLayout = findViewById(id.gridLayout)


        lifecycleScope.launch {
            createCardView("Front Card 1", pointTextView, points)
            createCardView("Front Card 2", pointTextView, points)
            createCardView("Front Card 3", pointTextView, points)
            createCardView("Front Card 4", pointTextView, points)
        }
    }

    private suspend fun createCardView(cardTitle: String, pointTextView: TextView, points: Int) {
        // Create a new instance of ViewFlipper
        val viewFlipper = ViewFlipper(this)

        // Set the width and height to be the same to create a square card
        val cardSize = resources.getDimensionPixelSize(R.dimen.card_size)
        val layoutParams = GridLayout.LayoutParams()
        layoutParams.width = cardSize
        layoutParams.height = cardSize
        layoutParams.also { viewFlipper.layoutParams = it }

        // Set up front card
        val frontCard = CardView(this)
        frontCard.radius = resources.getDimension(R.dimen.card_corner_radius)
        frontCard.useCompatPadding = true
        frontCard.setCardBackgroundColor(Color.parseColor("#263238")) // front card color


        val frontImageView = ImageView(this)
        val characterService = CharacterService();
        val frontCharacter = characterService.getRandomCharacter()

        // Load the character's image into the ImageView using Picasso (or the desired library)
        Picasso.get().load(frontCharacter.image).into(frontImageView)

        frontCard.addView(frontImageView)

        val frontTextView = TextView(this)
        frontTextView.text = cardTitle
        frontTextView.textSize = resources.getDimension(R.dimen.card_text_size)
        frontTextView.setTextColor(Color.WHITE)
        frontCard.addView(frontTextView)

        // Set up back card
        val backCard = CardView(this)
        backCard.radius = resources.getDimension(R.dimen.card_corner_radius)
        backCard.useCompatPadding = true
        backCard.setCardBackgroundColor(Color.parseColor("#A7CB54")) // back card color
        backCard.visibility = View.INVISIBLE // Initially hide the back card

        val backTextView = TextView(this)
        backTextView.text = "Back of $cardTitle"
        backTextView.textSize = resources.getDimension(R.dimen.card_text_size)
        backTextView.setTextColor(Color.WHITE)
        backCard.addView(backTextView)

        // Add both cards to the ViewFlipper
        viewFlipper.addView(frontCard)
        viewFlipper.addView(backCard)

        // Add the ViewFlipper to the GridLayout
        gridLayout.addView(viewFlipper)

        // Set up flip animation for the ViewFlipper
        val flipAnimationIn = AnimatorInflater.loadAnimator(
            applicationContext,
            R.animator.frontanimator
        ) as AnimatorSet
        val flipAnimationOut = AnimatorInflater.loadAnimator(
            applicationContext,
            R.animator.backanimator
        ) as AnimatorSet

        // Começa  o click listener para os flipping cards
        viewFlipper.setOnClickListener {
            if (viewFlipper.displayedChild == 0) {
                // Se o cartão da frente for exibido, vire para o cartão de trás
                frontCard.animate()
                    .rotationY(180f)
                    .setDuration(1000)
                    .withEndAction {
                        frontCard.visibility = View.INVISIBLE
                        backCard.visibility = View.VISIBLE
                        backCard.rotationY = 0f
                        viewFlipper.displayedChild = 1
                    }
            } else {
                // Se o cartão de trás for exibido, vire para o cartão da frente
                backCard.animate()
                    .rotationY(180f)
                    .setDuration(1000)
                    .withEndAction {
                        backCard.visibility = View.INVISIBLE
                        frontCard.visibility = View.VISIBLE
                        frontCard.rotationY = 0f
                        viewFlipper.displayedChild = 0
                    }
            }

            // Atualiza a pontuação
            if (viewFlipper.displayedChild == 1) {
                val newPoints = points + 1
                pointTextView.text = "Point: $newPoints"
            }

            // Pare o cronômetro se o cartão traseiro for clicado
            if (viewFlipper.displayedChild == 1) {
                stopwatch.stop()
            }
        }


    }
}


