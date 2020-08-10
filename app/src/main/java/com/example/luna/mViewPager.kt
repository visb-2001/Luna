package com.example.luna

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import androidx.viewpager.widget.ViewPager


class mViewPager : ViewPager {
    private var pagingEnabled = true

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!,
        attrs
    ) {
    }

    fun setPagingEnabled(pagingEnabled: Boolean) {
        this.pagingEnabled = pagingEnabled
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return pagingEnabled && super.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return pagingEnabled && super.onTouchEvent(event)
    }
}