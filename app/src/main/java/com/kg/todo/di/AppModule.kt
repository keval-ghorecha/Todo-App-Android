package com.kg.todo.di

import com.kg.todo.data.remote.TasksApi
import com.kg.todo.repository.TasksRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideTasksRepository(
        api: TasksApi
    ) = TasksRepo(api)

    @Singleton
    @Provides
    fun provideTasksApi(): TasksApi {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://nodoappnodo.herokuapp.com/")
            .build()
            .create(TasksApi::class.java)
    }
}