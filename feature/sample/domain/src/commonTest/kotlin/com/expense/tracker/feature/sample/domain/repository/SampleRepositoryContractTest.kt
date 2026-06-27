package com.expense.tracker.feature.sample.domain.repository

import com.expense.tracker.feature.sample.domain.model.SampleItem
import com.expense.tracker.feature.sample.domain.model.SampleCategory
import com.expense.tracker.shared.core.domain.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SampleRepositoryContractTest {
    @Test
    fun loadItemsReturnsDomainItemsWrappedInResult() = runTest {
        val repository = object : SampleRepository {
            override suspend fun loadItems(): Result<List<SampleItem>> = Result.Success(
                listOf(
                    SampleItem(
                        id = "shared-architecture",
                        title = "Shared architecture",
                        description = "Keep feature logic in feature modules.",
                        category = SampleCategory.Architecture,
                        occurredAtMillis = 100L,
                        updatedAtMillis = 100L,
                    )
                )
            )

            override suspend fun loadItem(id: String): Result<SampleItem?> = Result.Success(null)

            override suspend fun createItem(
                title: String,
                description: String,
                category: SampleCategory,
            ): Result<SampleItem> = Result.Success(
                SampleItem("created", title, description, category, 200L, 200L)
            )

            override suspend fun updateItem(item: SampleItem): Result<SampleItem> = Result.Success(item)
        }

        val result = assertIs<Result.Success<List<SampleItem>>>(repository.loadItems())
        assertEquals("Shared architecture", result.value.single().title)
    }

    @Test
    fun createAndUpdateReturnDomainItemsWrappedInResult() = runTest {
        val repository = object : SampleRepository {
            private var item = SampleItem(
                id = "created",
                title = "Original",
                description = "Original description",
                category = SampleCategory.Contract,
                occurredAtMillis = 200L,
                updatedAtMillis = 200L,
            )

            override suspend fun loadItems(): Result<List<SampleItem>> = Result.Success(listOf(item))
            override suspend fun loadItem(id: String): Result<SampleItem?> = Result.Success(item.takeIf { it.id == id })

            override suspend fun createItem(
                title: String,
                description: String,
                category: SampleCategory,
            ): Result<SampleItem> {
                item = SampleItem("created", title, description, category, 300L, 300L)
                return Result.Success(item)
            }

            override suspend fun updateItem(item: SampleItem): Result<SampleItem> {
                this.item = item
                return Result.Success(item)
            }
        }

        val created = assertIs<Result.Success<SampleItem>>(
            repository.createItem("New", "New description", SampleCategory.Preview)
        ).value
        val updated = assertIs<Result.Success<SampleItem>>(
            repository.updateItem(created.update("Updated", "Updated description", SampleCategory.Architecture, 400L))
        ).value

        assertEquals("Updated", updated.title)
        assertEquals(SampleCategory.Architecture, updated.category)
    }
}
