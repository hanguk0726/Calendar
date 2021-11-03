package com.example.calendar

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import java.util.*

class MainActivity : AppCompatActivity() {
    private var mainCalendar: Calendar = Calendar.getInstance()
    val mainCalendarClone = { mainCalendar.clone() as Calendar }
    var listOfMainCalendar = mutableListOf(mainCalendar, mainCalendar, mainCalendar)
    var leftPage = { index: Int ->
        when (index) {
            0 -> 2
            1 -> 0
            2 -> 1
            else -> 0
        }
    }
    var rightPage = { index: Int ->
        when (index) {
            0 -> 1
            1 -> 2
            2 -> 0
            else -> 2
        }
    }
    var lastPage = 1


    @ExperimentalPagerApi
    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // set up system & activity ui
        supportActionBar?.hide()
        window.statusBarColor = android.graphics.Color.GRAY

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                CalendarPager()
            }
        }
    }

    @ExperimentalFoundationApi
    @ExperimentalPagerApi
    @Composable
    fun CalendarPager() {
        var pagerState = rememberPagerState(pageCount = 3, initialPage = 1, infiniteLoop = true)
        HorizontalPager(
            modifier = Modifier
                .fillMaxSize(),
            state = pagerState,
            verticalAlignment = Alignment.Top
        ) { index ->
            LaunchedEffect(pagerState.currentPageOffset) {
                when {
                    pagerState.currentPageOffset == 0f ->
                        listOfMainCalendar = mutableListOf(mainCalendar, mainCalendar, mainCalendar)
                    pagerState.currentPageOffset < 0 -> {
                        val calendarOfLeftPage = with(mainCalendarClone()) {
                            add(Calendar.MONTH, -1)
                            this
                        }
                        listOfMainCalendar[leftPage(pagerState.currentPage)] = calendarOfLeftPage
                    }
                    pagerState.currentPageOffset > 0 -> {
                        val calendarOfRightPage = with(mainCalendarClone()) {
                            add(Calendar.MONTH, +1)
                            this
                        }
                        listOfMainCalendar[rightPage(pagerState.currentPage)] = calendarOfRightPage
                    }
                }
                val isSwipedLeft = when (pagerState.currentPage) {
                    0 -> lastPage == 1
                    1 -> lastPage == 2
                    2 -> lastPage == 0
                    else -> false
                }
                if (lastPage != pagerState.currentPage) {
                    if (isSwipedLeft) {
                        mainCalendar.add(Calendar.MONTH, -1)
                    } else {
                        mainCalendar.add(Calendar.MONTH, +1)
                    }
                }
                lastPage = pagerState.currentPage
            }
            CalendarComponent(listOfMainCalendar[index])
        }
    }


    @ExperimentalFoundationApi
    @Composable
    fun CalendarComponent(calendar: Calendar) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
        ) {
            MonthAndYear(calendar)
            Spacer(Modifier.height(10.dp))
            DayOfWeek()
            Spacer(Modifier.height(15.dp))
            Date(calendar)
        }
    }

    @Composable
    fun MonthAndYear(calendar: Calendar) {
        // 0 ~ 11 + 1
        val actualMonth = calendar.get(Calendar.MONTH) + 1
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(10.dp))
            Text(
                text = "$actualMonth",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "${calendar.get(Calendar.YEAR)}",
                fontSize = 20.sp,
                color = Color.Gray
            )
        }
    }

    @Composable
    fun DayOfWeek() {
        val dayOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            for (i in 0..6) {
                Text("${dayOfWeek[i]}", textAlign = TextAlign.Center)
            }
        }
    }

    @ExperimentalFoundationApi
    @Composable
    fun Date(calendar: Calendar) {

        val dateData = getDateData(calendar).toList()
        val dateColor = { isInCurrentMonth: Boolean, isSunday: Boolean ->
            if (isSunday) {
                if (isInCurrentMonth) Color.Red else Color.Red.copy(alpha = 0.3f)
            } else {
                if (isInCurrentMonth) Color.Black else Color.LightGray
            }

        }

        val datesInTotal = dateData[0].size + dateData[1].size + dateData[2].size
        val maximumLineOfMonth = datesInTotal / 7
        val listOfRowData = MutableList(maximumLineOfMonth) { mutableListOf<Int>() }
        var cellIndexOfCalendar = 0
        var index: Int
        for (data in dateData) {
            if (data.isNotEmpty()) {
                for (i in data.indices) {
                    index = if (cellIndexOfCalendar != 0) cellIndexOfCalendar / 7 else 0
                    listOfRowData[index].add(
                        data[i]
                    )
                    cellIndexOfCalendar++
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            cellIndexOfCalendar = 0
            for (i in 0 until maximumLineOfMonth) {
                Row(
                    modifier = Modifier.weight(1f)
                ) {
                    for (j in 0..6) {
                        var isCurrentMonth = { cellIndexOfCalendar: Int ->
                            dateData[0].size <= cellIndexOfCalendar && cellIndexOfCalendar < dateData[0].size + dateData[1].size
                        }
                        var isSunday = j == 0
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = listOfRowData[i][j].toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
                                color = dateColor(isCurrentMonth(cellIndexOfCalendar), isSunday)
                            )
                        }
                        cellIndexOfCalendar++
                    }
                }
            }
        }
    }

    private fun getDateData(calendar: Calendar): Triple<List<Int>, List<Int>, List<Int>> {
        val lastDateOfPreviousMonth = with(calendar.clone() as Calendar) {
            add(Calendar.MONTH, -1)
            getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        val lastDateOfCurrentMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val dateOfFirstCellOfCalendarGrid = {
            val firstDayOfCurrentMonth = with(calendar.clone() as Calendar) {
                set(Calendar.DATE, 1)
                get(Calendar.DAY_OF_WEEK)
            }
            val isSunday = firstDayOfCurrentMonth == 1
            if (isSunday) {
                1
            } else {
                lastDateOfPreviousMonth - (firstDayOfCurrentMonth - 2)
            }
        }
        val dateOfLastCellOfCalendarGrid = {
            val lastDayOfCurrentMonth = with(calendar.clone() as Calendar) {
                set(Calendar.DATE, lastDateOfCurrentMonth)
                get(Calendar.DAY_OF_WEEK)
            }
            val isSaturday = lastDayOfCurrentMonth == 7
            if (isSaturday) {
                lastDateOfCurrentMonth
            } else {
                7 - lastDayOfCurrentMonth
            }
        }
        val listOfDateForPreviousMonth: List<Int> = with(dateOfFirstCellOfCalendarGrid()) {
            val firstCell = dateOfFirstCellOfCalendarGrid()
            if (firstCell != 1) {
                (dateOfFirstCellOfCalendarGrid()..lastDateOfPreviousMonth).toList()
            } else {
                listOf()
            }
        }
        val listOfDateForCurrentMonth = (1..lastDateOfCurrentMonth).toList()

        val listOfDateForNextMonth: List<Int> = with(dateOfFirstCellOfCalendarGrid()) {
            val lastCell = dateOfLastCellOfCalendarGrid()
            if (lastDateOfCurrentMonth != lastCell) {
                (1..lastCell).toList()
            } else {
                listOf()
            }
        }
        val totalDates =
            listOfDateForPreviousMonth.size + listOfDateForCurrentMonth.size + listOfDateForNextMonth.size
        assert(totalDates == 35 || totalDates == 42) {
            "Total dates in the calendar should be 35 or 42"
        }
        return Triple(listOfDateForPreviousMonth, listOfDateForCurrentMonth, listOfDateForNextMonth)
    }

}
