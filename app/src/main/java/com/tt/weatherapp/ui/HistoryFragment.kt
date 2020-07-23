package com.tt.weatherapp.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tt.weatherapp.R
import com.tt.weatherapp.adapter.SearchResultAdapter
import com.tt.weatherapp.model.PlaceEntity
import com.tt.weatherapp.viewmodel.PlaceViewModel
import com.tt.weatherapp.viewmodel.WeatherViewModel
import kotlinx.android.synthetic.main.fragment_history.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class HistoryFragment : Fragment() {

    private val listPlaceResult: MutableList<PlaceEntity> = ArrayList()
    private lateinit var searchResultAdapter: SearchResultAdapter
    private val sharedPref: SharedPreferences by inject()
    private val weatherViewModel: WeatherViewModel by sharedViewModel(from = { requireActivity() })
    private val placeViewModel: PlaceViewModel by sharedViewModel(from = { requireActivity() })
    private var isAtBottom = false
    private var isLoading = false
    private var isNext = true
    private var limit = 10
    private var page = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        generateSearchResultRecyclerView(requireContext())
        addRecyclerViewListener()

        searchResultAdapter.addLoading()
        placeViewModel.getPlacePaging(limit, page)
        isLoading = true

        placeViewModel.placeAdded.observe(viewLifecycleOwner, Observer {
            if (!listPlaceResult.contains(it)) {
                listPlaceResult.add(0, it)
                searchResultAdapter.notifyItemInserted(0)
            } else {
                val oldPos = listPlaceResult.indexOf(it)
                listPlaceResult.removeAt(oldPos)
                listPlaceResult.add(0, it)
                searchResultAdapter.notifyItemMoved(oldPos, 0)
            }
        })

        placeViewModel.listPlacePaging.observe(viewLifecycleOwner, Observer {
            searchResultAdapter.removeLoading()
            listPlaceResult.addAll(it)
            searchResultAdapter.notifyDataSetChanged()
            if (it.size < limit || it.isEmpty()) {
                isNext = false
            }
            isLoading = false
        })
    }

    private fun addRecyclerViewListener() {
        recycler_view_search_history.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(
                recyclerView: RecyclerView,
                newState: Int
            ) {
                super.onScrollStateChanged(recyclerView, newState)
                isAtBottom =
                    !recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE
                if (isAtBottom && !isLoading && isNext) {
                    searchResultAdapter.addLoading()
                    recyclerView.scrollToPosition(listPlaceResult.size - 1)
                    isLoading = true
                    page += 10
                    placeViewModel.getPlacePaging(limit, page)
                }
            }
        })
    }

    private fun generateSearchResultRecyclerView(context: Context) {
        val layoutManager = LinearLayoutManager(context)
        searchResultAdapter = SearchResultAdapter(context, listPlaceResult)
        searchResultAdapter.setOnItemClickListener(object :
            SearchResultAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val place = listPlaceResult[position]
                sharedPref.edit().apply {
                    putString("address", place.address)
                }.apply()

                weatherViewModel.setLocationLatLon(
                    place.latitude,
                    place.longitude
                )

                (parentFragment as MainFragment).showHomeFragment()
            }
        })
        recycler_view_search_history.layoutManager = layoutManager
        recycler_view_search_history.itemAnimator = DefaultItemAnimator()
        recycler_view_search_history.addItemDecoration(
            DividerItemDecoration(
                context,
                layoutManager.orientation
            ).apply {
                setDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.item_divider_silver_horizontal
                    )!!
                )
            }
        )
        recycler_view_search_history.adapter = searchResultAdapter
    }

    companion object {
        @JvmStatic
        fun newInstance() = HistoryFragment()
    }
}