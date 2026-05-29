package edu.guigu.accountbook.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import edu.guigu.accountbook.data.model.Record
import edu.guigu.accountbook.databinding.FragmentStatisticsBinding
import edu.guigu.accountbook.ui.viewmodel.RecordViewModel
import edu.guigu.accountbook.util.DateUtils

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecordViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[RecordViewModel::class.java]

        setupCharts()
        observeData()
    }

    private fun setupCharts() {
        // 1. 饼图基础设置
        binding.pieChart.apply {
            description.isEnabled = false
            holeRadius = 45f
            transparentCircleRadius = 50f
            legend.isEnabled = true
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
        }

        // 2. 折线图基础设置
        binding.lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setScaleEnabled(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            axisRight.isEnabled = false
        }
    }

    private fun observeData() {
        // 总收入
        viewModel.totalIncome.observe(viewLifecycleOwner) {
            binding.tvIncome.text = "¥${DateUtils.formatAmount(it)}"
            updateBalance()
        }
        // 总支出
        viewModel.totalExpense.observe(viewLifecycleOwner) {
            binding.tvExpense.text = "¥${DateUtils.formatAmount(it)}"
            updateBalance()
        }
        // 饼图数据
        viewModel.expenseCategorySummary.observe(viewLifecycleOwner) { summaryList ->
            val entries = summaryList.map { PieEntry(it.total.toFloat(), it.category) }
            val dataSet = PieDataSet(entries, "支出分类").apply {
                colors = summaryList.map { Record.getCategoryColor(it.category) }
                valueTextSize = 12f
                valueTextColor = Color.WHITE
            }
            binding.pieChart.data = PieData(dataSet)
            binding.pieChart.invalidate()
        }
        // 折线图数据
        viewModel.monthlyTrend.observe(viewLifecycleOwner) { trendList ->
            val incomeEntries = trendList.mapIndexed { i, t -> Entry(i.toFloat(), t.income.toFloat()) }
            val expenseEntries = trendList.mapIndexed { i, t -> Entry(i.toFloat(), t.expense.toFloat()) }

            val incomeSet = LineDataSet(incomeEntries, "收入").apply {
                color = Color.parseColor("#2ECC71")
                setCircleColor(Color.parseColor("#2ECC71"))
                lineWidth = 2f
            }
            val expenseSet = LineDataSet(expenseEntries, "支出").apply {
                color = Color.parseColor("#E74C3C")
                setCircleColor(Color.parseColor("#E74C3C"))
                lineWidth = 2f
            }

            binding.lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(trendList.map { it.month })
            binding.lineChart.data = LineData(incomeSet, expenseSet)
            binding.lineChart.invalidate()
        }
    }

    private fun updateBalance() {
        val income = viewModel.totalIncome.value ?: 0.0
        val expense = viewModel.totalExpense.value ?: 0.0
        binding.tvBalance.text = "¥${DateUtils.formatAmount(income - expense)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
