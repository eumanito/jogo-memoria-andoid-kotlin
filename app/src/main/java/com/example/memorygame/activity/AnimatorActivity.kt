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
    private lateinit var placar: CollectionReference
    private lateinit var gridLayout: GridLayout
    private var points = 0
    private var elapsedTime = 0
    private var lastRecord = ""

    private val cardPairs = mutableListOf<String>()
    private var lastFlippedView: ViewFlipper? = null
    private var matchedPairs = 0


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.oneflipcard)

        //https://comicvine.gamespot.com/rick-and-morty/4050-81059/characters/
        cardPairs.add("https://i.imgur.com/fITuWTt.png")
        cardPairs.add("https://i.imgur.com/fITuWTt.png")
        cardPairs.add("https://comicvine.gamespot.com/a/uploads/scale_small/6/66303/4472083-vlcsnap-2015-01-31-18h46m55s179.jpg")
        cardPairs.add("https://comicvine.gamespot.com/a/uploads/scale_small/6/66303/4472083-vlcsnap-2015-01-31-18h46m55s179.jpg")
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

        //obterPlacar()

        getRecord()
    }

    @SuppressLint("SetTextI18n")
    private fun createCardView(cardTitle: String, imageUrl: String) {
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
            } else {
                // Se o card de trás for exibido, vire para o card da frente
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

            if (lastFlippedView == null) {
                // Se este é o primeiro card virado
                lastFlippedView = viewFlipper
            } else {
                // Se este é a segundo card virado
                if (imageUrl == cardPairs[gridLayout.indexOfChild(viewFlipper) / 2]) {

                    // Correspondência encontrada

                    /*val pointTextView = findViewById<TextView>(id.pointTextView)
                    val pts = this.points + 1
                    pointTextView.text = "Pontos: $pts"
                    this.points = pts
                    gravarPlacar()*/

                    matchedPairs++
                    if (matchedPairs == cardPairs.size / 2) {
                        // Todas as correspondências foram encontradas - jogo completo
                        stopwatch.stop()
                        saveRecord()
                        Toast.makeText(this, "Você venceu!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Não há correspondência, vira os cards de volta
                    viewFlipper.postDelayed({
                        viewFlipper.displayedChild = 0
                        lastFlippedView?.displayedChild = 0
                        lastFlippedView = null
                    }, 2000)
                }
            }
        }
    }

    /*private fun obterPlacar() {
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
    }*/

    private fun getRecord() {
        val recordCollection = database.collection("Record")
        auth = Firebase.auth
        val user = auth.currentUser

        if (user != null) {
            recordCollection.document(user.email!!).get().addOnSuccessListener { documento ->
                if (documento != null && documento.exists()) {
                    val recordObject = documento.toObject(Record::class.java)
                    this.lastRecord = recordObject?.tempo.toString()
                    val lastRecordTextView = findViewById<TextView>(R.id.lastRecordTextView)
                    val text = "Seu Record: $lastRecord"
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
            timeTextView = findViewById(id.timeView)
            val recordObject = Record(user.email!!, this.elapsedTime, timeTextView.text.toString())
            val recordCollection = database.collection("Record")
            recordCollection.document(user.email!!).set(recordObject).addOnSuccessListener {
                Log.i("record", "Record gravado com sucesso")
            }.addOnFailureListener { error ->
                Log.e("record", "Erro ao gravar Record")
            }
        }
    }
}

