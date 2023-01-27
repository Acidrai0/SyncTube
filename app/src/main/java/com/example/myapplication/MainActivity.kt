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
    lateinit var defaultPlayerUiController: DefaultPlayerUiController



    lateinit var seekToEditText: EditText
    lateinit var stateOfPlayer: TextView
    lateinit var seekinTime: TextView
    lateinit var videoURLEditText: EditText

    val database = Firebase.database
    var dbPath: String = "sessions"
    var myRef = database.getReference(dbPath)


    var sessionID = ""
    var videoLoaded = false
    var currentVideoId = ""
    var currentSecond = "0"

    var updateJoinedUsersTime = false
    var currentUsers = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        seekToEditText = findViewById(R.id.seektoEditText)
        stateOfPlayer = findViewById(R.id.stateOfPlayer_id_tv)
        stateOfPlayer = findViewById(R.id.seeking_id_tv)
        videoURLEditText = findViewById(R.id.load_videoLink_ED)

        sessionID = intent.getStringExtra("sessionID").toString()



        dbPath = "sessions/session_$sessionID"
        myRef = database.getReference(dbPath)

        myRef.child("seekingState").setValue("false")

        initializePlayer()


        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                Log.e("pleasework", dataSnapshot.toString())

                val videoState = dataSnapshot.child("videoState").getValue(String::class.java)
                val seekingTime = dataSnapshot.child("seekingTime").getValue(String::class.java)?.toString()
                val currentSeconds = dataSnapshot.child("currentSecond").getValue(String::class.java)?.toInt()
                var joined = 0
                //Very Bad Logic I know


                if(dataSnapshot.child("joined").exists()){
                     joined = dataSnapshot.child("joined").getValue(String::class.java)?.toInt()!!
                        if(joined > currentUsers){
                            currentUsers = joined
                            updateJoinedUsersTime = true
                        }else{
                            updateJoinedUsersTime = false
                        }

                }




                if (dataSnapshot.child("videoURL").exists()) {
                    val videoId = dataSnapshot.child("videoURL").getValue(String::class.java)?.toString()
                    if(!videoLoaded) {
                        if (videoId != null) {
                            Log.e("currentSeconds", currentSeconds.toString())
                            if(joined>0){
                                if (currentSeconds != null) {
                                    loadVideo(videoId,currentSeconds + 5)
                                }
                            }else{
                                if (currentSeconds != null) {
                                    loadVideo(videoId,currentSeconds)
                                }
                            }
                            videoLoaded = true
                            currentVideoId = videoId
                            currentSecond = currentSeconds.toString()
                        }
                    }
                    if (currentVideoId != videoId){
                        videoLoaded = false
                        myRef.child("seekingTime").setValue("0")
                    }

                }


                if (videoState == "PAUSED") {
                    pauseVideo()
                }else if (videoState == "PLAYING"){
                    playVideo()
                }else if(videoState == "UNSTARTED"){
                    playVideo()
                }

                var seeked = false
                if (dataSnapshot.child("seekingState").exists()) {
                    val seekingState = dataSnapshot.child("seekingState").getValue(String::class.java)?.toString()
                    if(seekingState == "false" && !seeked){
                        if (seekingTime != null) {
                            seekTo(seekingTime.toFloat())
                        }
                        seeked=true
                    }
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



    private fun loadVideo(videoId: String, startSeconds: Int) {

        if (::initializedYouTubePlayer.isInitialized) {
            if (startSeconds > 0) {
                initializedYouTubePlayer.loadVideo(videoId, startSeconds.toFloat())
            } else {
                initializedYouTubePlayer.loadVideo(videoId, 0.0f)
            }
        } else {
            youTubePlayerView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                    initializedYouTubePlayer = youTubePlayer
                    if (startSeconds > 0) {
                        initializedYouTubePlayer.loadVideo(videoId, startSeconds.toFloat())
                    } else {
                        initializedYouTubePlayer.loadVideo(videoId, 0.0f)
                    }
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
        myRef.child("videoURL").setValue(videoURLEditText.text.toString())
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

                defaultPlayerUiController = DefaultPlayerUiController(youTubePlayerView, youTubePlayer, stateOfPlayer, database, dbPath)
                youTubePlayerView.setCustomPlayerUi(defaultPlayerUiController.rootView)

                initializedYouTubePlayer = youTubePlayer

            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                //currentSecond = second.toInt()
                if(updateJoinedUsersTime){
                    myRef.child("currentSecond").setValue(second.toInt().toString())
                    updateJoinedUsersTime = false
                }

            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                stateOfPlayer.text = state.toString()
                if(state == PlayerConstants.PlayerState.PLAYING){
                    myRef.child("videoState").setValue(state.toString())
                }else if (state == PlayerConstants.PlayerState.PAUSED){
                    myRef.child("videoState").setValue(state.toString())
                    myRef.child("seekingTime").setValue(currentSecond.toString())
                }else if(state == PlayerConstants.PlayerState.UNSTARTED){
                    myRef.child("videoState").setValue(state.toString())
                }

            }


        }

        val options = IFramePlayerOptions.Builder().controls(0).build()
        youTubePlayerView.initialize(listener, options)
    }




}







