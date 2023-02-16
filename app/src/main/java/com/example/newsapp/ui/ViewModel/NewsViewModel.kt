package com.example.newsapp.ui.ViewModel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.TYPE_MOBILE
import android.net.ConnectivityManager.TYPE_WIFI
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.os.Build
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Query
import com.example.newsapp.ui.NewsApplication
import com.example.newsapp.ui.adapters.NewsAdapter
import com.example.newsapp.ui.models.Article
import com.example.newsapp.ui.models.NewsResponse
import com.example.newsapp.ui.repository.NewsRepository
import com.example.newsapp.ui.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException


class NewsViewModel @ViewModelInject constructor(
    val newsRepository: NewsRepository,
    application: Application
): AndroidViewModel(application) {

    val breakingNews:MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var breakingNewsPage = 1
    var breakingNewsResponse: NewsResponse?=null


    val searchNews:MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse?=null

    init {
        getBreakingNews("in")
    }

    fun getBreakingNews(countryCode:String) =
        viewModelScope.launch {
            safeBreakingNewsCall(countryCode)
        }


    fun searchNews(searchQuery: String)
    {
       viewModelScope.launch {
           safeSearchNewsCall(searchQuery)
       }
    }

    private fun handleBreakingNewsResponse(response: Response<NewsResponse>) : Resource<NewsResponse>
    {
        if(response.isSuccessful)
        {
            response.body()?.let{ resultResponse ->
                breakingNewsPage++
                if(breakingNewsResponse == null){
                    breakingNewsResponse=resultResponse
                }
                else{
                    val oldArticles = breakingNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(breakingNewsResponse?:resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>) : Resource<NewsResponse>
    {
        if(response.isSuccessful)
        {
            response.body()?.let{ resultResponse ->
                searchNewsPage++
                if(searchNewsResponse == null){
                    searchNewsResponse=resultResponse
                }
                else{
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse?:resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun savedArticle(article: Article){
        viewModelScope.launch {
            newsRepository.upsert(article)
        }
    }

    fun getSavedNews() = newsRepository.getSavedNews()

    fun deleteArticle(article:Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)

    }
    private suspend fun safeSearchNewsCall(searchQuery: String){
        breakingNews.postValue(Resource.Loading())
        try {
            if(hasInternetConnection()) {
                val response = newsRepository.searchNews(searchQuery, searchNewsPage)
                searchNews.postValue(handleSearchNewsResponse(response))
            }
            else{
                searchNews.postValue(Resource.Error("No internet connection"))
            }
        }catch (t:Throwable)
        {
            when(t){
                is IOException -> searchNews.postValue(Resource.Error("Network Failure"))
                else -> searchNews.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private suspend fun safeBreakingNewsCall(countryCode: String){
        breakingNews.postValue(Resource.Loading())
        try {
            if(hasInternetConnection()) {
                val response = newsRepository.getBreakingNews(countryCode, breakingNewsPage)
                breakingNews.postValue(handleBreakingNewsResponse(response))
            }
            else{
                breakingNews.postValue(Resource.Error("No internet connection"))
            }
        }catch (t:Throwable)
        {
            when(t){
                is IOException -> breakingNews.postValue(Resource.Error("Network Failure"))
                else -> breakingNews.postValue(Resource.Error("Conversion Error"))
            }
        }
    }


    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<NewsApplication>().getSystemService(
        Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            val activeNetwork = connectivityManager.activeNetwork?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?:return false
            return when{
                capabilities.hasTransport(TRANSPORT_WIFI) -> return true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> return true
                else -> false
            }
        }
        else{
            connectivityManager.activeNetworkInfo?.run {
                return when(type)
                {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    else -> false
                }
            }
        }
        return false
    }
}