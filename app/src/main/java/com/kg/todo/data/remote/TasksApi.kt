package com.kg.todo.data.remote

import com.kg.todo.data.remote.request.InsertTask
import com.kg.todo.data.remote.request.UpdateTask
import com.kg.todo.data.remote.response.Tasks
import retrofit2.http.*

interface TasksApi {
    @GET("get")
    suspend fun getTasks(): Tasks

    @POST("add")
    suspend fun addTask(@Body insertTask: InsertTask): Tasks

    @PUT("update/{task_id}")
    suspend fun updateTask(@Path("task_id") taskId: String, @Body updateTask: UpdateTask): Tasks

    @DELETE("delete/{task_id}")
    suspend fun deleteTask(@Path("task_id") taskId: String): Tasks
}