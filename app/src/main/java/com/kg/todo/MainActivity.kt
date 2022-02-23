package com.kg.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.kg.todo.tasklist.TaskListViewModel
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.kg.todo.data.models.TasksListEntry
import com.kg.todo.ui.theme.DarkBackground
import com.kg.todo.ui.theme.ItemBackground
import com.kg.todo.ui.theme.PrimaryColor
import com.kg.todo.ui.theme.PrimaryLightColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val scaffoldState = rememberScaffoldState()
                val TaskListViewModelFinal: TaskListViewModel by viewModels()

                val isRefreshing by remember { TaskListViewModelFinal.isRefreshing }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize(), scaffoldState = scaffoldState
                ) {
                    SwipeRefresh(
                        state = rememberSwipeRefreshState(false),
                        onRefresh = { TaskListViewModelFinal.loadTasks() },
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(DarkBackground)
                                .padding(horizontal = 18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CustomTextField(
                                    modifier = Modifier
                                        .weight(2f)
                                        .padding(top = 18.dp, bottom = 12.dp)
                                        .background(DarkBackground),
                                    TaskListViewModelFinal
                                )
                            }
                            TasksListComposable(TaskListViewModelFinal)
                        }
                        if (isRefreshing) {
                            Box(
                                modifier = Modifier
                                    .background(DarkBackground.copy(alpha = 0.6f))
                                    .fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(color = PrimaryColor)
                            }
                        }
                    }
                }
            }
        }

    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun CustomTextField(
        modifier: Modifier,
        viewModel: TaskListViewModel
    ) {

    val taskText = remember{ viewModel.taskTextField }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current


    TextField(
            value = taskText.value,
            label = {
                Text(
                    text = "Enter Task",
                    color = PrimaryColor
                )
                    },
            onValueChange = { textThis -> taskText.value = textThis },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if(taskText.value.isNotEmpty()) {
                        viewModel.addTask()
                    }
                    focusManager.clearFocus()
                    keyboardController?.hide()
            }),
            singleLine = true,
            modifier = modifier,
            trailingIcon = {
                IconButton(onClick = {
                    if(taskText.value.isNotEmpty()) {
                        viewModel.addTask()
                }
                }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "addIcon",
                            tint = PrimaryColor
                        )
                    }
            },
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = PrimaryColor,
                unfocusedIndicatorColor = PrimaryLightColor,
            ),
            textStyle = TextStyle(color = Color.White)
        )
    }

    @ExperimentalComposeUiApi
    @Composable
    fun TasksListComposable(
        viewModel: TaskListViewModel) {

        fun Modifier.clearFocusOnKeyboardDismiss(): Modifier = composed {
            var isFocused by remember { mutableStateOf(false) }
            var keyboardAppearedSinceLastFocused by remember { mutableStateOf(false) }
            if (isFocused) {
                val imeIsVisible = LocalWindowInsets.current.ime.isVisible
                val focusManager = LocalFocusManager.current
                LaunchedEffect(imeIsVisible) {
                    if (imeIsVisible) {
                        keyboardAppearedSinceLastFocused = true
                    } else if (keyboardAppearedSinceLastFocused) {
                        focusManager.clearFocus()
                    }
                }
            }
            onFocusEvent {
                if (isFocused != it.isFocused) {
                    isFocused = it.isFocused
                    if (isFocused) {
                        keyboardAppearedSinceLastFocused = false
                    }
                }
            }
        }

        val isRefreshing by remember { viewModel.isRefreshing }

        val tasksList by viewModel.tasksList.collectAsState(initial = listOf<TasksListEntry>())


        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current

            if (!tasksList.isNullOrEmpty()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    LazyColumn {
                        itemsIndexed(tasksList) { index, item ->
                            val checkedState = mutableStateOf(false)
                            var textFieldState by remember { mutableStateOf<String>(item.task) }
                            var isEditing by remember { mutableStateOf<Boolean>(false) }
                            val focusRequester = remember { FocusRequester() }

                            DisposableEffect(key1 = item){
                                textFieldState = item.task
                                onDispose {  }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .background(ItemBackground)
                                    .padding(vertical = 12.dp)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onDoubleTap = {
                                                isEditing = true
                                            }
                                        )
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                DisposableEffect(isEditing) {
                                    if (isEditing) {
                                        focusRequester.requestFocus()
                                    } else {
                                        focusRequester.freeFocus()
                                        textFieldState = item.task
                                    }
                                    onDispose {}
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                BasicTextField(
                                    value = textFieldState,
                                    onValueChange = { textFieldState = it },
                                    enabled = isEditing,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            if(textFieldState.isNotEmpty()) {
                                                viewModel.editTask(item._id, textFieldState)
                                                isEditing = false
                                            }
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                        }),
                                    modifier = Modifier
                                        .clearFocusOnKeyboardDismiss()
                                        .weight(0.8f)
                                        .padding(start = 4.dp, end = 8.dp)
                                        .focusRequester(focusRequester)
                                        .onFocusChanged { state ->
                                            if (!state.isFocused) {
                                                isEditing = false
                                            }
                                        },
                                    textStyle = TextStyle(fontSize = 16.sp, color = Color.White)
                                )
                                if (isEditing) {
                                    IconButton(
                                        onClick = {
                                            if(textFieldState.isNotEmpty()) {
                                                viewModel.editTask(item._id, textFieldState)
                                                isEditing = false
                                            }
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                        },
                                        modifier = Modifier.weight(0.2f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "EditIcon",
                                            tint = PrimaryColor
                                        )
                                    }
                                } else {
                                    Box(modifier = Modifier.weight(0.2f),
                                        contentAlignment = Alignment.Center) {
                                        Checkbox(
                                            checked = checkedState.value,
                                            modifier = Modifier,
                                            onCheckedChange = {
                                                checkedState.value = it
                                                if(it) viewModel.deleteTask(item._id, index)
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = PrimaryColor,
                                                checkmarkColor = Color.White,
                                                uncheckedColor = PrimaryColor,
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if(!isRefreshing){
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkBackground)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No Tasks...",
                            textAlign = TextAlign.Center,
                            fontSize = 32.sp,
                            color = PrimaryColor
                        )
                    }
                }
            }
    }
}

