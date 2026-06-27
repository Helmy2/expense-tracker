package com.expense.tracker.feature.sample.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class SampleItemTest {
    @Test
    fun preservesTitleAndDescription() {
        val item = SampleItem(
            id = "contract-first",
            title = "Contract-first features",
            description = "Generate features from compact contracts.",
            category = SampleCategory.Contract,
            occurredAtMillis = 100L,
            updatedAtMillis = 100L,
        )

        assertEquals("contract-first", item.id)
        assertEquals("Contract-first features", item.title)
        assertEquals("Generate features from compact contracts.", item.description)
    }

    @Test
    fun updateTrimsEditableFieldsAndPreservesIdentity() {
        val item = SampleItem(
            id = "contract-first",
            title = "Contract-first features",
            description = "Generate features from compact contracts.",
            category = SampleCategory.Contract,
            occurredAtMillis = 100L,
            updatedAtMillis = 100L,
        )

        val updated = item.update(
            title = " Updated title ",
            description = " Updated description ",
            category = SampleCategory.Architecture,
            updatedAtMillis = 200L,
        )

        assertEquals("contract-first", updated.id)
        assertEquals("Updated title", updated.title)
        assertEquals("Updated description", updated.description)
        assertEquals(SampleCategory.Architecture, updated.category)
        assertEquals(100L, updated.occurredAtMillis)
        assertEquals(200L, updated.updatedAtMillis)
    }
}
