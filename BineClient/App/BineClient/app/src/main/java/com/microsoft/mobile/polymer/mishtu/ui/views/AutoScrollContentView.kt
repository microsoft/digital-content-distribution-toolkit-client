package com.microsoft.mobile.polymer.mishtu.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ViewAutoScrollContentBinding
import com.microsoft.mobile.polymer.mishtu.storage.entities.Content
import com.microsoft.mobile.polymer.mishtu.ui.adapter.AutoScrollContentAdapter
import kotlinx.coroutines.delay
import kotlin.collections.ArrayList


class AutoScrollContentView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyle: Int = 0) : FrameLayout(context, attrs, defStyle) {

    private val bookingModels = ArrayList<Content>()
    lateinit var binding: ViewAutoScrollContentBinding
    lateinit var adapter: AutoScrollContentAdapter
    private var margin: Int = 0
    private var clickListener: OnClickListener? = null
    private lateinit var layoutManager: LinearLayoutManager
    private var isScrolling: Boolean = false
    private var shouldStop: Boolean = false

    init {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.AutoScrollContentView)
            margin = a.getDimension(R.styleable.AutoScrollContentView_viewMargin, 0F).toInt()
        }
        initView()
    }

    private fun initView() {
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = ViewAutoScrollContentBinding.inflate(inflater, this, true)

        adapter = AutoScrollContentAdapter(bookingModels, context,
            context.resources.getDimension(R.dimen.dp_90).toInt(),
            context.resources.getDimension(R.dimen.dp_120).toInt(),
            margin
        )

        layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = RecyclerView.HORIZONTAL
        binding.recyclerView.layoutManager = layoutManager

        binding.recyclerView.adapter = adapter

        binding.recyclerView.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                clickListener?.onClick()
                return true
            }
        })
    }

    fun addOnClickListener(listener: OnClickListener) {
        clickListener = listener
    }

    fun setData(bookingModels: List<Content>) {
        this.bookingModels.clear()
        this.bookingModels.addAll(bookingModels)
        adapter.notifyDataSetChanged()
    }

    suspend fun startAutoScroll() {
        if (isScrolling) {
            return
        }
        isScrolling = true
        shouldStop = false
        startAutoScrollRecursive()
    }

    private tailrec suspend fun startAutoScrollRecursive() {
        if (shouldStop) return
        if (binding.recyclerView.canScrollHorizontally(1)) {
            binding.recyclerView.smoothScrollBy(scrollDeltaX, 0, null, delayBetweenScrolls.toInt())
        } else {
            val firstPosition =
                (binding.recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            if (firstPosition != RecyclerView.NO_POSITION) {
                isScrolling = false
                return
            }
        }
        delay(delayBetweenScrolls)
        startAutoScrollRecursive()
    }

    fun stopScroll() {
        shouldStop = true
        isScrolling = false
        binding.recyclerView.scrollToPosition(0)
    }

    private val delayBetweenScrolls = 1500L
    private val scrollDeltaX = 200

    interface OnClickListener {
       fun onClick()
    }
}