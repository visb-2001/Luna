package com.example.luna

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.luna.classes.Sketch
import com.example.luna.database.SketchDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream

// Stroke width for the the paint.
private const val STROKE_WIDTH = 12f
var isTouchValid = false

/**
 * Custom view that follows touch events to draw on a canvas.
 */
class MyCanvasView(context: Context) : View(context) {



    // Path representing what's currently being drawn
    private val curPath = Path()

    //lateinit var saveCanvas: Canvas
    lateinit var savedBitmap: Bitmap
    lateinit var curCanvas: Canvas
    lateinit var curBitmap: Bitmap
    lateinit var mergeBitmap: Bitmap

    private val drawColor = ResourcesCompat.getColor(resources, R.color.colorAccent, null)
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorPrimary, null)


    // Set up the paint with which to draw.
    private val paint = Paint().apply {
        color = drawColor
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
    }



    private var currentX = 0f
    private var currentY = 0f

    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f



    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        savedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        curBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mergeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        curCanvas = Canvas(curBitmap)

        Thread {
            var sketch  = SketchDatabase.getInstance(context).sketchDao().readSketch()
            if(sketch.isNotEmpty()){
                if(sketch[0].sketchBitmap != null){
                    savedBitmap = Converter.getImage(sketch[0].sketchBitmap)
                }
                invalidate()
            }
            else{
                curCanvas.drawColor(backgroundColor)
            }

        }.start()
    }

    override fun onDraw(canvas: Canvas) {
        // Draw the drawing so far
        canvas.drawBitmap(savedBitmap, 0f, 0f, paint)
        canvas.drawBitmap(curBitmap, 0f, 0f, paint)
        // Draw any current squiggle
        canvas.drawPath(curPath, paint)
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y
        return if(isTouchValid){
            when (event.action) {
                MotionEvent.ACTION_DOWN -> touchStart()
                MotionEvent.ACTION_MOVE -> touchMove()
                MotionEvent.ACTION_UP -> touchUp()
            }
            true
        } else{
            false
        }

    }


    private fun touchStart() {
        curPath.reset()
        curPath.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
    }

    private fun touchMove() {
        curPath.quadTo(currentX, currentY, (motionTouchEventX + currentX) / 2, (motionTouchEventY + currentY) / 2)
        currentX = motionTouchEventX
        currentY = motionTouchEventY

        invalidate()
    }

    private fun touchUp() {
        // Reset the path so it doesn't get drawn again.
        curCanvas.drawPath(curPath,paint)
        var sketch = Sketch()
        var mergedBitmap = merge(savedBitmap,curBitmap)
        sketch.sketchBitmap = Converter.getBytes(mergedBitmap)
        //curBitmap = mergedBitmap
        Thread{
           // Thread.sleep(200)
            SketchDatabase.getInstance(context).sketchDao().saveSketch(sketch)
        }.start()
        if(sketch.sketchBitmap != null){
            Log.d("upload","gghgh")
            val ref = FirebaseStorage.getInstance().getReference("/sketches/${FirebaseAuth.getInstance().uid}")
            ref.putBytes(sketch.sketchBitmap!!).addOnFailureListener {
                Log.d("upload",it.message)
            }
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {
                        sketch.sketchUrl = it.toString()
                        Log.d("upload",it.toString())
                        sketch.sketchBitmap = null
                        FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}").child("sketch").setValue(sketch)
                    }
                }
        }

        curPath.reset()
    }

    fun setTouchValid(stat: Boolean){
        isTouchValid = stat
    }

    fun setPaintColor(col: Int,width: Float){
        paint.color = ResourcesCompat.getColor(resources, col, null)
        paint.strokeWidth = width
    }

    fun merge(bmp1: Bitmap, bmp2: Bitmap): Bitmap {
        val bmMerge =
            Bitmap.createBitmap(bmp1.width, bmp1.height, bmp1.config)
        val canvas = Canvas(bmMerge)
        canvas.drawBitmap(bmp1, 0f, 0f, null)
        canvas.drawBitmap(bmp2, 0f, 0f, null)
        return bmMerge
    }

    fun clear(){
        savedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        curCanvas.drawColor(backgroundColor)
        Thread {
            var sketchList  = SketchDatabase.getInstance(context).sketchDao().readSketch()
            if(sketchList.isNotEmpty()){
                SketchDatabase.getInstance(context).sketchDao().deleteSketch(sketchList[0])
            }
            var sketch = Sketch()
            sketch.sketchUrl = ""
            sketch.sketchBitmap = null
            FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}").child("sketch").setValue(sketch)
            invalidate()
        }.start()

    }

}