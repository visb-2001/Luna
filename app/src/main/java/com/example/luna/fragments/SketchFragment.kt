package com.example.luna.fragments


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.luna.*
import com.example.luna.database.SketchDatabase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_sketch.*
import kotlinx.android.synthetic.main.fragment_sketch.view.*
import java.io.File
import java.io.FileOutputStream


class SketchFragment : Fragment() {


    var isPencilSeleted = true
    lateinit var canvas:MyCanvasView



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        canvas = context?.let { MyCanvasView(it) }!!
        val params = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.addRule(RelativeLayout.BELOW, R.id.sketchViewTitle)
        params.addRule(RelativeLayout.ABOVE, R.id.lockToggle)
        val rootView: View =
            inflater.inflate(R.layout.fragment_sketch, container, false)

        rootView.sketchView.addView(canvas,params)
        return rootView
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pencil.setBackgroundResource(R.drawable.selected)
        eraser.setBackgroundResource(R.drawable.not_selected)

        share.setOnClickListener {
            Thread {
                var sketch  = SketchDatabase.getInstance(requireContext()).sketchDao().readSketch()
                if(sketch.isNotEmpty()) {
                    val image = Converter.getImage(sketch[0].sketchBitmap)
                    val file = File(requireActivity().externalCacheDir,"image.png")
                    val fout = FileOutputStream(file)
                    image.compress(Bitmap.CompressFormat.PNG, 100, fout)
                    fout.flush()
                    fout.close()
                    file.setReadable(true,false)
                    val text = "My Sketch"
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(
                            Intent.EXTRA_STREAM, FileProvider.getUriForFile(requireContext(),
                                BuildConfig.APPLICATION_ID + ".provider", file))
                        putExtra(Intent.EXTRA_TEXT, text)
                        type = "image/png"
                    }
                    ContextCompat.startActivity(
                        requireContext(),
                        Intent.createChooser(sendIntent, "Share"),
                        null
                    )
                }
                else{
                    requireActivity().runOnUiThread {
                        Toast.makeText(context,"The sketch is empty", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }

        clear.setOnTouchListener { _, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN ->
                {
                    clear.setBackgroundResource(R.drawable.selected)
                }
                MotionEvent.ACTION_UP ->
                {
                    canvas?.clear()
                    clear.setBackgroundColor(Color.parseColor("#00000000"))
                }

            }
            false
        }
        pencil.setOnClickListener {
            lockpage()
            if(!isPencilSeleted){
                pencil.setBackgroundResource(R.drawable.selected)
                eraser.setBackgroundResource(R.drawable.not_selected)
                isPencilSeleted = true
                canvas?.setPaintColor(R.color.colorAccent,12f)
            }
        }
        eraser.setOnClickListener {
            lockpage()
            if(isPencilSeleted){
                eraser.setBackgroundResource(R.drawable.selected)
                pencil.setBackgroundResource(R.drawable.not_selected)
                isPencilSeleted = false
                canvas?.setPaintColor(R.color.colorPrimary,40f)
            }
        }






        lockToggle.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                (activity as MainActivity?)!!.viewPager.setPagingEnabled(false)
                canvas?.setTouchValid(true)

            }
            else{
                (activity as MainActivity?)!!.viewPager.setPagingEnabled(true)
                canvas?.setTouchValid(false)
            }
        }


    }
    private fun lockpage(){
        lockToggle.isChecked = true
    }

}