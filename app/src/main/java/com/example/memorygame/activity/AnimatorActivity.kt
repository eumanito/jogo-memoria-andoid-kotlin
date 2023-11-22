package com.example.memorygame.activity

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.memorygame.R
import com.example.memorygame.`object`.Placar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class AnimatorActivity : AppCompatActivity() {
    private lateinit var timeTextView: TextView
    private lateinit var stopwatch: Stopwatch
    private lateinit var frontAnimation: AnimatorSet
    private lateinit var backAnimation: AnimatorSet
    private var isFront = true

    private lateinit var auth: FirebaseAuth

    private lateinit var database: FirebaseFirestore
    private lateinit var placar: CollectionReference

    private var points = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.oneflipcard)

        auth = Firebase.auth
        val user = auth.currentUser

        database = FirebaseFirestore.getInstance()

        val scale = applicationContext.resources.displayMetrics.density
        val cardFront = findViewById<TextView>(R.id.card_front)
        val cardBack = findViewById<TextView>(R.id.card_back)

        cardFront.cameraDistance = 8000 * scale
        cardBack.cameraDistance = 8000 * scale

        frontAnimation = AnimatorInflater.loadAnimator(applicationContext, R.animator.frontanimator) as AnimatorSet
        backAnimation = AnimatorInflater.loadAnimator(applicationContext, R.animator.backanimator) as AnimatorSet

        findViewById<TextView>(R.id.timeView)
        timeTextView = findViewById(R.id.timeView)
        stopwatch = Stopwatch(timeTextView)
        stopwatch.start()

        /* OBTEM PLACAR */
        placar = database.collection("placar")
        if (user != null) {
            placar.document(user.email!!).get().addOnSuccessListener { documento ->
                if (documento != null && documento.exists()) {
                    val placar = documento.toObject(Placar::class.java)
                    this.points = placar?.pontuacao!!

                    val pointTextView = findViewById<TextView>(R.id.pointTextView)
                    var pts = this.points
                    this.points = pts
                    val pointText = "Point: $pts"
                    pointTextView.text = pointText

                } else {
                    Toast.makeText(
                        baseContext,
                        "Erro ao ler o documento, ele não existe ou está vazio"
                        , Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { error ->
                Toast.makeText(
                    baseContext,
                    "Erro ao ler Dados do Servidor: ${error.message.toString()}",Toast.LENGTH_SHORT).show()
            }
        }

        val pointTextView = findViewById<TextView>(R.id.pointTextView)
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

                var pts = this.points
                pts = pts + 1
                this.points = pts
                val pointText = "Point: $pts"
                pointTextView.text = pointText

                /* GRAVA PLACAR */
                if (user != null) {
                    val pontuacao = Placar(user.email!!, pts)
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

