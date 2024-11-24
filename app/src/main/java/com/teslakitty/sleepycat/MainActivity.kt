package com.teslakitty.sleepycat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFilePath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnRecord: Button = findViewById(R.id.btnRecord)
        val btnPlay: Button = findViewById(R.id.btnPlay)
        val btnLibrary: Button = findViewById(R.id.btnLibrary)
        val storyTitle: EditText = findViewById(R.id.storyTitle)
        val storyContent: EditText = findViewById(R.id.storyContent)

        // Setup audio file path
        audioFilePath = "${externalCacheDir?.absolutePath}/story_audio.3gp"

        // Check permissions
        if (!hasMicrophonePermission()) {
            requestMicrophonePermission()
        }

        // Handle Recording
        btnRecord.setOnClickListener {
            if (mediaRecorder == null) {
                startRecording()
                btnRecord.text = "Stop Recording"
            } else {
                stopRecording(storyTitle, storyContent)
                btnRecord.text = "Record Narration"
            }
        }

        // Handle Playback
        btnPlay.setOnClickListener {
            playAudio()
        }

        // Navigate to Library screen
        btnLibrary.setOnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java))
        }
    }

    private fun hasMicrophonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestMicrophonePermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 200)
    }

    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFilePath)
            prepare()
            start()
        }
        Toast.makeText(this, "Recording started...", Toast.LENGTH_SHORT).show()
    }

    private fun stopRecording(storyTitle: EditText, storyContent: EditText) {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null

        // Save the story
        saveStory(storyTitle.text.toString(), storyContent.text.toString())
        Toast.makeText(this, "Recording saved: $audioFilePath", Toast.LENGTH_SHORT).show()
    }

    private fun playAudio() {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(audioFilePath)
            prepare()
            start()
        }
        mediaPlayer?.setOnCompletionListener {
            it.release()
            Toast.makeText(this, "Playback completed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveStory(title: String, content: String) {
        if (title.isBlank() || content.isBlank() || audioFilePath.isBlank()) {
            Toast.makeText(this, "Please fill all fields before saving", Toast.LENGTH_SHORT).show()
            return
        }

        val story = Story(title = title, content = content, audioPath = audioFilePath)

        CoroutineScope(Dispatchers.IO).launch {
            StoryDatabase.getDatabase(this@MainActivity).storyDao().insertStory(story)
        }

        Toast.makeText(this, "Story saved!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaRecorder = null
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
