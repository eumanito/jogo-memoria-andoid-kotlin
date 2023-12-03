package com.example.memorygame.activity

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.memorygame.R
import com.example.memorygame.R.*
import com.example.memorygame.`object`.Record
import com.example.memorygame.services.CharacterService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.coroutines.runBlocking

class AnimatorActivity : AppCompatActivity() {
    private lateinit var timeTextView: TextView
    private lateinit var stopwatch: Stopwatch
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var gridLayout: GridLayout
    private var lastRecord = ""
    private var lastImage: String? = null

    private var bundle: Bundle? = null

    private val cardPairs = mutableListOf<String>()
    private var lastFlippedView: ViewFlipper? = null
    private var lastFront: CardView? = null
    private var lastBack: CardView? = null
    private var matchedPairs = 0

    private var image1: String = "https://i.imgur.com/fITuWTt.png"
    private var image2: String = "https://rickandmortyapi.com/api/character/avatar/2.jpeg"
    private var image3: String = "https://comicvine.gamespot.com/a/uploads/scale_small/11/110802/7976283-brad.jpg"

    private val characterService = CharacterService()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        bundle = savedInstanceState
        super.onCreate(savedInstanceState)
        setContentView(layout.oneflipcard)
        image1 = getImage()
        image2 = getImage()
        image3 = getImage()

        cardPairs.add(image1)
        cardPairs.add(image1)
        cardPairs.add(image2)
        cardPairs.add(image2)
        cardPairs.add(image3)
        cardPairs.add(image3)

        // Embaralhar os cards
        cardPairs.shuffle()

        database = FirebaseFirestore.getInstance()

        timeTextView = findViewById(id.timeView)
        stopwatch = Stopwatch(timeTextView)
        stopwatch.start()

        gridLayout = findViewById(id.gridLayout)

        // Cria os cards
        for (i in 0 until cardPairs.size) {
            createCardView("Card ${i + 1}", cardPairs[i])
        }

        auth = Firebase.auth
        val user = auth.currentUser
        val textViewActiveUser = findViewById<TextView>(R.id.textViewActiveUser)
        textViewActiveUser.text = user!!.email

        getRecord()
    }

    @SuppressLint("SetTextI18n")
    private fun createCardView(cardTitle: String, imageUrl: String) {
        val viewFlipper = ViewFlipper(this)

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

        // Configura frente do card
        val frontTextView = TextView(this)
        frontTextView.text = cardTitle
        frontTextView.textSize = 14f
        frontTextView.gravity = Gravity.CENTER
        frontTextView.setTextColor(Color.WHITE)
        frontCard.addView(frontTextView)

        // Configura atrás do card
        val backCard = CardView(this)
        backCard.radius = resources.getDimension(dimen.card_corner_radius)
        backCard.useCompatPadding = true
        backCard.setCardBackgroundColor(Color.parseColor("#A7CB54"))
        backCard.visibility = View.INVISIBLE

        // Configura imagem atrás do card
        val imageView = ImageView(this);
        Picasso.with(this).load(imageUrl).into(imageView)
        backCard.addView(imageView)

        // Adiciona os dois lados no viewFlipper
        viewFlipper.addView(frontCard)
        viewFlipper.addView(backCard)

        // Adiciona card montado ao grid
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

            if (viewFlipper.displayedChild == 0) { // card não revelado

                showCard(viewFlipper)

                if (this.lastFlippedView == null) { // se for 1º da jogada
                    this.lastImage = this.cardPairs[this.gridLayout.indexOfChild(viewFlipper)]
                    this.lastFlippedView = viewFlipper
                    this.lastFront = frontCard
                    this.lastBack = backCard

                } else { // se for 2º da jogada

                    if (imageUrl == this.lastImage) { // acertou

                        matchedPairs++

                        this.lastImage = null
                        this.lastFlippedView = null
                        this.lastFront = null
                        this.lastBack = null

                        if (matchedPairs == cardPairs.size / 2) { // acertou todos - fim
                            stopwatch.stop()
                            saveRecord()
                            Toast.makeText(this, "Você venceu!", Toast.LENGTH_SHORT).show()

                            val playAgainButton = findViewById<Button>(id.playAgain)
                            playAgainButton.visibility = View.VISIBLE
                        }

                    } else {  // não acertou
                        viewFlipper.postDelayed({
                            hideCard(viewFlipper)
                            hideCard(lastFlippedView as ViewFlipper)
                            lastFlippedView = null
                        }, 1000)
                    }
                }
            }
        }
    }

    private fun showCard(viewFlipper: ViewFlipper) {
        val frontCard = viewFlipper.getChildAt(0) as CardView
        val backCard = viewFlipper.getChildAt(1) as CardView
        frontCard.animate()
            .rotationY(180f)
            .setDuration(1000)
            .withEndAction {
                frontCard.visibility = View.INVISIBLE
                backCard.visibility = View.VISIBLE
                backCard.rotationY = 0f
                viewFlipper.displayedChild = 1
            }
    }

    private fun hideCard(viewFlipper: ViewFlipper) {
        val frontCard = viewFlipper.getChildAt(0) as CardView
        val backCard = viewFlipper.getChildAt(1) as CardView
        backCard.animate()
            ?.rotationY(180f)
            ?.setDuration(1000)
            ?.withEndAction {
                backCard.visibility = View.INVISIBLE
                frontCard.visibility = View.VISIBLE
                frontCard.rotationY = 0f
                viewFlipper.displayedChild = 0
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
                    if (this.lastRecord != "0:00:00") {
                        val lastRecordTextView = findViewById<TextView>(R.id.lastTimeTextView)
                        val text = "Seu Record: $lastRecord"
                        lastRecordTextView.text = text
                    }
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
            timeTextView = findViewById(id.timeView)
            val recordObject = Record(user.email!!, timeTextView.text.toString())
            val recordCollection = database.collection("Record")
            recordCollection.document(user.email!!).set(recordObject).addOnSuccessListener {
                Log.i("record", "Record gravado com sucesso")
            }.addOnFailureListener { error ->
                Log.e("record", "Erro ao gravar Record")
            }
        }
    }

    fun playAgain() {
        val intent = Intent(this, AnimatorActivity::class.java)
        startActivity(intent)
        onCreate(bundle)
        //finish()
    }

    private fun getImage(): String {
        val character = characterService.getRandomCharacter()
        val imageUrl = character.image

        return if (imageUrl.isNotEmpty()) {
            imageUrl
        } else {
            "https://i.imgur.com/fITuWTt.png"
        }
    }
}
