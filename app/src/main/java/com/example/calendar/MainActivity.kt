package com.example.calendar

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyGridScope
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

class MainActivity : AppCompatActivity() {
    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // set up system & activity ui
        supportActionBar?.hide()
        window.statusBarColor = android.graphics.Color.GRAY

        setContent {

            var calendar by remember { mutableStateOf(Calendar.getInstance()) }
            calendar.set(Calendar.MONTH, 8)
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                Column(
                    Modifier.padding(20.dp),
                ) {
                    MonthAndYear(calendar)
                    Spacer(Modifier.height(10.dp))
                    DayOfWeek()
                    Spacer(Modifier.height(15.dp))
                    Date(calendar)
                }
            }
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
        val dateColor = {isInCurrentMonth : Boolean, isSunday : Boolean ->
            if(isSunday){
                if(isInCurrentMonth) Color.Red else Color.Red.copy(alpha = 0.3f)
            }else{
                if(isInCurrentMonth) Color.Black else Color.LightGray
            }

        }
        LazyVerticalGrid(
            cells = GridCells.Fixed(7),
            content = {
                var gridIndex = 0
                for((componentIndex, component) in dateData.withIndex()) {
                    if(component.isNotEmpty()){
                        items(component.size) { index ->
                            Box(
                                modifier = Modifier
                                    .height(120.dp)
                                    .fillMaxWidth(),
                            ) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = component[index].toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    textAlign = TextAlign.Center,
                                    color = dateColor((componentIndex == 1), gridIndex % 7 == 0)
                                )
                            }
                            gridIndex++
                        }
                    }
                }
            })
    }

    private fun getDateData(calendar: Calendar): Triple<List<Int>, List<Int>, List<Int>> {
        val lastDateOfPreviousMonth = with(calendar.clone() as Calendar) {
            val isJanuary = calendar.get(Calendar.MONTH) == 0
            if (isJanuary) {
                set(Calendar.MONTH, 11)
            } else {
                set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1)
            }
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
