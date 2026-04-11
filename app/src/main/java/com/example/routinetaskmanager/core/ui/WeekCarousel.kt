package com.example.routinetaskmanager.core.ui

import android.icu.util.Calendar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

fun LocalDate.startOfWeek(): LocalDate {
    return this.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
}

fun LocalDate.weekAround() : List<LocalDate>{
    val start = startOfWeek()
    return (0..6).map { start.plusDays(it.toLong()) }
}

@Composable
fun WeekCarousel(
    modifier: Modifier = Modifier
) {
    val locale = Locale.getDefault()
    val today = remember { LocalDate.now() }

    val startPage = 1000
    val pagerState = rememberPagerState(initialPage = startPage) { 2000 }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.wrapContentSize()
    ) { page ->
        val weekOffset = page - startPage
        var selectedWeekDate by remember {
            mutableStateOf(if (weekOffset != 0) today.plusWeeks(weekOffset.toLong()).startOfWeek() else today)
        }
        val weekToShow = selectedWeekDate.weekAround()
        WeekRow(
            week = weekToShow,
            isSelected = { it == selectedWeekDate },
            locale = locale,
            onClick = {
                selectedWeekDate = it
            }
        )
    }
}

@Composable
fun WeekRow(
    week : List<LocalDate>,
    isSelected : (LocalDate) -> Boolean,
    locale : Locale,
    onClick: (LocalDate) -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.tertiary),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        week.forEach { day ->
            DayOfWeekBox(
                isSelected = isSelected(day),
                date = day.format(DateTimeFormatter.ofPattern("d")),
                dayOfWeek = day.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT_STANDALONE, locale),
                onClick = { onClick(day) }
            )
        }
    }
}

@Composable
fun DayOfWeekBox(
    isSelected : Boolean,
    date : String,
    dayOfWeek : String,
    onClick : () -> Unit
){
    val selectedTextColor = MaterialTheme.colorScheme.tertiary
    val selectedContainerColor = MaterialTheme.colorScheme.primary

    val unselectedTextColor = MaterialTheme.colorScheme.primary
    val unselectedContainerColor = Color.Transparent

    Box(
        Modifier
            .padding(top = 8.dp, bottom = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) selectedContainerColor else unselectedContainerColor
            )
            .clickable(
                onClick = onClick
            )
    ){
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.bodyLarge,
                color = if(isSelected) selectedTextColor else unselectedTextColor
            )
            Text(
                text = dayOfWeek,
                style = MaterialTheme.typography.bodyLarge,
                color = if(isSelected) selectedTextColor else unselectedTextColor
            )
        }
    }
}