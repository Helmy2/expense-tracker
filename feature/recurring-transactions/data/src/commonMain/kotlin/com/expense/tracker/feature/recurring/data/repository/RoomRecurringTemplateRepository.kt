package com.expense.tracker.feature.recurring.data.repository

import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.feature.recurring.data.mapper.toDomain
import com.expense.tracker.feature.recurring.data.mapper.toEntity
import com.expense.tracker.feature.recurring.domain.model.RecurringFrequency
import com.expense.tracker.feature.recurring.domain.model.RecurringTemplate
import com.expense.tracker.feature.recurring.domain.model.UpcomingRecurring
import com.expense.tracker.feature.recurring.domain.repository.RecurringTemplateRepository
import com.expense.tracker.shared.core.data.dao.RecurringTemplateDao
import com.expense.tracker.shared.core.data.dao.TransactionDao
import com.expense.tracker.shared.core.data.entity.TransactionEntity
import com.expense.tracker.shared.core.domain.AppError
import com.expense.tracker.shared.core.domain.Result
import com.expense.tracker.shared.core.domain.TimeProvider
import com.expense.tracker.shared.core.domain.runSuspendCatching
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class RoomRecurringTemplateRepository(
    private val templateDao: RecurringTemplateDao,
    private val transactionDao: TransactionDao,
    private val timeProvider: TimeProvider,
) : RecurringTemplateRepository {

    override suspend fun loadTemplates(): Result<List<RecurringTemplate>> = runSuspendCatching(
        block = { templateDao.getAll().map { it.toDomain() } },
        onFailure = { AppError.Unknown },
    )

    override suspend fun loadTemplateById(id: String): Result<RecurringTemplate?> = runSuspendCatching(
        block = { templateDao.getById(id)?.toDomain() },
        onFailure = { AppError.Unknown },
    )

    override suspend fun createTemplate(
        amount: Double,
        type: TransactionType,
        category: TransactionCategory,
        note: String,
        frequency: RecurringFrequency,
        startDateMillis: Long,
        endDateMillis: Long?,
    ): Result<RecurringTemplate> = runSuspendCatching(
        block = {
            val now = timeProvider.nowMillis()
            val template = RecurringTemplate(
                id = generateId(),
                amount = amount,
                type = type,
                category = category,
                note = note.trim(),
                frequency = frequency,
                startDateMillis = startDateMillis,
                endDateMillis = endDateMillis,
                isPaused = false,
                lastGeneratedDateMillis = null,
                createdAtMillis = now,
                updatedAtMillis = now,
            )
            templateDao.insert(template.toEntity())
            template
        },
        onFailure = { AppError.Unknown },
    )

    override suspend fun updateTemplate(
        id: String,
        amount: Double,
        type: TransactionType,
        category: TransactionCategory,
        note: String,
        frequency: RecurringFrequency,
        startDateMillis: Long,
        endDateMillis: Long?,
    ): Result<RecurringTemplate> = runSuspendCatching(
        block = {
            val existing = templateDao.getById(id)
                ?: throw IllegalArgumentException("RecurringTemplate not found: $id")
            val updated = existing.copy(
                amount = amount,
                type = type.name,
                category = category.name,
                note = note.trim(),
                frequency = frequency.name,
                startDateMillis = startDateMillis,
                endDateMillis = endDateMillis,
                updatedAtMillis = timeProvider.nowMillis(),
            )
            templateDao.insert(updated)
            updated.toDomain()
        },
        onFailure = { AppError.Unknown },
    )

    override suspend fun deleteTemplate(id: String): Result<Unit> = runSuspendCatching(
        block = { templateDao.deleteById(id) },
        onFailure = { AppError.Unknown },
    )

    override suspend fun togglePause(id: String): Result<RecurringTemplate> = runSuspendCatching(
        block = {
            val entity = templateDao.getById(id)
                ?: throw IllegalArgumentException("RecurringTemplate not found: $id")
            val newPaused = !entity.isPaused
            templateDao.updatePauseStatus(id, newPaused)
            entity.copy(isPaused = newPaused).toDomain()
        },
        onFailure = { AppError.Unknown },
    )

    override suspend fun processDueRecurring(): Result<Int> = runSuspendCatching(
        block = {
            val templates = templateDao.getUnpaused()
            val now = timeProvider.nowMillis()
            val zone = timeProvider.timeZone()
            var count = 0

            for (entity in templates) {
                val dueDates = computeDueDates(
                    startDateMillis = entity.startDateMillis,
                    endDateMillis = entity.endDateMillis,
                    frequency = RecurringFrequency.valueOf(entity.frequency),
                    lastGeneratedDateMillis = entity.lastGeneratedDateMillis,
                    nowMillis = now,
                    zone = zone,
                )

                for (dueDateMillis in dueDates) {
                    val transactionEntity = TransactionEntity(
                        id = "${generateId()}-recurring",
                        amount = entity.amount,
                        type = entity.type,
                        category = entity.category,
                        note = entity.note,
                        createdAtMillis = dueDateMillis,
                    )
                    transactionDao.insert(transactionEntity)
                    count++
                }

                if (dueDates.isNotEmpty()) {
                    templateDao.updateLastGenerated(id = entity.id, lastGeneratedDateMillis = dueDates.last())
                }
            }

            count
        },
        onFailure = { AppError.Unknown },
    )

    override suspend fun loadUpcoming(count: Int): Result<List<UpcomingRecurring>> = runSuspendCatching(
        block = {
            val templates = templateDao.getUnpaused()
            val now = timeProvider.nowMillis()
            val zone = timeProvider.timeZone()

            val upcoming = templates.mapNotNull { entity ->
                val frequency = RecurringFrequency.valueOf(entity.frequency)
                val nextDue = computeNextDueDate(
                    startDateMillis = entity.startDateMillis,
                    endDateMillis = entity.endDateMillis,
                    frequency = frequency,
                    afterMillis = now,
                    zone = zone,
                ) ?: return@mapNotNull null

                UpcomingRecurring(
                    templateId = entity.id,
                    amount = entity.amount,
                    type = TransactionType.valueOf(entity.type),
                    category = TransactionCategory.valueOf(entity.category),
                    note = entity.note,
                    frequency = frequency,
                    nextDueDateMillis = nextDue,
                )
            }

            upcoming
                .sortedBy { it.nextDueDateMillis }
                .take(count)
        },
        onFailure = { AppError.Unknown },
    )

    private fun generateId(): String = "${timeProvider.nowMillis()}-${kotlin.random.Random.nextLong()}"

    private fun computeDueDates(
        startDateMillis: Long,
        endDateMillis: Long?,
        frequency: RecurringFrequency,
        lastGeneratedDateMillis: Long?,
        nowMillis: Long,
        zone: TimeZone,
    ): List<Long> {
        val startDate = kotlin.time.Instant.fromEpochMilliseconds(startDateMillis)
            .toLocalDateTime(zone).date
        val today = kotlin.time.Instant.fromEpochMilliseconds(nowMillis)
            .toLocalDateTime(zone).date

        val effectiveStartDate = if (lastGeneratedDateMillis != null) {
            val lastGenDate = kotlin.time.Instant.fromEpochMilliseconds(lastGeneratedDateMillis)
                .toLocalDateTime(zone).date
            maxOf(startDate, lastGenDate.plus(DatePeriod(days = 1)))
        } else {
            startDate
        }

        val endDate = if (endDateMillis != null) {
            kotlin.time.Instant.fromEpochMilliseconds(endDateMillis).toLocalDateTime(zone).date
        } else {
            null
        }

        val dates = mutableListOf<Long>()
        var currentDate = effectiveStartDate

        while (currentDate <= today && (endDate == null || currentDate <= endDate)) {
            val currentMillis = currentDate.atStartOfDayIn(zone).toEpochMilliseconds()
            dates.add(currentMillis)

            currentDate = when (frequency) {
                RecurringFrequency.DAILY -> currentDate.plus(DatePeriod(days = 1))
                RecurringFrequency.WEEKLY -> currentDate.plus(DatePeriod(days = 7))
                RecurringFrequency.MONTHLY -> currentDate.plus(DatePeriod(months = 1))
                RecurringFrequency.YEARLY -> currentDate.plus(DatePeriod(years = 1))
            }
        }

        return dates
    }

    private fun computeNextDueDate(
        startDateMillis: Long,
        endDateMillis: Long?,
        frequency: RecurringFrequency,
        afterMillis: Long,
        zone: TimeZone,
    ): Long? {
        val startDate = kotlin.time.Instant.fromEpochMilliseconds(startDateMillis)
            .toLocalDateTime(zone).date
        val afterDate = kotlin.time.Instant.fromEpochMilliseconds(afterMillis)
            .toLocalDateTime(zone).date

        val endDate = if (endDateMillis != null) {
            kotlin.time.Instant.fromEpochMilliseconds(endDateMillis).toLocalDateTime(zone).date
        } else {
            null
        }

        var currentDate = startDate
        while (currentDate <= afterDate) {
            currentDate = when (frequency) {
                RecurringFrequency.DAILY -> currentDate.plus(DatePeriod(days = 1))
                RecurringFrequency.WEEKLY -> currentDate.plus(DatePeriod(days = 7))
                RecurringFrequency.MONTHLY -> currentDate.plus(DatePeriod(months = 1))
                RecurringFrequency.YEARLY -> currentDate.plus(DatePeriod(years = 1))
            }
        }

        if (endDate != null && currentDate > endDate) {
            return null
        }

        return currentDate.atStartOfDayIn(zone).toEpochMilliseconds()
    }
}
