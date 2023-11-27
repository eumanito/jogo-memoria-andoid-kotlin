package com.example.memorygame.activity

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
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
import com.example.memorygame.`object`.Record
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class AnimatorActivity : AppCompatActivity() {
    private lateinit var timeTextView: TextView
    private lateinit var stopwatch: Stopwatch
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var gridLayout: GridLayout
    private var lastRecord = ""

    private val cardPairs = mutableListOf<String>()
    private var lastFlippedView: ViewFlipper? = null
    private var lastFront: CardView? = null
    private var lastBack: CardView? = null
    private var matchedPairs = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.oneflipcard)

        //https://comicvine.gamespot.com/rick-and-morty/4050-81059/characters/
        cardPairs.add("https://i.imgur.com/fITuWTt.png")
        cardPairs.add("https://i.imgur.com/fITuWTt.png")
        cardPairs.add("https://rickandmortyapi.com/api/character/avatar/2.jpeg")
        cardPairs.add("https://rickandmortyapi.com/api/character/avatar/2.jpeg")
        cardPairs.add("https://comicvine.gamespot.com/a/uploads/scale_small/11/110802/7976283-brad.jpg")
        cardPairs.add("https://comicvine.gamespot.com/a/uploads/scale_small/11/110802/7976283-brad.jpg")
        //cardPairs.add("https://comicvine.gamespot.com/a/uploads/scale_small/11/110802/7975577-squanchy.jpg")
        //cardPairs.add("https://comicvine.gamespot.com/a/uploads/scale_small/11/110802/7975577-squanchy.jpg")

        // Embaralhar os cards
        cardPairs.shuffle()

        database = FirebaseFirestore.getInstance()

        // Initialize views
        timeTextView = findViewById(id.timeView)
        stopwatch = Stopwatch(timeTextView)
        stopwatch.start()

        // Initialize the GridLayout
        gridLayout = findViewById(id.gridLayout)

        // Create card views
        for (i in 0 until cardPairs.size) {
            createCardView("Card ${i + 1}", cardPairs[i])
        }

        getRecord()
    }

    @SuppressLint("SetTextI18n")
    private fun createCardView(cardTitle: String, imageUrl: String) {
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
        frontTextView.textSize = 14f
        frontTextView.gravity = Gravity.CENTER
        frontTextView.setTextColor(Color.WHITE)
        frontCard.addView(frontTextView)

        // Set up back card
        val backCard = CardView(this)
        backCard.radius = resources.getDimension(dimen.card_corner_radius)
        backCard.useCompatPadding = true
        backCard.setCardBackgroundColor(Color.parseColor("#A7CB54")) // back card color
        backCard.visibility = View.INVISIBLE // Initially hide the back card

        val imageView = ImageView(this);
        Picasso.with(this).load(imageUrl).into(imageView)

        backCard.addView(imageView)
        viewFlipper.addView(frontCard)
        viewFlipper.addView(backCard)

        gridLayout.addView(viewFlipper)

        AnimatorInflater.loadAnimator(
            applicationContext,
            animator.frontanimator
        ) as AnimatorSet
        AnimatorInflater.loadAnimator(
            applicationContext,
            animator.backanimator
        ) as AnimatorSet

        viewFlipper.setOnClickListener {
            if (viewFlipper.displayedChild == 0) {
                // Se o card da frente for exibido, vire para o card de trás
                frontCard.animate()
                    .rotationY(180f)
                    .setDuration(1000)
                    .withEndAction {
                        frontCard.visibility = View.INVISIBLE
                        backCard.visibility = View.VISIBLE
                        backCard.rotationY = 0f
                        viewFlipper.displayedChild = 1
                    }

                if (lastFlippedView == null) {
                    // Se este é o primeiro card virado
                    lastFlippedView = viewFlipper
                    lastFront = frontCard
                    lastBack = backCard
                }
                else {

                    // Se este é a segundo card virado
                    if (imageUrl == cardPairs[gridLayout.indexOfChild(viewFlipper) / 2]) {

                        // Correspondência encontrada

                        matchedPairs++

                        if (matchedPairs == cardPairs.size / 2) {
                            // Todas as correspondências foram encontradas - jogo completo
                            stopwatch.stop()
                            saveRecord()
                            Toast.makeText(this, "Você venceu!", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        viewFlipper.postDelayed({
                            // If the back card is displayed, flip to the front card
                            backCard.animate()
                                .rotationY(180f)
                                .setDuration(1000)
                                .withEndAction {
                                    backCard.visibility = View.INVISIBLE
                                    frontCard.visibility = View.VISIBLE
                                    frontCard.rotationY = 0f
                                    viewFlipper.displayedChild = 0
                                }

                            lastBack?.animate()
                                ?.rotationY(180f)
                                ?.setDuration(1000)
                                ?.withEndAction {
                                    lastBack!!.visibility = View.INVISIBLE
                                    lastFront!!.visibility = View.VISIBLE
                                    lastFront!!.rotationY = 0f
                                    lastFlippedView?.displayedChild = 0
                                    lastFlippedView = null
                                }
                        }, 1000) // Delay to allow the user to see the cards
                    }

                    /*lastFlippedView = null
                    lastFront = null
                    lastBack = null*/
                }
            }
        }
    }

    private fun getRecord() {
        val recordCollection = database.collection("Record")
        auth = Firebase.auth
        val user = auth.currentUser

        if (user != null) {
            recordCollection.document(user.email!!).get().addOnSuccessListener { documento ->
                if (documento != null && documento.exists()) {
                    val recordObject = documento.toObject(Record::class.java)
                    this.lastRecord = recordObject?.tempo.toString()
                    val lastRecordTextView = findViewById<TextView>(R.id.lastTimeTextView)
                    val text = "Ultimo tempo: $lastRecord"
                    lastRecordTextView.text = text
                } else {
                    Log.e("record","Erro ao ler o documento, ele não existe ou está vazio")
                }
            }.addOnFailureListener { error ->
                Log.e("record","Erro ao ler dados do servidor: ${error.message.toString()}")
            }
        }
    }
    private fun saveRecord() {
        auth = Firebase.auth
        val user = auth.currentUser

        if (user != null) {
            timeTextView = findViewById(id.lastTimeTextView)
            val recordObject = Record(user.email!!, timeTextView.text.toString())
            val recordCollection = database.collection("Record")
            recordCollection.document(user.email!!).set(recordObject).addOnSuccessListener {
                Log.i("record", "Record gravado com sucesso")
            }.addOnFailureListener { error ->
                Log.e("record", "Erro ao gravar Record")
            }
        }
    }
}

