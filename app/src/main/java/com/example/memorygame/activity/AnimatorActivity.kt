package com.example.memorygame.activity

import android.animation.AnimatorInflater
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
import android.widget.Toast
import com.example.memorygame.R
import com.example.memorygame.R.*
import com.squareup.picasso.Picasso
import com.example.memorygame.`object`.Placar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

@Suppress("NAME_SHADOWING")
class AnimatorActivity : AppCompatActivity() {
    private lateinit var timeTextView: TextView
    private lateinit var stopwatch: Stopwatch
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var placar: CollectionReference
    private lateinit var gridLayout: GridLayout
    private var points = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.oneflipcard)

        auth = Firebase.auth
        val user: FirebaseUser? = auth.currentUser

        database = FirebaseFirestore.getInstance()

        // Initialize views
        timeTextView = findViewById(id.timeView)
        stopwatch = Stopwatch(timeTextView)
        stopwatch.start()

        // Initialize the GridLayout
        gridLayout = findViewById(id.gridLayout)

        // Create card views
        createCardView("Front Card 1")
        createCardView("Front Card 2")
        createCardView("Front Card 3")
        createCardView("Front Card 4")

        obterPlacar()
    }

    @SuppressLint("SetTextI18n")
    private fun createCardView(cardTitle: String) {
        // Create a new instance of ViewFlipper
        val viewFlipper = ViewFlipper(this)

        // Set the width and height to be the same to create a square card
        val cardSize = resources.getDimensionPixelSize(dimen.card_size)
        val layoutParams = GridLayout.LayoutParams()
        layoutParams.width = cardSize
        layoutParams.height = cardSize
        layoutParams.also { viewFlipper.layoutParams = it }

        // Set up front card
        val frontCard = CardView(this)
        frontCard.radius = resources.getDimension(dimen.card_corner_radius)
        frontCard.useCompatPadding = true
        frontCard.setCardBackgroundColor(Color.parseColor("#263238")) // front card color

        val frontTextView = TextView(this)
        frontTextView.text = cardTitle
        frontTextView.textSize = resources.getDimension(dimen.card_text_size)
        frontTextView.setTextColor(Color.WHITE)
        frontCard.addView(frontTextView)

        // Set up back card
        val backCard = CardView(this)
        backCard.radius = resources.getDimension(dimen.card_corner_radius)
        backCard.useCompatPadding = true
        backCard.setCardBackgroundColor(Color.parseColor("#A7CB54")) // back card color
        backCard.visibility = View.INVISIBLE // Initially hide the back card
        /* Como era antigamente
        val backTextView = TextView(this)
        backTextView.text = "Back of $cardTitle"
        backTextView.textSize = resources.getDimension(dimen.card_text_size)
        backTextView.setTextColor(Color.WHITE)
        backCard.addView(backTextView) */
        val imageView = ImageView(this);
        val url = "https://i.imgur.com/fITuWTt.png"
        Picasso.with(this).load(url).into(imageView)

        backCard.addView(imageView)
        // Add both cards to the ViewFlipper
        viewFlipper.addView(frontCard)
        viewFlipper.addView(backCard)

        // Add the ViewFlipper to the GridLayout
        gridLayout.addView(viewFlipper)

        // Set up flip animation for the ViewFlipper
        AnimatorInflater.loadAnimator(
            applicationContext,
            animator.frontanimator
        ) as AnimatorSet
        AnimatorInflater.loadAnimator(
            applicationContext,
            animator.backanimator
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
                val pointTextView = findViewById<TextView>(id.pointTextView)
                val pts = this.points + 1
                pointTextView.text = "Point: $pts"

                gravarPlacar()
            }

            // Pare o cronômetro se o cartão traseiro for clicado
            if (viewFlipper.displayedChild == 1) {
                stopwatch.stop()
            }
        }
    }

    private fun obterPlacar() {
        placar = database.collection("placar")
        auth = Firebase.auth
        val user = auth.currentUser

        if (user != null) {
            placar.document(user.email!!).get().addOnSuccessListener { documento ->
                if (documento != null && documento.exists()) {
                    val placar = documento.toObject(Placar::class.java)
                    this.points = placar?.pontuacao!!

                    val pointTextView = findViewById<TextView>(R.id.pointTextView)
                    val pts = this.points
                    this.points = pts
                    val pointText = "Point: $pts"
                    pointTextView.text = pointText

                } else {
                    Toast.makeText(
                        baseContext,
                        "Erro ao ler o documento, ele não existe ou está vazio",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener { error ->
                Toast.makeText(
                    baseContext,
                    "Erro ao ler Dados do Servidor: ${error.message.toString()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun gravarPlacar() {
        auth = Firebase.auth
        val user = auth.currentUser

        if (user != null) {
            val pontuacao = Placar(user.email!!, this.points)
            placar = database.collection("placar")
            placar.document(user.email!!).set(pontuacao).addOnSuccessListener {
                Toast.makeText(baseContext, "Sucesso ao gravar dados", Toast.LENGTH_SHORT)
                    .show()
            }.addOnFailureListener { error ->
                Toast.makeText(
                    baseContext,
                    "Erro ao gravar dados: ${error.message.toString()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

