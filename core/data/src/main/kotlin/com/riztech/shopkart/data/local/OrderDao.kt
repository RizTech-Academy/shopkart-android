package com.riztech.shopkart.data.local

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.riztech.shopkart.data.local.entity.OrderEntity
import com.riztech.shopkart.data.local.entity.OrderLineEntity
import kotlinx.coroutines.flow.Flow

data class OrderWithLines(
    @Embedded val order: OrderEntity,
    @Relation(parentColumn = "reference", entityColumn = "orderReference")
    val lines: List<OrderLineEntity>,
)

@Dao
interface OrderDao {

    @Transaction
    @Query("SELECT * FROM orders ORDER BY placedAtEpochMillis DESC")
    fun observeOrders(): Flow<List<OrderWithLines>>

    @Insert
    suspend fun insertOrder(order: OrderEntity)

    @Insert
    suspend fun insertLines(lines: List<OrderLineEntity>)

    /**
     * An order and its lines are written in one transaction. A half-written
     * order - header with no lines - is worse than no order at all, because it
     * looks like a successful purchase of nothing.
     */
    @Transaction
    suspend fun insert(order: OrderEntity, lines: List<OrderLineEntity>) {
        insertOrder(order)
        insertLines(lines)
    }
}
