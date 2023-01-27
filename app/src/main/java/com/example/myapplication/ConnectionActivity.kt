package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.EditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ConnectionActivity : AppCompatActivity() {

    private lateinit var mYoutubeLinkEd: EditText
    private lateinit var mSessionCodeEd: EditText
    var sessionID = ""

    val database = Firebase.database
    var myRef = database.getReference("sessions")



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection)

        mYoutubeLinkEd = findViewById(R.id.youtube_link_Ed)
        mSessionCodeEd = findViewById(R.id.session_code_Ed)
    }



    private fun generateSessionId(length: Int) : String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }


    fun createSession(view: View) {
        sessionID = generateSessionId(4)
        myRef.child("session_$sessionID").child("videoURL").setValue(mYoutubeLinkEd.text.toString())

        myRef.child("session_$sessionID").child("seekingState").setValue("false")
        myRef.child("session_$sessionID").child("currentSecond").setValue("0")


        val i = Intent(this, MainActivity::class.java)
        i.putExtra("sessionID", sessionID)
        startActivity(i)

    }

    fun joinSession(view: View) {
        val i = Intent(this, MainActivity::class.java)
        i.putExtra("sessionID", mSessionCodeEd.text.toString())
        myRef.child("session_" + mSessionCodeEd.text.toString()).child("joined").setValue("1")
        myRef.child("session_" + mSessionCodeEd.text.toString()).child("seekingState").setValue("false")

        myRef.addListenerForSingleValueEvent(object: ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.child("session_" + mSessionCodeEd.text.toString()).child("currentSecond").exists()){
                    myRef.child("session_" + mSessionCodeEd.text.toString()).child("currentSecond").setValue("0")
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })


        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            startActivity(i)
        }, 500) // delay of 3 seconds
    }

}