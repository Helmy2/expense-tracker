package com.expense.tracker.feature.sample.impl

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import com.expense.tracker.feature.sample.domain.model.SampleCategory
import com.expense.tracker.shared.core.domain.AppError
import com.expense.tracker.shared.core.strings.Res
import com.expense.tracker.shared.core.strings.sample_category_architecture
import com.expense.tracker.shared.core.strings.sample_category_contract
import com.expense.tracker.shared.core.strings.sample_category_label
import com.expense.tracker.shared.core.strings.sample_category_preview
import com.expense.tracker.shared.core.strings.sample_description_label
import com.expense.tracker.shared.core.strings.sample_error_body
import com.expense.tracker.shared.core.strings.sample_title_label
import com.expense.tracker.shared.core.strings.sample_validation_required
import com.expense.tracker.shared.designsystem.components.SegmentedButton
import com.expense.tracker.shared.designsystem.components.TextField
import org.jetbrains.compose.resources.stringResource

@Composable
fun TitleField(
    formState: SampleFormState,
    onAction: (SampleAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = formState.title.asText(),
        onValueChange = { onAction(SampleAction.TitleChanged(it)) },
        placeholder = stringResource(Res.string.sample_title_label),
        isError = formState.titleError,
        supportingText = if (formState.titleError) stringResource(Res.string.sample_validation_required) else null,
        singleLine = true,
        modifier = modifier,
    )
}

@Composable
fun DescriptionField(
    formState: SampleFormState,
    onAction: (SampleAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = formState.description.asText(),
        onValueChange = { onAction(SampleAction.DescriptionChanged(it)) },
        placeholder = stringResource(Res.string.sample_description_label),
        isError = formState.descriptionError,
        supportingText = if (formState.descriptionError) stringResource(Res.string.sample_validation_required) else null,
        singleLine = false,
        minLines = 2,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    category: SampleCategory,
    onCategoryChange: (SampleCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = SampleCategory.entries.map { it.label() }
    val selectedIndex = SampleCategory.entries.indexOf(category)
    SegmentedButton(
        selectedIndex = selectedIndex,
        onOptionSelect = { index ->
            onCategoryChange(SampleCategory.entries[index])
        },
        options = options,
        modifier = modifier,
    )
}

@Composable
fun SampleCategory.label(): String = when (this) {
    SampleCategory.Contract -> stringResource(Res.string.sample_category_contract)
    SampleCategory.Architecture -> stringResource(Res.string.sample_category_architecture)
    SampleCategory.Preview -> stringResource(Res.string.sample_category_preview)
}

internal fun SampleTextUi.asText(): String = when (this) {
    is SampleTextUi.Raw -> value
}

@Composable
internal fun AppError.messageText(): String = when (this) {
    AppError.Unknown -> stringResource(Res.string.sample_error_body)
    is AppError.Message -> value
}

