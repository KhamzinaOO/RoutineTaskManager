package com.example.routinetaskmanager.core.presentation.ui.dateTime

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

private const val WEEK_CAROUSEL_START_PAGE = 1000
private const val WEEK_CAROUSEL_PAGE_COUNT = 2000

val LocalDateSaver = Saver<LocalDate, Long>(
    save = { it.toEpochDay() },
    restore = { LocalDate.ofEpochDay(it) }
)

fun LocalDate.startOfWeek(): LocalDate =
    with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

fun LocalDate.daysOfWeekFromMonday(): List<LocalDate> {
    val start = startOfWeek()
    return (0..6).map { start.plusDays(it.toLong()) }
}

fun pageToWeekDate(
    page: Int,
    startPage: Int,
    today: LocalDate
): LocalDate {
    val weekOffset = page - startPage
    return if (weekOffset == 0) {
        today
    } else {
        today.plusWeeks(weekOffset.toLong()).startOfWeek()
    }
}

@Composable
fun WeekCarousel(
    modifier: Modifier = Modifier,
    onDaySelected: (LocalDate) -> Unit
) {
    val locale = remember { Locale.getDefault() }
    val today = remember { LocalDate.now() }

    val pagerState = rememberPagerState(
        initialPage = WEEK_CAROUSEL_START_PAGE
    ) { WEEK_CAROUSEL_PAGE_COUNT }

    var selectedDate by rememberSaveable(stateSaver = LocalDateSaver) {
        mutableStateOf(today)
    }

    LaunchedEffect(pagerState.currentPage) {
        val weekBaseDate = pageToWeekDate(
            page = pagerState.currentPage,
            startPage = WEEK_CAROUSEL_START_PAGE,
            today = today
        )

        selectedDate = if (pagerState.currentPage == WEEK_CAROUSEL_START_PAGE) {
            today
        } else {
            weekBaseDate
        }
    }

    LaunchedEffect(selectedDate) {
        onDaySelected(selectedDate)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.tertiary)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.wrapContentSize()
        ) { page ->
            val weekBaseDate = pageToWeekDate(
                page = page,
                startPage = WEEK_CAROUSEL_START_PAGE,
                today = today
            )

            val weekToShow = weekBaseDate.daysOfWeekFromMonday()

            WeekRow(
                week = weekToShow,
                isSelected = { it == selectedDate },
                locale = locale,
                onClick = { clickedDate ->
                    selectedDate = clickedDate
                }
            )
        }
    }
}

@Composable
fun WeekRow(
    week: List<LocalDate>,
    isSelected: (LocalDate) -> Boolean,
    locale: Locale,
    onClick: (LocalDate) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        week.forEach { day ->
            DayOfWeekBox(
                isSelected = isSelected(day),
                date = day.format(DateTimeFormatter.ofPattern("d")),
                dayOfWeek = day.dayOfWeek.getDisplayName(
                    TextStyle.SHORT_STANDALONE,
                    locale
                ),
                onClick = { onClick(day) }
            )
        }
    }
}

@Composable
fun DayOfWeekBox(
    isSelected: Boolean,
    date: String,
    dayOfWeek: String,
    onClick: () -> Unit
) {
    val selectedTextColor = MaterialTheme.colorScheme.tertiary
    val selectedContainerColor = MaterialTheme.colorScheme.onTertiary

    val unselectedTextColor = MaterialTheme.colorScheme.onTertiary
    val unselectedContainerColor = Color.Transparent

    Box(
        Modifier
            .padding(top = 8.dp, bottom = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) selectedContainerColor else unselectedContainerColor
            )
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) selectedTextColor else unselectedTextColor
            )
            Text(
                text = dayOfWeek,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) selectedTextColor else unselectedTextColor
            )
        }
    }
}