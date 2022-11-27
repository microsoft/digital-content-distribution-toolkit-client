package com.microsoft.mobile.polymer.mishtu.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.microsoft.mobile.polymer.mishtu.R
import java.util.*

class VoiceSearchUtil {

    companion object {
        var instance: VoiceSearchUtil? = null

        @Synchronized
        fun getNewInstance(): VoiceSearchUtil {
            if (instance == null) {
                instance = VoiceSearchUtil()
            }
            return instance as VoiceSearchUtil
        }
    }

    fun voiceInput(context: Context, contract: ActivityResultLauncher<Intent>) {

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        if (intent.resolveActivity(context.packageManager) != null) {
            contract.launch(intent)
        } else {
            Toast.makeText(context,
                context.getString(R.string.voice_input_not_supported),
                Toast.LENGTH_SHORT).show()
        }
    }

    fun getContract(
        fragment: Fragment,
        callback: VoiceSearchCallBack,
    ): ActivityResultLauncher<Intent> {
        return fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                // your operation...

                if (data != null) {
                    val matchedResult = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    val res = matchedResult?.get(0)
                    if (res != null)
                        callback.onSerachQueryDetected(res)
                }
            }
        }
    }

    fun getContract(
        activity: AppCompatActivity,
        callback: VoiceSearchCallBack,
    ): ActivityResultLauncher<Intent> {

        return activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                // your operation...

                if (data != null) {
                    val matchedResult = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    val res = matchedResult?.get(0)
                    if(res != null)
                    callback.onSerachQueryDetected(res)
                }
            }
        }
    }

    interface VoiceSearchCallBack {
        fun onSerachQueryDetected(res: String)
    }

}