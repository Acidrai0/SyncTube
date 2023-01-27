package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
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

        val i = Intent(this, MainActivity::class.java)
        i.putExtra("sessionID", sessionID)
        startActivity(i)

    }

    fun joinSession(view: View) {

        myRef.child("session_" + mSessionCodeEd.text.toString()).child("joined").setValue("1")
        val i = Intent(this, MainActivity::class.java)
        i.putExtra("sessionID", mSessionCodeEd.text.toString())
        startActivity(i)
    }

}