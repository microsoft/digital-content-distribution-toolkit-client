package com.msr.bine_android.data.entities

import androidx.lifecycle.LiveData
import androidx.room.*
import org.jetbrains.annotations.NotNull

@Dao
interface CartEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg cartEntity: CartEntity)

    @Transaction
    @Query("SELECT * FROM Cart")
    fun getCartItems(): LiveData<List<CartEntity>>

    @Transaction
    @Query("SELECT * FROM Cart WHERE id =:contentID")
    fun getCartItem(contentID: String): LiveData<CartEntity>

    @Transaction
    @Delete
    suspend fun deleteItem(cartEntity: CartEntity)

}