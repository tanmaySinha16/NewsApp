package com.example.newsapp.ui.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.newsapp.ui.ViewModel.NewsViewModel
import com.example.newsapp.ui.api.NewsApi
import com.example.newsapp.ui.db.ArticleDatabase
import com.example.newsapp.ui.repository.NewsRepository
import com.example.newsapp.ui.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun providesArticleDatabase(@ApplicationContext app: Context) = Room.databaseBuilder(
        app,
        ArticleDatabase::class.java,
        "article_db.db"
    ).build()

    @Singleton
    @Provides
    fun providesArticleDao(db:ArticleDatabase) = db.getArticleDao()

    @Singleton
    @Provides
    fun providesRetrofitInstance(): NewsApi {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        return retrofit.create(NewsApi::class.java)
    }


}