package com.expense.tracker.shared.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class MviViewModel<State : Any, Action : Any, Event : Any>(
    initialState: State,
) : ViewModel() {
    private val _state = MutableStateFlow(initialState)
    private val _events = MutableSharedFlow<Event>()
    private val actions = Channel<Action>(capacity = Channel.UNLIMITED)

    val state: StateFlow<State> = _state.asStateFlow()
    val eventFlow: SharedFlow<Event> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            actions.receiveAsFlow().collect { action ->
                handleAction(action)
            }
        }
    }

    fun onAction(action: Action) {
        actions.trySend(action)
    }

    protected fun updateState(transform: (State) -> State) {
        _state.update(transform)
    }

    protected suspend fun sendEvent(event: Event) {
        _events.emit(event)
    }

    override fun onCleared() {
        actions.close()
        super.onCleared()
    }

    protected abstract suspend fun handleAction(action: Action)
}
