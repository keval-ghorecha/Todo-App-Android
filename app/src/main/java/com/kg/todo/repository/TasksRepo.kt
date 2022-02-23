package com.kg.todo.repository

import com.kg.todo.data.remote.TasksApi
import com.kg.todo.data.remote.request.InsertTask
import com.kg.todo.data.remote.request.UpdateTask
import com.kg.todo.data.remote.response.Tasks
import com.kg.todo.util.Resource
import dagger.hilt.android.scopes.ActivityScoped
//import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class TasksRepo @Inject constructor(
    private val api: TasksApi
){
    suspend fun getTodos(): Resource<Tasks> {
        val response = try{
            api.getTasks()
        }catch (e:Exception){
            return Resource.Error(message = e.message)
        }
        return Resource.Success(response)
    }

    suspend fun addTodo(insertTask: InsertTask): Resource<Tasks> {
        val response = try{
            api.addTask(insertTask)
        }catch (e:Exception){
            return Resource.Error(message = e.message)
        }
        return Resource.Success(response)
    }

    suspend fun updateTodo(taskId:String, updateTask: UpdateTask): Resource<Tasks> {
        val response = try{
            api.updateTask(taskId, updateTask)
        }catch (e:Exception){
            return Resource.Error(message = e.message)
        }
        return Resource.Success(response)
    }

    suspend fun deleteTodo(taskId: String): Resource<Tasks> {
        val response = try{
            api.deleteTask(taskId)
        }catch (e:Exception){
            return Resource.Error(message = e.message)
        }
        return Resource.Success(response)
    }
}