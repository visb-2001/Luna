package com.example.luna.adapters


import android.content.Context
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.luna.R
import com.example.luna.classes.Chats
import java.util.*


class MessageAdapter(
    responseMessages: List<Chats>,
    context: Context
) :
    RecyclerView.Adapter<MessageAdapter.CustomViewHolder>() {
    var messages: MutableList<Chats> = responseMessages as MutableList<Chats>
    var context: Context = context
    val TTS = TextToSpeech(context, TextToSpeech.OnInitListener {})

    var isResult = false
    var isTimer = false
    var imgLoaded = true
    var timeStop = false

    inner class CustomViewHolder(itemView: View) :
        ViewHolder(itemView) {
        lateinit var title: TextView
        lateinit var extract: TextView
        lateinit var image: ImageView
        lateinit var textView: TextView
        lateinit var timerView: TextView
        lateinit var timeBar : ProgressBar
        init {
            TTS.language = Locale.US
            when {
                isResult -> {
                    title = itemView.findViewById(R.id.title)
                    extract = itemView.findViewById(R.id.description)
                    image = itemView.findViewById(R.id.image)
                }
                isTimer -> {
                    timerView = itemView.findViewById(R.id.timeLeft)
                    timeBar = itemView.findViewById(R.id.timerBar)
                }
                else -> {
                    textView = itemView.findViewById(R.id.textMessage)
                }
            }

        }

    }

    override fun getItemViewType(position: Int): Int {
        when {
            messages[position].isMe -> {
                isResult = false
                isTimer = false
                return R.layout.me_bubble
            }
            messages[position].isResult -> {
                isResult = true
                isTimer = false
                return R.layout.bot_wiki

            }
            messages[position].isTimer -> {
                isTimer = true
                isResult = false
                return R.layout.bot_timer
            }
            else -> {
                isResult = false
                isTimer = false
                return R.layout.bot_bubble
            }
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CustomViewHolder {
        return CustomViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(viewType, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: CustomViewHolder,
        position: Int
    ) {
        if(isResult){
            holder.title.text = messages[position].title
            holder.extract.text = messages[position].extract
            if(messages[position].image != null){
                holder.image.setImageBitmap(messages[position].image)
            }
            else{
                holder.image.visibility = View.GONE

            }
        }
        else if(isTimer){
            var mLen = messages.size
            holder.timeBar.max = messages[position].time
            var timer = object: CountDownTimer(messages[position].time.toLong(), 10L){
                override fun onTick(millisUntilFinished: Long) {
                    if(!timeStop){
                        var prog = (millisUntilFinished).toInt()
                        Log.d("time",prog.toString())
                        holder.timeBar.progress = prog-10
                        var timerText = prog/1000
                        holder.timerView.text = (timerText/3600).toInt().toString()+":"+((timerText%3600)/60).toInt().toString()+":"+((timerText%3600)%60).toString()
                    }
                    if(mLen < messages.size){
                        timeStop = true
                    }
                }
                override fun onFinish() {
                    val response =
                        Chats("Timer done.", false)
                    messages.add(response)
                    notifyDataSetChanged()
                }
            }
            if(!messages[position].timerStarted){
                timer.start()
                messages[position].timerStarted = true
            }

        }
        else{
            holder.textView.text = messages[position].textMessage
           if(!messages[position].isMe && !messages[position].spoken){
               if(messages[position].isResult){
                   TTS.speak(messages[position].title,TextToSpeech.QUEUE_ADD,null)
               }
               else{
                   TTS.speak(messages[position].textMessage,TextToSpeech.QUEUE_ADD,null)
               }
               messages[position].spoken = true
           }

        }

    }

}
