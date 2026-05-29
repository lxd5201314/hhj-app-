package edu.guigu.accountbook.data.dao

import androidx.room.*
import edu.guigu.accountbook.data.model.Record

/** 分类汇总数据（用于饼图） */
data class CategorySummary(
    val category: String,
    val total: Double
)

/** 月度趋势数据（用于折线图） */
data class MonthlyTrend(
    val month: String,
    val income: Double,
    val expense: Double
)

@Dao
interface RecordDao {

    // ===== 查询 =====

    @Query("SELECT * FROM records ORDER BY date DESC")
    suspend fun getAllRecords(): List<Record>

    @Query("SELECT SUM(amount) FROM records WHERE type = ${Record.TYPE_INCOME}")
    suspend fun getTotalIncome(): Double?

    @Query("SELECT SUM(amount) FROM records WHERE type = ${Record.TYPE_EXPENSE}")
    suspend fun getTotalExpense(): Double?

    @Query("SELECT * FROM records WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getRecordsByDateRange(startDate: Long, endDate: Long): List<Record>

    @Query("SELECT category, SUM(amount) AS total FROM records WHERE type = :type GROUP BY category ORDER BY total DESC")
    suspend fun getCategorySummary(type: Int): List<CategorySummary>

    @Query("""
        SELECT 
            strftime('%Y-%m', date / 1000, 'unixepoch', 'localtime') AS month,
            SUM(CASE WHEN type = ${Record.TYPE_INCOME} THEN amount ELSE 0 END) AS income,
            SUM(CASE WHEN type = ${Record.TYPE_EXPENSE} THEN amount ELSE 0 END) AS expense
        FROM records 
        GROUP BY month 
        ORDER BY month ASC 
        LIMIT 6
    """)
    suspend fun getMonthlyTrend(): List<MonthlyTrend>

    @Query("SELECT * FROM records WHERE category LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%' ORDER BY date DESC")
    suspend fun searchRecords(query: String): List<Record>

    // ===== 增删改 =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: Record): Long

    @Update
    suspend fun update(record: Record)

    @Delete
    suspend fun delete(record: Record)

    @Query("DELETE FROM records WHERE id = :id")
    suspend fun deleteById(id: Long)
}
