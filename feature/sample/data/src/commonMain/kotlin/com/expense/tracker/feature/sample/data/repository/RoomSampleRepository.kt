package com.expense.tracker.feature.sample.data.repository

import com.expense.tracker.feature.sample.data.mapper.toDomain
import com.expense.tracker.feature.sample.data.mapper.toEntity
import com.expense.tracker.feature.sample.domain.model.SampleCategory
import com.expense.tracker.feature.sample.domain.model.SampleItem
import com.expense.tracker.feature.sample.domain.repository.SampleRepository
import com.expense.tracker.shared.core.data.local.SampleItemDao
import com.expense.tracker.shared.core.data.local.SampleItemEntity
import com.expense.tracker.shared.core.domain.AppError
import com.expense.tracker.shared.core.domain.Result
import com.expense.tracker.shared.core.domain.TimeProvider
import com.expense.tracker.shared.core.domain.runSuspendCatching

class RoomSampleRepository(
    private val dao: SampleItemDao,
    private val timeProvider: TimeProvider,
) : SampleRepository {
    override suspend fun loadItems(): Result<List<SampleItem>> = runSuspendCatching(
        block = {
            seedIfNeeded()
            dao.getAll().map { entity -> entity.toDomain() }
        },
        onFailure = { AppError.Unknown }
    )

    override suspend fun loadItem(id: String): Result<SampleItem?> = runSuspendCatching(
        block = {
            seedIfNeeded()
            dao.getById(id)?.toDomain()
        },
        onFailure = { AppError.Unknown }
    )

    override suspend fun createItem(
        title: String,
        description: String,
        category: SampleCategory,
    ): Result<SampleItem> = runSuspendCatching(
        block = {
            seedIfNeeded()
            val now = timeProvider.nowMillis()
            val item = SampleItem(
                id = "sample-$now",
                title = title.trim(),
                description = description.trim(),
                category = category,
                occurredAtMillis = now,
                updatedAtMillis = now,
            )
            dao.insert(item.toEntity(position = dao.count()))
            item
        },
        onFailure = { AppError.Unknown }
    )

    override suspend fun updateItem(item: SampleItem): Result<SampleItem> = runSuspendCatching(
        block = {
            val now = timeProvider.nowMillis()
            val existing = dao.getById(item.id)
            val updated = item.copy(updatedAtMillis = now)
            dao.update(updated.toEntity(position = existing?.position ?: dao.count()))
            updated
        },
        onFailure = { AppError.Unknown }
    )

    private suspend fun seedIfNeeded() {
        if (dao.count() == 0) {
            dao.insertAll(defaultSampleEntities())
        }
    }

    private fun defaultSampleEntities(): List<SampleItemEntity> = listOf(
        SampleItemEntity(
            id = "contract-first",
            title = "Contract-first features",
            description = "Generate features from compact contracts stored in .features/.",
            category = SampleCategory.Contract.name,
            occurredAtMillis = 1_720_000_000_000,
            updatedAtMillis = 1_720_000_000_000,
            position = 0,
        ),
        SampleItemEntity(
            id = "shared-architecture",
            title = "Shared architecture",
            description = "Keep domain, data, presentation, and UI code in stable modules.",
            category = SampleCategory.Architecture.name,
            occurredAtMillis = 1_720_086_400_000,
            updatedAtMillis = 1_720_086_400_000,
            position = 1,
        ),
        SampleItemEntity(
            id = "preview-workflow",
            title = "Preview workflow",
            description = "Use fast Android and iOS runs as the main preview and repair surface.",
            category = SampleCategory.Preview.name,
            occurredAtMillis = 1_720_172_800_000,
            updatedAtMillis = 1_720_172_800_000,
            position = 2,
        )
    )
}
