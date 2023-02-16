package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.accessibility.AccessibilityEventCompat.setAction

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.ui.ViewModel.NewsViewModel
import com.example.newsapp.ui.activity.NewsActivity
import com.example.newsapp.ui.adapters.NewsAdapter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SavedNewsFragment:Fragment(R.layout.fragment_saved_news) {
    private val viewModel:NewsViewModel by viewModels()
    lateinit var newsAdapter:NewsAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article",it)
            }
            findNavController().navigate(
                R.id.action_savedNewsFragment_to_articleFragment,bundle
            )
        }

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val article = newsAdapter.differ.currentList[position]
                viewModel.deleteArticle(article)

                Snackbar.make(view,"Successfully deleted article",Snackbar.LENGTH_LONG).apply {
                   setAction("Undo")
                   {
                       viewModel.savedArticle(article)
                   }
                   show()
                }
            }

        }

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(requireView().findViewById(R.id.rvSavedNews))
        }

        viewModel.getSavedNews().observe(viewLifecycleOwner, Observer { articles ->
            newsAdapter.differ.submitList(articles)
        })
    }
    private fun setupRecyclerView()
    {
        newsAdapter = NewsAdapter()
        requireView().findViewById<RecyclerView>(R.id.rvSavedNews).apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }
}