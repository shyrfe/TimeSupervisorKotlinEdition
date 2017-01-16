package com.example.vshurygin.timesupervisorkotlinedition

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.IntegerRes
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import io.realm.Realm
import com.example.vshurygin.timesupervisorkotlinedition.DateRecord
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_listview.view.*
import kotlin.properties.Delegates
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {
    val mLocalContext: Context;
    var realm: Realm by Delegates.notNull();


    init{
        mLocalContext = this;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Realm.init(this);
        //val config = RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build()
        //realm = Realm.getInstance(config);
        realm = Realm.getDefaultInstance();
        //realm.deleteAll();
        CalculateOfTimeButton.setOnClickListener {
            calculateOfTimeButtonListener();
        }
        AddRecordButton.setOnClickListener {
            addRecordButtonClickListener()
        }
        ClearAllRecordsButton.setOnClickListener {
            deleteAllRecords();
        }


        val results = realm.where(DateRecord::class.java).findAll();
        val allRecords : MutableList<DateRecord> = mutableListOf();
        for(item:DateRecord in results){
            allRecords.add(item);
            Log.d("add","add");
        }
        for(item:DateRecord in allRecords){
            Log.d("Result",item.TheBeginningOfDayTime.toString());
            Log.d("Result",item.TheEndingOfDayTime.toString());
            Log.d("Result",item.Date.toString());
            Log.d("Result",item.WeekNumber.toString());
        }
        RecordsList.adapter = ResultListAdapter(allRecords);
    }

    override fun onDestroy() {
        super.onDestroy();
        realm.close();
    }

    fun addRecord(theBeginningOfTheDay: String, theEndingOfTheDay: String, date: String, weekNumber: Int){
        realm.executeTransaction {
                    var record = realm.createObject(DateRecord::class.java);
                    record.TheBeginningOfDayTime = theBeginningOfTheDay;
                    record.TheEndingOfDayTime = theEndingOfTheDay;
                    record.Date = date;
                    record.WeekNumber = weekNumber;
        }
        Log.d("addRecords","Records Added");
    }
    fun deleteAllRecords(){
        realm.executeTransaction { Realm.Transaction {
                fun execute(realm:Realm){
                    realm.deleteAll();
                }
            }
        }
    }


    fun calculateHoursForWeek(week:Int):Int{
        var TotalHours = 0;
        var TotalMinute = 0;
        var TotalTime = 0;

        for(record:DateRecord in realm.where(DateRecord::class.java).findAll()){
            if(record.WeekNumber == week){
                val sdf = SimpleDateFormat("HH:mm");
                val Bdate = sdf.parse(record.TheBeginningOfDayTime);
                val Bcalendar = GregorianCalendar.getInstance();
                Bcalendar.setTime(Bdate);

                val Bhour = Bcalendar.get(Calendar.HOUR_OF_DAY);
                val Bminute = Bcalendar.get(Calendar.MINUTE);

                val Edate = sdf.parse(record.TheEndingOfDayTime);
                val Ecalendar = GregorianCalendar.getInstance();
                Ecalendar.setTime(Edate);

                val Ehour = Ecalendar.get(Calendar.HOUR_OF_DAY);
                val Eminute = Ecalendar.get(Calendar.MINUTE);

                var Thours = Ehour - Bhour;
                var Tminute = Eminute - Bminute;

                if(Tminute < 0){
                    Thours--;
                    Tminute = Math.abs(60 + Tminute);
                }

                var WorkHour = Thours - 6;
                var WorkMinute = Tminute;

                if((WorkHour < 0) && (WorkMinute != 0)){
                    WorkMinute = 60 - WorkMinute;
                    WorkHour++;
                }

                if(WorkHour >= 0){
                    if(TotalHours >= 0){
                        if(TotalMinute + WorkMinute >= 60){
                            TotalHours = TotalHours + (WorkHour + 1);
                            TotalMinute = (TotalMinute + WorkMinute) - 60;
                        }
                        else{
                            TotalHours = TotalHours + WorkHour;
                            TotalMinute = TotalMinute + WorkMinute;
                        }
                    }
                    else{
                        if(Math.abs(TotalHours) < WorkHour){
                            if(TotalMinute < WorkMinute){
                                TotalHours = TotalHours + WorkHour;
                                TotalMinute = WorkMinute - TotalMinute;
                            }
                            else{
                                WorkHour--;
                                TotalHours = TotalHours + WorkHour;
                                TotalMinute = (60 + WorkMinute) - TotalMinute;
                            }
                        }
                        else{
                            if(TotalMinute >= WorkMinute){
                                TotalHours = TotalHours + WorkHour;
                                TotalMinute = TotalMinute - WorkMinute;
                            }
                            else{
                                WorkHour++;
                                TotalHours = TotalHours + WorkHour;
                                TotalMinute = (60 + TotalMinute) - WorkMinute;
                            }
                        }
                    }
                }
                else{
                    if(TotalHours >= 0){
                        if(TotalMinute - WorkMinute >= 0){
                            TotalHours = TotalHours - Math.abs(WorkHour);
                            TotalMinute = TotalMinute - WorkMinute;
                        }
                        else{
                            TotalHours = TotalHours - (Math.abs(WorkHour)+1);
                            TotalMinute = 60 + (TotalMinute - WorkMinute);
                        }
                    }
                    else{
                        if(TotalMinute + WorkMinute >= 60){
                            TotalHours = TotalHours - (Math.abs(WorkHour)+1);
                            TotalMinute = 60 - (TotalMinute + WorkMinute);
                        }
                        else{
                            TotalHours = TotalHours - Math.abs(WorkHour);
                            TotalMinute = TotalMinute + WorkMinute;
                        }
                    }
                }
            }
        }
        if(TotalHours > 0){
            TotalTime = (Math.abs(TotalHours)*100+Math.abs(TotalMinute));
        }
        else{
            TotalTime = (Math.abs(TotalHours)*100+Math.abs(TotalMinute)) * -1;
        }
       return TotalTime;
    }

    fun calculateOfTimeButtonListener(){
        if(!WeekNumber.text.toString().equals("")){
            val weeknumber = Integer.parseInt(WeekNumber.text.toString());
            val totalHours = calculateHoursForWeek(weeknumber);
            val hours = totalHours / 100;
            var minute = totalHours % 100;
            if(minute < 0){
                minute *= -1;
            }
            CalculatedTime.setText(hours.toString()+":"+minute.toString());
        }
        else{
            Toast.makeText( mLocalContext,R.string.EmptyWeek,Toast.LENGTH_SHORT).show();
        }
    }
    fun addRecordButtonClickListener(){
        val beginningOfTheDay = TheBeginningOfTheDay.text.toString();
        val endingOfTheDay = TheEndingOfTheDay.text.toString();
        val date = TodaysDate.text.toString();
        val sWeekNumber = WeekNumber.text.toString();

        fun checkTime(value:String):Boolean{
            val pat = Pattern.compile("^(([0,1][0-9])|(2[0-3])):[0-5][0-9]$");
            val mat = pat.matcher(value);
            return mat.matches();
        }
        fun checkDate(value:String):Boolean{
            val pat = Pattern.compile("(0[1-9]|1[0-9]|2[0-9]|3[01])-(0[1-9]|1[012])-[0-9]{4}");
            val mat = pat.matcher(value);
            return mat.matches();
        }

        if((checkDate(date))&&(checkTime(beginningOfTheDay))&&(checkTime(endingOfTheDay))&&(sWeekNumber != "")){
            val weeknumber = Integer.parseInt(sWeekNumber);
            addRecord(beginningOfTheDay,endingOfTheDay,date,weeknumber);
        }
        else{
            Log.d("Validate","Date: ${checkDate(date).toString()}");
            Log.d("Validate","Beginning: ${checkTime(beginningOfTheDay).toString()}");
            Log.d("Validate","Ending: ${checkTime(endingOfTheDay).toString()}");
            Log.d("Validate","Input data are not valid");
        }

    }
}

class ResultListAdapter(records:List<DateRecord>) : BaseAdapter(){

    val mRecords : List<DateRecord>;

    init{
        mRecords = records;
    }

    override fun getCount(): Int {
        return mRecords.size;
    }
    override fun getItem(position: Int):DateRecord{
        return mRecords.get(position);
    }
    override fun getItemId(id:Int):Long{
        return id.toLong();
    }
    override fun getView(position: Int,convertView:View?,parent:ViewGroup):View?{
        var view = convertView;
        if(view == null){
            view = LayoutInflater.from(parent.context).inflate(R.layout.activity_listview,parent,false);
        }
        val record = getItem(position);

        view?.listItemWeekNumber?.setText(record.WeekNumber.toString());
        view?.listItemBeginningOfDayTime?.setText(record.TheBeginningOfDayTime.toString());
        view?.listItemEndingOfDayTime?.setText(record.TheEndingOfDayTime.toString());
        view?.listItemDate?.setText(record.Date.toString());

        return view;
    }
}
