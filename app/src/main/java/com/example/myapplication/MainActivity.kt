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
    lateinit var videoUrlEditText: EditText
    lateinit var stateOfPlayer: TextView
    lateinit var seekinTime: TextView


    val database = Firebase.database
    var dbPath: String = "sessions"
    var myRef = database.getReference(dbPath)

    // initialize variables for session ID, video load status, and current video ID

    var sessionID = ""
    var videoLoaded = false
    var currentVideoId = ""

    // variable to track if users have joined the session since the last check
    var updateJoinedUsersTime = false
    // variable to track the current number of users in the session
    var currentUsers = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // initialize views
        seekToEditText = findViewById(R.id.seektoEditText)
        stateOfPlayer = findViewById(R.id.stateOfPlayer_id_tv)
        stateOfPlayer = findViewById(R.id.seeking_id_tv)
        videoUrlEditText = findViewById(R.id.load_video_EditText)

        // get session ID from intent
        sessionID = intent.getStringExtra("sessionID").toString()


        // set reference to specific session in Firebase database
        dbPath = "sessions/session_$sessionID"
        myRef = database.getReference(dbPath)

        // initialize YouTube player
        initializePlayer()

        //Handle all the firebase Logic here
        firebaseHandler()



    }


    private fun firebaseHandler(){
        // add listener for changes to the session in the Firebase database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                // check if seekingTime, seekingState, and joined keys exist in the snapshot
                // and set initial values if they do not
                if(!dataSnapshot.child("seekingTime").exists()){
                    myRef.child("seekingTime").setValue("0")
                }
                if(!dataSnapshot.child("seekingState").exists()){
                    myRef.child("seekingState").setValue("false")
                }
                if(!dataSnapshot.child("joined").exists()){
                    myRef.child("joined").setValue("0")
                }


                Log.e("pleasework", dataSnapshot.toString())


                // get values for video state, seeking state, seeking time, and video ID from snapshot
                val videoState = dataSnapshot.child("videoState").getValue(String::class.java)
                val seekingState = dataSnapshot.child("seekingState").getValue(String::class.java)
                val seekingTime = dataSnapshot.child("seekingTime").getValue(String::class.java)


                // check if there is a joined key in the snapshot, and update currentUsers
                if(dataSnapshot.child("joined").exists()){
                    val joined = dataSnapshot.child("joined").getValue(String::class.java)?.toInt()!!
                    if(joined > currentUsers){
                        updateJoinedUsersTime = true
                        currentUsers = joined
                    }else{
                        updateJoinedUsersTime = false
                    }

                }

                // Check if the "videoURL" child exists in the database
                if (dataSnapshot.child("videoURL").exists()) {
                    val videoId = dataSnapshot.child("videoURL").getValue(String::class.java)?.toString()
                    // Check if the video has not been loaded yet
                    if(!videoLoaded) {
                        if (videoId != null) {
                            // Load the video
                            loadVideo(videoId)
                            videoLoaded = true
                            currentVideoId = videoId
                        }
                    }
                    // Check if the videoId has been changed
                    if (currentVideoId != videoId){
                        videoLoaded = false
                        myRef.child("seekingTime").setValue("0")

                    }
                }


//                if(!videoLoaded) {
//                    //Check if video is not loaded
//                    loadVideo(videoId)
//                    videoLoaded = true
//                }

                // Get the current state of the video from Database and
                // change youtube player state
                if (videoState == "PAUSED") {
                    pauseVideo()
                }else if (videoState == "PLAYING"){
                    playVideo()
                }else if(videoState == "UNSTARTED"){
                    playVideo()
                }


                // A flag to check if the video has been seeked
                var seeked = false

                // Check if the seeking state is "false" and the
                if(seekingState == "false" && !seeked){
                    seekTo(seekingTime!!.toFloat())
                    // Set the flag to true to indicate that the video has been seeked
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
            initializedYouTubePlayer.loadVideo(videoId, 0f)
        } else {
            youTubePlayerView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                    initializedYouTubePlayer = youTubePlayer
                    initializedYouTubePlayer.loadVideo(videoId, 0f)
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
        myRef.child("videoURL").setValue(videoUrlEditText.text.toString())
        loadVideo(videoUrlEditText.text.toString())
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

            var currentSecond = 0.0f
            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                currentSecond = second
                if(updateJoinedUsersTime){
                    myRef.child("seekingTime").setValue((second+1.0f).toString())
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







