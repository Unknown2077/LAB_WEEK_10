package com.example.lab_week_10.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TotalViewModel : ViewModel() {
    private val _total = MutableLiveData(0)
    val total: LiveData<Int> get() = _total

    fun incrementTotal() {
        _total.value = (_total.value ?: 0) + 1
    }
}