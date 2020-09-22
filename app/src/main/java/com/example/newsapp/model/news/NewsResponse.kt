package com.example.newsapp.model.news

import com.example.newsapp.model.news.Article

data class NewsResponse(
    val articles: MutableList<Article>,
    val status: String,
    val totalResults: Int
)