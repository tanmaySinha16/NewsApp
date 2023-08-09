package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.ui.ViewModel.NewsViewModel
import com.example.newsapp.ui.activity.NewsActivity
import com.example.newsapp.ui.adapters.NewsAdapter
import com.example.newsapp.ui.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchNewsFragment:Fragment(R.layout.fragment_search_news) {
    private val viewModel:NewsViewModel by viewModels()
    lateinit var newsAdapter: NewsAdapter
    val TAG = "SearchNewsFragment"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article",it)
            }
            findNavController().navigate(
                R.id.action_searchNewsFragment_to_articleFragment,bundle
            )
        }

        requireView().findViewById<Button>(R.id.btnSearch).setOnClickListener {
            viewModel.searchNews(requireView().findViewById<EditText>(R.id.etSearch).text.toString())
        }

        viewModel.searchNews.observe(viewLifecycleOwner, Observer { response ->
            when(response)
            {
                is Resource.Success ->
                {
                    hideProgressBar()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / 20 + 2
                        isLastPage = viewModel.searchNewsPage ==totalPages
                       t
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let {
                        Toast.makeText(activity,"An error occured $it", Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Loading ->{
                    showProgressBar()
                }
            }
        })

    }
    private fun hideProgressBar()
    {
        requireView().findViewById<ProgressBar>(R.id.paginationProgressBar).visibility = View.INVISIBLE
        isLoading= false
    }
    private fun showProgressBar()
    {
        requireView().findViewById<ProgressBar>(R.id.paginationProgressBar).visibility = View.VISIBLE
        isLoading= true
    }
    private fun setupRecyclerView()
    {
        newsAdapter = NewsAdapter()
        requireView().findViewById<RecyclerView>(R.id.rvSearchNews).apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchNewsFragment.scrollListener)
        }
    }
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    val scrollListener = object : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading  && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >=0
            val isTotalMoreThanVisible = totalItemCount >= 20
            val  shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning
                    && isTotalMoreThanVisible && isScrolling
            if(shouldPaginate)
            {
                viewModel.searchNews(requireView().findViewById<EditText>(R.id.etSearch).text.toString())
                isScrolling=false
            }
            else {
                requireView().findViewById<RecyclerView>(R.id.rvSearchNews).setPadding(0, 0, 0, 0)
            }

        }
    }
}