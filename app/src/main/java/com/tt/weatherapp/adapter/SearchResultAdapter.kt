package com.tt.weatherapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tt.weatherapp.R
import com.tt.weatherapp.model.PlaceEntity
import kotlinx.android.synthetic.main.item_search_result.view.*

private const val LOADING = 0
private const val DATA = 1

class SearchResultAdapter(context: Context, private val listResult: MutableList<PlaceEntity>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)
    private lateinit var mListener: OnItemClickListener

    class ViewHolder(itemView: View, listener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                val adapterPos: Int = adapterPosition
                if (adapterPos != RecyclerView.NO_POSITION) {
                    listener.onItemClick(adapterPos)
                }
            }
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        when (viewType) {
            LOADING -> {
                ViewHolder(
                    layoutInflater.inflate(R.layout.item_progress_bar, parent, false),
                    mListener
                )
            }
            else -> {
                ViewHolder(
                    layoutInflater.inflate(R.layout.item_search_result, parent, false),
                    mListener
                )
            }
        }


    override fun getItemCount() = listResult.size

    override fun getItemViewType(position: Int) =
        if (listResult[position].dateSearch == 0L) {
            LOADING
        } else DATA


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val result = listResult[position]

        when (holder.itemViewType) {
            DATA -> {
                holder as ViewHolder

                holder.itemView.text_view_search_title.text = result.title
                holder.itemView.text_view_search_address.text = result.address
            }
        }

    }

    fun addLoading() {
        listResult.add(PlaceEntity("", "", 0.0, 0.0, 0L))
        notifyItemInserted(listResult.size - 1)
    }

    fun removeLoading() {
        val position = listResult.size - 1
        val item = listResult[position]
        if (item.dateSearch == 0L) {
            listResult.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}