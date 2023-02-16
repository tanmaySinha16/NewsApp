package com.example.newsapp.ui.repository

import com.example.newsapp.ui.api.NewsApi
import com.example.newsapp.ui.db.ArticleDao
import com.example.newsapp.ui.models.Article
import javax.inject.Inject


class NewsRepository@Inject constructor(
    val articleDao:ArticleDao,
    val newsApi: NewsApi
)
{
    suspend fun getBreakingNews(countryCode:String,pageNumber:Int) =
       newsApi.getBreakingNews(countryCode,pageNumber)

    suspend fun searchNews(searchQuery:String,pageNumber: Int) =
        newsApi.searchForNews(searchQuery,pageNumber)

    suspend fun upsert(article:Article) = articleDao.upsert(article)

    fun getSavedNews()=articleDao.getAllArticles()

    suspend fun deleteArticle(article: Article) = articleDao.deleteArticle(article)

}