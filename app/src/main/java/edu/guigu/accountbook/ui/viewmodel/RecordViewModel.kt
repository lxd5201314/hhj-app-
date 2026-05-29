package edu.guigu.accountbook.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import edu.guigu.accountbook.data.dao.CategorySummary
import edu.guigu.accountbook.data.dao.MonthlyTrend
import edu.guigu.accountbook.data.database.AppDatabase
import edu.guigu.accountbook.data.model.Record
import edu.guigu.accountbook.data.repository.RecordRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class RecordViewModel(application: Application) : AndroidViewModel(application) {

    // 数据层对象
    private val repository: RecordRepository

    // 内部可修改，外部只读
    private val _allRecords = MutableLiveData<List<Record>>(emptyList())
    val allRecords: LiveData<List<Record>> get() = _allRecords

    private val _totalIncome = MutableLiveData(0.0)
    val totalIncome: LiveData<Double> get() = _totalIncome

    private val _totalExpense = MutableLiveData(0.0)
    val totalExpense: LiveData<Double> get() = _totalExpense

    // 支出分类汇总（给饼图用）
    private val _expenseCategorySummary = MutableLiveData<List<CategorySummary>>(emptyList())
    val expenseCategorySummary: LiveData<List<CategorySummary>> get() = _expenseCategorySummary

    // 月度趋势（给折线图用）
    private val _monthlyTrend = MutableLiveData<List<MonthlyTrend>>(emptyList())
    val monthlyTrend: LiveData<List<MonthlyTrend>> get() = _monthlyTrend

    init {
        val dao = AppDatabase.getInstance(application).recordDao()
        repository = RecordRepository(dao)
        refreshData()
    }

    // 获取数据业务
    fun refreshData() {
        // 启动协程
        viewModelScope.launch {
            // 到数据层获取数据，转为状态数据LiveData
            _allRecords.postValue(repository.getAllRecords())
            _totalIncome.postValue(repository.getTotalIncome() ?: 0.0)
            _totalExpense.postValue(repository.getTotalExpense() ?: 0.0)
            _expenseCategorySummary.postValue(repository.getCategorySummary(Record.TYPE_EXPENSE))
            _monthlyTrend.postValue(repository.getMonthlyTrend())
        }
    }

    // 搜索
    fun search(query: String) {
        viewModelScope.launch {
            _allRecords.postValue(repository.searchRecords(query))
        }
    }

    // 按月筛选
    fun filterByMonth(year: Int, month: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1, 0, 0, 0)
        val start = calendar.timeInMillis
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        val end = calendar.timeInMillis

        viewModelScope.launch {
            _allRecords.postValue(repository.getRecordsByDateRange(start, end))
        }
    }

    // 添加数据业务
    fun insert(record: Record) {
        viewModelScope.launch {
            repository.insert(record)
            refreshData()
        }
    }

    // 更新数据业务
    fun update(record: Record) {
        viewModelScope.launch {
            repository.update(record)
            refreshData()
        }
    }

    // 删除数据业务
    fun delete(record: Record) {
        viewModelScope.launch {
            repository.delete(record)
            refreshData()
        }
    }
}
