package com.example.calendar

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import java.lang.StringBuilder
import java.util.*

class MainActivity : AppCompatActivity() {

    private var mainCalendar: Calendar = Calendar.getInstance()
    private var listOfMainCalendar = mutableListOf(mainCalendar, mainCalendar, mainCalendar)
    private var lastPageOfCalendarPager = 1

    // <keyOfDataForRequestedDate, scheduleDataId>
    private var scheduleDataKeyMap: MutableMap<String, MutableList<String>> = mutableMapOf()

    // <scheduleDataId, scheduleDataValue>  scheduleDataValue = content, color, dates(requestedDateAsKey) for this schedule
    private var scheduleDataValueMap: MutableMap<String, Triple<String, Color, MutableList<String>>> =
        mutableMapOf()
    private var calendarOfPickedDateForDialog: Calendar = Calendar.getInstance()
    private val colorPalettes =
        listOf(Color(0xFF0275d8), Color(0xFF5cb85c), Color(0xFFf0ad4e), Color(0xFFd9534f))
    val cloneOfCalendar = { calendar: Calendar -> calendar.clone() as Calendar }
    val generatedKey = { calendar: Calendar ->
        with(StringBuilder()) {
            append(calendar.get(Calendar.YEAR).toString())
            append(calendar.get(Calendar.MONTH).toString().padStart(2, '0'))
            append(calendar.get(Calendar.DATE).toString().padStart(2, '0'))
            toString()
        }
    }
    var leftPageOfCalendarPager = { page: Int ->
        when (page) {
            0 -> 2
            1 -> 0
            // when 2
            else -> 1
        }
    }
    var rightPageOfCalendarPager = { page: Int ->
        when (page) {
            0 -> 1
            1 -> 2
            // when 2
            else -> 0
        }
    }

    val dateColor = { isInCurrentMonth: Boolean, isSunday: Boolean ->
        if (isSunday) {
            if (isInCurrentMonth) Color.Red else Color.Red.copy(alpha = 0.3f)
        } else {
            if (isInCurrentMonth) Color.Black else Color.LightGray
        }
    }
    val nameOfDayOfWeek = { value: Int ->
        // enum Calendar.DAY_OF_WEEK -> 1 ~ 7
        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")[value - 1]
    }
    val nameOfMonth = { value: Int ->
        listOf(
            "January", "February", "March", "April", "May", "June", "July", "August",
            "September", "October", "November", "December"
        )[value]
    }

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
                // when user starts dragging, target page should already be changed.
                when {
                    pagerState.currentPageOffset == 0f ->
                        listOfMainCalendar = mutableListOf(mainCalendar, mainCalendar, mainCalendar)
                    pagerState.currentPageOffset < 0 -> {
                        val calendarOfLeftPage = with(cloneOfCalendar(mainCalendar)) {
                            add(Calendar.MONTH, -1)
                            this
                        }
                        listOfMainCalendar[leftPageOfCalendarPager(pagerState.currentPage)] =
                            calendarOfLeftPage
                    }
                    pagerState.currentPageOffset > 0 -> {
                        val calendarOfRightPage = with(cloneOfCalendar(mainCalendar)) {
                            add(Calendar.MONTH, +1)
                            this
                        }
                        listOfMainCalendar[rightPageOfCalendarPager(pagerState.currentPage)] =
                            calendarOfRightPage
                    }
                }
                // when the drag ends, modify original data(mainCalendar)
                val isSwipedLeft = when (pagerState.currentPage) {
                    0 -> lastPageOfCalendarPager == 1
                    1 -> lastPageOfCalendarPager == 2
                    2 -> lastPageOfCalendarPager == 0
                    else -> false
                }
                if (lastPageOfCalendarPager != pagerState.currentPage) {
                    if (isSwipedLeft) {
                        mainCalendar.add(Calendar.MONTH, -1)
                    } else {
                        mainCalendar.add(Calendar.MONTH, +1)
                    }
                }
                lastPageOfCalendarPager = pagerState.currentPage
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
        val listOfDateOfPreviousMonth = dateData[0]
        val listOfDateOfCurrentMonth = dateData[1]
        val listOfDateOfNextMonth = dateData[2]
        val datesInTotal =
            listOfDateOfPreviousMonth.size + listOfDateOfCurrentMonth.size + listOfDateOfNextMonth.size
        val weeksOfMonth = datesInTotal / 7
        val listOfDateOfEachWeek = MutableList(weeksOfMonth) { mutableListOf<Int>() }
        var boxIndexOfCalendar = 0
        var indexOfDataListForEachWeek: Int
        for (data in dateData) {
            if (data.isNotEmpty()) {
                for (i in data.indices) {
                    indexOfDataListForEachWeek =
                        if (boxIndexOfCalendar != 0) boxIndexOfCalendar / 7 else 0
                    listOfDateOfEachWeek[indexOfDataListForEachWeek].add(
                        data[i]
                    )
                    boxIndexOfCalendar++
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            boxIndexOfCalendar = 0
            for (i in 0 until weeksOfMonth) {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    Row {
                        for (j in 0..6) {
                            val isPreviousMonth =
                                boxIndexOfCalendar < listOfDateOfPreviousMonth.size
                            val isCurrentMonth =
                                listOfDateOfPreviousMonth.size <= boxIndexOfCalendar && boxIndexOfCalendar < listOfDateOfPreviousMonth.size + listOfDateOfCurrentMonth.size
                            val isSunday = j == 0
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        when {
                                            isPreviousMonth -> with(cloneOfCalendar(calendar)) {
                                                add(Calendar.MONTH, -1)
                                                set(Calendar.DATE, listOfDateOfEachWeek[i][j])
                                                calendarOfPickedDateForDialog = this
                                            }
                                            isCurrentMonth -> with(cloneOfCalendar(calendar)) {
                                                set(Calendar.DATE, listOfDateOfEachWeek[i][j])
                                                calendarOfPickedDateForDialog = this
                                            }
                                            else -> with(cloneOfCalendar(calendar)) {
                                                add(Calendar.MONTH, +1)
                                                set(Calendar.DATE, listOfDateOfEachWeek[i][j])
                                                calendarOfPickedDateForDialog = this
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
                                        text = listOfDateOfEachWeek[i][j].toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        textAlign = TextAlign.Center,
                                        color = dateColor(isCurrentMonth, isSunday)
                                    )
                                    var keyOfRequestedDate: String
                                    when {
                                        isPreviousMonth -> with(cloneOfCalendar(calendar)) {
                                            add(Calendar.MONTH, -1)
                                            set(Calendar.DATE, listOfDateOfEachWeek[i][j])
                                            keyOfRequestedDate = generatedKey(this)
                                        }
                                        isCurrentMonth -> with(cloneOfCalendar(calendar)) {
                                            set(Calendar.DATE, listOfDateOfEachWeek[i][j])
                                            keyOfRequestedDate = generatedKey(this)
                                        }
                                        else -> with(cloneOfCalendar(calendar)) {
                                            add(Calendar.MONTH, +1)
                                            set(Calendar.DATE, listOfDateOfEachWeek[i][j])
                                            keyOfRequestedDate = generatedKey(this)
                                        }
                                    }
                                    if (scheduleDataKeyMap.containsKey(keyOfRequestedDate)) {
                                        val listOfKeyForScheduleDataValue =
                                            scheduleDataKeyMap[keyOfRequestedDate]
                                        for (element in listOfKeyForScheduleDataValue!!) {
                                            val data = scheduleDataValueMap[element]
                                            Spacer(Modifier.height(2.dp))
                                            Card(
                                                Modifier.fillMaxWidth(0.9f),
                                                elevation = 0.dp,
                                                backgroundColor = data!!.second,
                                                shape = RoundedCornerShape(16)
                                            ) {
                                                Text(
                                                    "${data!!.first}",
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    fontSize = 12.sp,
                                                    modifier = Modifier.padding(horizontal = 1.dp),
                                                    color = Color.White,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.weight(1.0f))
                                }
                            }
                            boxIndexOfCalendar++
                        }
                    }
                }
            }
        }
        if (openDialog) {
            ScheduleDialog(openDialog, calendarOfPickedDateForDialog) { openDialog = false }
        }
    }

    @ExperimentalComposeUiApi
    @Composable
    fun ScheduleDialog(
        openDialog: Boolean,
        calendarDataOfPickedDate: Calendar,
        dismissDialog: () -> Unit
    ) {
        var scheduleContent by remember {
            mutableStateOf("")
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
                    shape = RoundedCornerShape(8)
                ) {
                    Column(
                        verticalArrangement = Arrangement.SpaceAround,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        var title = with(StringBuilder()) {
                            append(nameOfDayOfWeek(calendarDataOfPickedDate.get(Calendar.DAY_OF_WEEK)))
                            append(", ")
                            append(calendarDataOfPickedDate.get(Calendar.DATE))
                            append(", ")
                            append(nameOfMonth(calendarDataOfPickedDate.get(Calendar.MONTH)))
                            toString()
                        }
                        Text(
                            text = title,
                            textAlign = TextAlign.Center,
                            fontSize = 25.sp
                        )
                        OutlinedTextField(
                            value = scheduleContent,
                            onValueChange = { scheduleContent = it },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color.LightGray,
                                unfocusedBorderColor = Color.Gray,
                                cursorColor = Color.Black
                            )
                        )
                        Button(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(50.dp),
                            onClick = {
                                saveSchedule(calendarDataOfPickedDate, scheduleContent)
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
        calendarDataOfPickedDate: Calendar,
        scheduleContent: String
    ) {
        val uuidOfScheduleDataValue = UUID.randomUUID().toString()
        val scheduleDuration = (1..7).random()
        val listOfKeyOfRequestedDate = mutableListOf<String>()
        val calendarOfPickedDate = cloneOfCalendar(calendarDataOfPickedDate)
        for (i in 1..scheduleDuration) {
            val keyOfDate = generatedKey(calendarOfPickedDate)
            listOfKeyOfRequestedDate.add(keyOfDate)
            if (scheduleDataKeyMap.containsKey(keyOfDate)) {
                scheduleDataKeyMap[keyOfDate]!!.add(uuidOfScheduleDataValue)
            } else {
                scheduleDataKeyMap[keyOfDate] = mutableListOf(uuidOfScheduleDataValue)
            }
            calendarOfPickedDate.add(Calendar.DATE, 1)
        }
        scheduleDataValueMap[uuidOfScheduleDataValue] =
            Triple(scheduleContent, colorPalettes.random(), listOfKeyOfRequestedDate)
        Log.i("calendar_logger", "scheduleDataKeyMap :: $scheduleDataKeyMap")
        Log.i("calendar_logger", "scheduleDataValueMap :: $scheduleDataValueMap")
    }

    private fun getDateData(calendar: Calendar): Triple<List<Int>, List<Int>, List<Int>> {
        val lastDateOfPreviousMonth = with(cloneOfCalendar(calendar)) {
            add(Calendar.MONTH, -1)
            getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        val lastDateOfCurrentMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val dateOfFirstBoxOfCalendarGrid = {
            val firstDayOfCurrentMonth = with(cloneOfCalendar(calendar)) {
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
        val dateOfLastBoxOfCalendarGrid = {
            val lastDayOfCurrentMonth = with(cloneOfCalendar(calendar)) {
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
        val listOfDateOfPreviousMonth: List<Int> = with(dateOfFirstBoxOfCalendarGrid()) {
            val firstBox = dateOfFirstBoxOfCalendarGrid()
            if (firstBox != 1) {
                (dateOfFirstBoxOfCalendarGrid()..lastDateOfPreviousMonth).toList()
            } else {
                listOf()
            }
        }
        val listOfDateOfCurrentMonth = (1..lastDateOfCurrentMonth).toList()

        val listOfDateOfNextMonth: List<Int> = with(dateOfFirstBoxOfCalendarGrid()) {
            val lastBox = dateOfLastBoxOfCalendarGrid()
            if (lastDateOfCurrentMonth != lastBox) {
                (1..lastBox).toList()
            } else {
                listOf()
            }
        }
        val totalDates =
            listOfDateOfPreviousMonth.size + listOfDateOfCurrentMonth.size + listOfDateOfNextMonth.size
        assert(totalDates == 35 || totalDates == 42) {
            "Total dates in the calendar should be 35 or 42"
        }
        return Triple(listOfDateOfPreviousMonth, listOfDateOfCurrentMonth, listOfDateOfNextMonth)
    }

}
