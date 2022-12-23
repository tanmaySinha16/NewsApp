package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavArgs
import androidx.navigation.NavArgsLazy
import androidx.navigation.fragment.navArgs
import com.example.newsapp.R
import com.example.newsapp.ui.ViewModel.NewsViewModel
import com.example.newsapp.ui.activity.NewsActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class ArticleFragment:Fragment(R.layout.fragment_article) {
    lateinit var viewModel: NewsViewModel
    val args:ArticleFragmentArgs by navArgs()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel =(activity as NewsActivity).viewModel
        val article = args.article
        requireView().findViewById<WebView>(R.id.webView).apply {
            webViewClient = WebViewClient()
            article.url?.let { loadUrl(it) }
        }

        requireView().findViewById<FloatingActionButton>(R.id.fab).setOnClickListener{
            viewModel.savedArticle(article)
            Snackbar.make(view,"Article saved successfully",Snackbar.LENGTH_SHORT).show()

        }

    }


}