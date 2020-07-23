package com.tt.weatherapp.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.LanguageCode
import com.here.sdk.core.errors.InstantiationErrorException
import com.here.sdk.search.*
import com.jakewharton.rxbinding4.widget.textChanges
import com.tt.weatherapp.R
import com.tt.weatherapp.adapter.SearchResultAdapter
import com.tt.weatherapp.model.PlaceEntity
import com.tt.weatherapp.viewmodel.PlaceViewModel
import com.tt.weatherapp.viewmodel.WeatherViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_search_place.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*
import java.util.concurrent.TimeUnit

class SearchPlaceFragment : Fragment() {

    private val sharedPref: SharedPreferences by inject()
    private val weatherViewModel: WeatherViewModel by sharedViewModel(from = { requireActivity() })
    private val placeViewModel: PlaceViewModel by sharedViewModel(from = { requireActivity() })
    private var searchEngine: SearchEngine? = null
    private val maxItems = 5
    private val searchOptions = SearchOptions(LanguageCode.EN_US, maxItems)
    private val listPlaceResult: MutableList<PlaceEntity> = ArrayList()
    private val listPlaceHistory: MutableList<PlaceEntity> = ArrayList()
    private lateinit var searchResultAdapter: SearchResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            searchEngine = SearchEngine()
        } catch (e: InstantiationErrorException) {
            throw RuntimeException("Initialization of SearchEngine failed: " + e.error.name)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_place, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch(Dispatchers.Main) {
            delay(77)
            showSoftKeyboard(edit_text_search_place)
        }

        generateSearchResultRecyclerView(requireContext())

        placeViewModel.getAllPlaceHistory()
        placeViewModel.listPlaceHistoryLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer {
                listPlaceResult.clear()
                listPlaceResult.addAll(it)
                searchResultAdapter.notifyDataSetChanged()
                listPlaceHistory.clear()
                listPlaceHistory.addAll(it)
            })

        listenToEditTextSearch()

        clear_button.setOnClickListener {
            clearEditText()
        }

        backButton.setOnClickListener {
            closeSoftKeyboard(edit_text_search_place)
            parentFragmentManager.popBackStack()
        }
    }

    private fun listenToEditTextSearch() {
        edit_text_search_place.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) clear_button.visibility = View.GONE
                else clear_button.visibility = View.VISIBLE
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        edit_text_search_place.textChanges()
            .debounce(177, TimeUnit.MILLISECONDS)
            .map { charSequence ->
                charSequence.toString()
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { string ->
                if (string.isNullOrEmpty()) {
                    listPlaceResult.clear()
                    placeViewModel.listPlaceHistoryLiveData.value?.let {
                        listPlaceResult.addAll(it)
                    }
                    searchResultAdapter.notifyDataSetChanged()
                } else {
                    val centerGeoCoordinates = GeoCoordinates(
                        weatherViewModel.latLiveData.value ?: 90.0,
                        weatherViewModel.lonLiveData.value ?: 180.0
                    )
                    searchEngine?.suggest(
                        TextQuery(
                            string.toString(), centerGeoCoordinates
                        ), searchOptions, object : SuggestCallback {
                            override fun onSuggestCompleted(
                                searchError: SearchError?,
                                list: MutableList<Suggestion>?
                            ) {
                                if (searchError != null) {
                                    Log.d(
                                        "HereMapInfo",
                                        "AutoSuggest Error: " + searchError.name
                                    )
                                    listPlaceResult.clear()
                                    searchResultAdapter.notifyDataSetChanged()
                                    return
                                }

                                if (list != null) {
                                    Log.d(
                                        "HereMapInfo",
                                        "AutoSuggest results: " + list.size
                                    )
                                    val now =
                                        System.currentTimeMillis() / 1000
                                    listPlaceResult.clear()
                                    for (autoSuggestResult in list) {
                                        autoSuggestResult.let { suggestion ->
                                            suggestion.place?.let { place ->
                                                listPlaceResult.add(
                                                    PlaceEntity(
                                                        suggestion.title,
                                                        place.address.addressText,
                                                        place.coordinates.latitude,
                                                        place.coordinates.longitude,
                                                        now
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    searchResultAdapter.notifyDataSetChanged()
                                }
                            }
                        })
                }
            }
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

                closeSoftKeyboard(edit_text_search_place)

                val random = Random().nextInt(11)
                if (random > 0 && random % 2 == 0 || listPlaceHistory.isEmpty()) {
                    RatingDialogFragment.newInstance().also {
                        it.show(
                            (activity as MainActivity).supportFragmentManager,
                            "rating_fragment"
                        )
                    }
                }

                placeViewModel.insertPlace(place)
                parentFragmentManager.popBackStack()
            }
        })
        recycler_view_search_result.layoutManager = layoutManager
        recycler_view_search_result.itemAnimator = DefaultItemAnimator()
        recycler_view_search_result.addItemDecoration(
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
        recycler_view_search_result.adapter = searchResultAdapter
    }

    private fun clearEditText() {
        edit_text_search_place.text = null
    }

    private fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun closeSoftKeyboard(editTextSearchPlace: EditText) {
        if (editTextSearchPlace.requestFocus()) {
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(
                editTextSearchPlace.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SearchPlaceFragment()
    }
}