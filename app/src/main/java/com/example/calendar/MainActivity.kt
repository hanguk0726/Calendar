package com.example.calendar

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import java.util.*

class MainActivity : AppCompatActivity() {

    private var mainCalendar: Calendar = Calendar.getInstance()
    private var listOfMainCalendar = mutableListOf(mainCalendar, mainCalendar, mainCalendar)
    private var lastPage = 1
    private var scheduleDataKey: MutableMap<String, MutableList<String>> = mutableMapOf()
    private var scheduleDataValue : MutableMap<String, Triple<String, Color, MutableList<String>>> = mutableMapOf()
    private var pickedDateForDialog : Calendar = Calendar.getInstance()
    val mainCalendarClone = { mainCalendar.clone() as Calendar }
    val buildKey = {  _calendar: Calendar ->
        var keyBuilder = ""
        keyBuilder += _calendar.get(Calendar.YEAR)
        keyBuilder += _calendar.get(Calendar.MONTH).toString().padStart(2, '0')
        keyBuilder += _calendar.get(Calendar.DATE).toString().padStart(2, '0')
        keyBuilder
    }
    var leftPage = { index: Int ->
        when (index) {
            0 -> 2
            1 -> 0
            // 2 -> 1
            else -> 1
        }
    }
    var rightPage = { index: Int ->
        when (index) {
            0 -> 1
            1 -> 2
            // 2 -> 0
            else -> 0
        }
    }

    data class DialogData(
        val year: Int,
        val month: Int,
        val date: Int,
        val day: Int,
    )
    var colorPalettes = listOf(Color(0xFF0275d8),Color(0xFF5cb85c),Color(0xFFf0ad4e),Color(0xFFd9534f))


    @ExperimentalComposeUiApi
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

    @ExperimentalComposeUiApi
    @ExperimentalFoundationApi
    @ExperimentalPagerApi
    @Composable
    fun CalendarPager() {
        val pagerState = rememberPagerState(pageCount = 3, initialPage = 1, infiniteLoop = true)
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


    @ExperimentalComposeUiApi
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
                Text(dayOfWeek[i], textAlign = TextAlign.Center)
            }
        }
    }

    @ExperimentalComposeUiApi
    @ExperimentalFoundationApi
    @Composable
    fun Date(calendar: Calendar) {

        var openDialog by remember {
            mutableStateOf(false)
        }
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
                Box(
                    modifier = Modifier.weight(1f)
                ) {

                    Row {
                        for (j in 0..6) {
                            val isPreviousMonth =
                                cellIndexOfCalendar < dateData[0].size

                            val isCurrentMonth =
                                dateData[0].size <= cellIndexOfCalendar && cellIndexOfCalendar < dateData[0].size + dateData[1].size

                            val isSunday = j == 0

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        when {
                                            isPreviousMonth -> with(calendar.clone() as Calendar) {
                                                add(Calendar.MONTH, -1)
                                                set(Calendar.DATE, listOfRowData[i][j])
                                                pickedDateForDialog = this
                                            }
                                            isCurrentMonth -> with(calendar.clone() as Calendar) {
                                                set(Calendar.DATE, listOfRowData[i][j])
                                                pickedDateForDialog = this
                                            }
                                            else -> with(calendar.clone() as Calendar) {
                                                add(Calendar.MONTH, +1)
                                                set(Calendar.DATE, listOfRowData[i][j])
                                                pickedDateForDialog = this
                                            }
                                        }
                                        openDialog = true
                                    }
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.SpaceAround,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = listOfRowData[i][j].toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        textAlign = TextAlign.Center,
                                        color = dateColor(isCurrentMonth, isSunday)
                                    )
                                    var key: String
                                    when{
                                        isPreviousMonth -> with(calendar.clone() as Calendar) {
                                            add(Calendar.MONTH, -1)
                                            set(Calendar.DATE, listOfRowData[i][j])
                                            key = buildKey(this)
                                        }
                                        isCurrentMonth -> with(calendar.clone() as Calendar) {
                                            set(Calendar.DATE, listOfRowData[i][j])
                                            key = buildKey(this)
                                        }
                                        else -> with(calendar.clone() as Calendar) {
                                            add(Calendar.MONTH, +1)
                                            set(Calendar.DATE, listOfRowData[i][j])
                                            key = buildKey(this)
                                        }
                                    }
                                    if(scheduleDataKey.containsKey(key)){
                                        val valueList = scheduleDataKey[key]
                                        for(item in valueList!!){
                                            val _scheduleDataValue = scheduleDataValue[item]
                                            Spacer(Modifier.height(2.dp))
                                            Card (
                                                Modifier.fillMaxWidth(0.9f),
                                                elevation = 0.dp,
                                                backgroundColor = _scheduleDataValue!!.second,
                                                shape = RoundedCornerShape(16)){
                                                Text("${_scheduleDataValue!!.first}",maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 1.dp),
                                                color= Color.White,
                                                textAlign = TextAlign.Center)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.weight(1.0f))
                                }
                            }
                            cellIndexOfCalendar++
                        }
                    }
                }
            }
        }
        if (openDialog) {
            ScheduleDialog(openDialog, pickedDateForDialog) { openDialog = false }
        }
    }

    @ExperimentalComposeUiApi
    @Composable
    fun ScheduleDialog(
        openDialog: Boolean,
        _pickedDateForDialog : Calendar,
        dismissDialog: () -> Unit
    ) {
        var scheduleContent by remember {
            mutableStateOf("")
        }
        val getNameForDayOfWeek = { value: Int ->
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")[value - 1]
        }
        val getNameForMonth = { value: Int ->
            listOf(
                "January", "February", "March", "April", "May", "June", "July", "August",
                "September", "October", "November", "December"
            )[value]
        }
        if (openDialog) {
            Dialog(
                onDismissRequest = {
                    dismissDialog()
                },
            ) {
                Surface(
                    modifier = Modifier
                        .width(400.dp)
                        .height(300.dp),
                    shape =  RoundedCornerShape(8)
                ) {
                    Column(
                        verticalArrangement = Arrangement.SpaceAround,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${getNameForDayOfWeek(_pickedDateForDialog.get(Calendar.DAY_OF_WEEK))}, ${_pickedDateForDialog.get(Calendar.DATE)}, ${
                                getNameForMonth(
                                    _pickedDateForDialog.get(Calendar.MONTH)
                                )
                            }",
                            textAlign = TextAlign.Center,
                            fontSize = 25.sp
                        )
                        OutlinedTextField(
                            value = scheduleContent,
                            onValueChange = { scheduleContent = it },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color.LightGray,
                                unfocusedBorderColor = Color.Gray,
                                cursorColor = Color.Black)
                        )

                        Button(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(50.dp),
                            onClick = {
                                saveSchedule(_pickedDateForDialog,scheduleContent)
                                scheduleContent = ""
                                dismissDialog()
                              },
                            colors = ButtonDefaults.buttonColors(backgroundColor = colorPalettes.first())
                        ) {
                            Text("Submit", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    private fun saveSchedule(
        _pickedDateForDialog : Calendar,
        scheduleContent : String
    ) {
        val uuidOfScheduleData = UUID.randomUUID().toString()
        val scheduleDuration = (1..7).random()
        val keyListOfScheduleData = mutableListOf<String>()
        val calendar = _pickedDateForDialog.clone() as Calendar
        for (i in 1 .. scheduleDuration){
            val _key = buildKey(calendar)
            keyListOfScheduleData.add(_key)
            if(scheduleDataKey.containsKey(_key)){
                scheduleDataKey[_key]!!.add(uuidOfScheduleData)
            }else{
                scheduleDataKey[_key] = mutableListOf(uuidOfScheduleData)
            }
            calendar.add(Calendar.DATE, 1)
        }
        scheduleDataValue[uuidOfScheduleData] = Triple(scheduleContent, colorPalettes.random(), keyListOfScheduleData)
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
