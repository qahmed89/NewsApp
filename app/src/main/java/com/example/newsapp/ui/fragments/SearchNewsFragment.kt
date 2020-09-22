package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.model.news.NewsResponse
import com.example.newsapp.ui.NewActivity
import com.example.newsapp.util.Constants
import com.example.newsapp.util.Constants.Companion.SEARCH_TIME_DELAY
import com.example.newsapp.util.Resource
import com.example.newsapp.viewmodel.NewsViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_breaking_news.*
import kotlinx.android.synthetic.main.fragment_search_news.*
import kotlinx.android.synthetic.main.fragment_search_news.paginationProgressBar
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchNewsFragment : Fragment(R.layout.fragment_search_news) {
    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    val TAG = "SearchNewsFragment"
    var x = ""


    // TODO: Rename and change types of parameters
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewActivity).viewModel
        setupRecyclerView()
        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)

            }
            findNavController().navigate(
                R.id.action_searchNewsFragment2_to_articleFragment,
                bundle
            )
        }
        var job: Job? = null
        etSearch.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_TIME_DELAY)
                editable?.let {
                    if (editable.toString().isNotEmpty() ) {
                        viewModel.searchNews(editable.toString())
                      x=editable.toString()
                    }
                    if (editable.toString().isEmpty()){

                        viewModel.clearsearch()

                    }
                }
            }

        }

        viewModel.searchNews.observe(viewLifecycleOwner, Observer { response ->

            showProgressbar()
            when (response) {
                is Resource.Success -> {
                    hideProgressbar()
                    response.data?.let { newsResponse ->

                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / Constants.QUERY_Page_SIZE + 2
                        isLastPage = viewModel.searchnewspage == totalPages
                       if(isLastPage){
                           rvSearchNews.setPadding(0,0,0,0)

                       }



                    }
                }
                is Resource.Error -> {
                    hideProgressbar()
                    response.message?.let {
                        if(it=="No Internet Connection"){
                            Snackbar.make(view, "No Internet Connection", Snackbar.LENGTH_INDEFINITE).apply {
                                setAction("Refresh") {
                                    viewModel.searchNews(etSearch.toString())
                                }
                                show()
                            }
                        }else {
                            Toast.makeText(view.context, it, Toast.LENGTH_LONG).show()
                            Log.d(TAG, it)
                        }


                    }
                }
                is Resource.Loading -> {
                    showProgressbar()
                }
            }
        })

    }


    private fun hideProgressbar() {
        paginationProgressBar.visibility = View.INVISIBLE
        isLoading=false
    }

    private fun showProgressbar() {
        paginationProgressBar.visibility = View.VISIBLE
        isLoading=true
    }
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_Page_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling
            if(shouldPaginate) {
                viewModel.searchNews(etSearch.text.toString())
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }


    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        rvSearchNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchNewsFragment.scrollListener)
        }
    }}