package com.example.newsapp.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.newsapp.model.news.Article
import retrofit2.http.DELETE
@Dao
interface ArticleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(article: Article):Long
    @Query("SELECT * FROM articles")
     fun getAllArticles():LiveData <List<Article>>
    @Delete
    suspend fun deleteArticle(article: Article)
}