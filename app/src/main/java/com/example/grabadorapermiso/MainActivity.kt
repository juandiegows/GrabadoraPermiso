package com.example.grabadorapermiso

import android.Manifest
import android.content.Intent
import android.media.MediaRecorder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import com.example.grabadorapermiso.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    private var audioFilePath: Uri? = null

    private lateinit var mediaRecorder: MediaRecorder
    private var isRecording: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mediaRecorder = MediaRecorder()
        binding.apply {
            btnPermisoAlmacenamiento.setOnClickListener {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            btnPermisoMicrofono.setOnClickListener {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            startStopButton.setOnClickListener {
                if (audioFilePath == null) {
                    // Mostrar el diálogo de selección de ubicación
                    showDirectoryPicker()
                } else {
                    // Iniciar o detener la grabación según el estado actual
                    toggleRecording()
                }
            }
        }
    }

    private fun showDirectoryPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        recordingButtonLauncher.launch(intent)
    }
    private fun toggleRecording() {
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }
    val recordingButtonLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    audioFilePath = uri
                    startRecording()
                }
            }
        }


    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {

                Toast.makeText(this, "Aceptado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "denegado", Toast.LENGTH_SHORT).show()
            }
        }

    private fun startRecording() {
        try {
            isRecording = true
            binding.startStopButton.setText("detener")
            mediaRecorder.apply {
                reset()
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)


                val documentFile = DocumentFile.fromTreeUri(this@MainActivity, audioFilePath!!)
                val audioFileName = "audio.mp3"
                val audioFile = documentFile!!.createFile("audio/mpeg", audioFileName)






                setOutputFile(contentResolver.openFileDescriptor(audioFile!!.uri, "w")?.fileDescriptor)

                prepare()
                start()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        try {
            isRecording = false
            binding.startStopButton.setText("Grabar")
            mediaRecorder.stop()
            mediaRecorder.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}