package com.kg.todo.tasklist

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kg.todo.data.models.TasksListEntry
import com.kg.todo.data.remote.request.InsertTask
import com.kg.todo.data.remote.request.UpdateTask
import com.kg.todo.data.remote.response.TasksItem
import com.kg.todo.repository.TasksRepo
import com.kg.todo.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val repository: TasksRepo
): ViewModel(){

    val taskTextField = mutableStateOf<String>("")
    private val _tasksList = MutableSharedFlow<List<TasksListEntry>>()

    val tasksList = _tasksList.asSharedFlow()


    val isRefreshing = mutableStateOf(false)

    init {
        loadTasks()
    }

    fun loadTasks(){
        viewModelScope.launch {
            isRefreshing.value = true
            val result = repository.getTodos()
            Log.d("API",result.toString())
            when(result) {
                is Resource.Success ->{
                    val tasksFinal = result.data?.mapIndexed { index: Int, entry: TasksItem ->
                        TasksListEntry(entry._id, entry.task)
                    }
                    Log.d("taskList", tasksFinal.toString())
                    if(tasksFinal != null){
                        _tasksList.emit(tasksFinal)
                    }
                }
            }
            isRefreshing.value = false
        }
    }

    fun addTask(){
        if(taskTextField.value.isNotEmpty()) {
            viewModelScope.launch {
                isRefreshing.value = true
                val result = repository.addTodo(InsertTask(task = taskTextField.value))
                Log.d("API",result.toString())
                when(result) {
                    is Resource.Success ->{
                        val tasksFinal = result.data?.mapIndexed { index: Int, entry: TasksItem ->
                            TasksListEntry(entry._id, entry.task)
                        }
                        if(tasksFinal != null){
                            _tasksList.emit(tasksFinal)
                        }
                    }
                }
                taskTextField.value = ""
                isRefreshing.value = false
            }
        }
    }

    fun editTask(_id: String, taskParam: String){
        viewModelScope.launch {
            isRefreshing.value = true
            val result = repository.updateTodo(_id, UpdateTask(taskParam))
            Log.d("API",result.toString())
            when(result) {
                is Resource.Success ->{
                    val tasksFinal = result.data?.mapIndexed { index: Int, entry: TasksItem ->
                        TasksListEntry(entry._id, entry.task)
                    }
                    if(tasksFinal != null){
                        _tasksList.emit(tasksFinal)
                    }
                }
            }
            isRefreshing.value = false
        }
    }

    fun deleteTask(taskId: String, index: Int){
        viewModelScope.launch {
            isRefreshing.value = true
            val result = repository.deleteTodo(taskId)
            Log.d("API", result.toString())
            when (result) {
                is Resource.Success -> {
                    val tasksFinal = result.data?.mapIndexed { index: Int, entry: TasksItem ->
                        TasksListEntry(entry._id, entry.task)
                    }
                    Log.d("taskList", tasksFinal.toString())
                    if(tasksFinal != null){
                        _tasksList.emit(tasksFinal)
                    }
                }
            }
            isRefreshing.value = false
        }
    }

}