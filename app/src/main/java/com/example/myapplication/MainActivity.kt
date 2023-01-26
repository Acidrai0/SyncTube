package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.DefaultPlayerUiController



class MainActivity : AppCompatActivity() {

    lateinit var initializedYouTubePlayer: YouTubePlayer
    lateinit var youTubePlayerView: YouTubePlayerView


    private lateinit var seekToEditText: EditText
    lateinit var stateOfPlayer: TextView
    lateinit var seekinTime: TextView

    val database = Firebase.database
    var myRef = database.getReference("sessions")

    private lateinit var defaultPlayerUiController: DefaultPlayerUiController



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        seekToEditText = findViewById(R.id.seektoEditText)
        stateOfPlayer = findViewById(R.id.stateOfPlayer_id_tv)
        stateOfPlayer = findViewById(R.id.seeking_id_tv)



        initializePlayer()
        loadVideo("LtMvg0RfSuA")

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                Log.e("pleasework", dataSnapshot.toString())

                val videoState = dataSnapshot.child("session1").child("videoState").getValue(String::class.java)
                val seekingState = dataSnapshot.child("session1").child("seekingState").getValue(String::class.java)
                val seekingTime = dataSnapshot.child("session1").child("seekingTime").getValue(String::class.java)
                var seeked = false


                if (videoState == "PAUSED") {
                    pauseVideo()
                }else if (videoState == "PLAYING"){
                    playVideo()
                }else if(videoState == "UNSTARTED"){
                    playVideo()
                }


                if(seekingState == "false" && !seeked){
                    seekTo(seekingTime!!.toFloat())
                    seeked=true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })

    }




    private fun initYoutubePlayerView() {
        youTubePlayerView = findViewById(R.id.youtube_player_view)
        lifecycle.addObserver(youTubePlayerView)

        youTubePlayerView.getYouTubePlayerWhenReady(object: YouTubePlayerCallback {
            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                initializedYouTubePlayer = youTubePlayer
              //  loadVideo("LtMvg0RfSuA")

                //initializedYouTubePlayer.addListener()

            }

        })

    }



    private fun loadVideo(videoId: String) {
        if (::initializedYouTubePlayer.isInitialized) {
            initializedYouTubePlayer.loadVideo(videoId, 700f)
        } else {
            youTubePlayerView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                    initializedYouTubePlayer = youTubePlayer
                    initializedYouTubePlayer.loadVideo(videoId, 700f)
                }
            })
        }
    }


    private fun pauseVideo() {
        if (::initializedYouTubePlayer.isInitialized) {
            initializedYouTubePlayer.pause()
        } else {
            youTubePlayerView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                    initializedYouTubePlayer = youTubePlayer
                    initializedYouTubePlayer.pause()
                }
            })
        }
    }


    private fun playVideo() {
        if (::initializedYouTubePlayer.isInitialized) {
            initializedYouTubePlayer.play()
        } else {
            youTubePlayerView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                    initializedYouTubePlayer = youTubePlayer
                    initializedYouTubePlayer.play()
                }
            })
        }
    }


    private fun seekTo(time: Float) {
        if (::initializedYouTubePlayer.isInitialized) {
            initializedYouTubePlayer.seekTo(time)
        } else {
            youTubePlayerView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                    initializedYouTubePlayer = youTubePlayer
                    initializedYouTubePlayer.seekTo(time)
                }
            })
        }
    }


    fun loadBtn(view: View) {
        loadVideo("LtMvg0RfSuA")
    }
    fun pauseBtn(view: View) {
        pauseVideo()
    }
    fun seekToBtn(view: View) {
        val time : Float = seekToEditText.text.toString().toFloat()
        seekTo(time)
    }

    fun playBtn(view: View) {
        playVideo()
    }

    private fun initializePlayer() {
        youTubePlayerView = findViewById(R.id.youtube_player_view)
        lifecycle.addObserver(youTubePlayerView)

        youTubePlayerView.enableAutomaticInitialization = false

        val listener = object: AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {

                defaultPlayerUiController = DefaultPlayerUiController(youTubePlayerView, youTubePlayer, stateOfPlayer, database)
                youTubePlayerView.setCustomPlayerUi(defaultPlayerUiController.rootView)

                initializedYouTubePlayer = youTubePlayer

            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                stateOfPlayer.text = state.toString()
                if(state == PlayerConstants.PlayerState.PLAYING){
                    myRef.child("session1").child("videoState").setValue(state.toString())
                }else if (state == PlayerConstants.PlayerState.PAUSED){
                    myRef.child("session1").child("videoState").setValue(state.toString())
                }else if(state == PlayerConstants.PlayerState.UNSTARTED){
                    myRef.child("session1").child("videoState").setValue(state.toString())
                }

            }


        }

        val options = IFramePlayerOptions.Builder().controls(0).build()
        youTubePlayerView.initialize(listener, options)
    }




}







