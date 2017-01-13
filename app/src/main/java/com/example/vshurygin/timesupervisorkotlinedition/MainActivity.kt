package com.example.vshurygin.timesupervisorkotlinedition

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import io.realm.Realm
import com.example.vshurygin.timesupervisorkotlinedition.DateRecord
import io.realm.RealmConfiguration
import kotlin.properties.Delegates
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import java.text.SimpleDateFormat
import java.util.*


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
        val config = RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build()
        realm = Realm.getInstance(config);
        //realm = Realm.getDefaultInstance();
        //realm.deleteAll();
        CalculateOfTimeButton.setOnClickListener {
            calculateOfTimeButtonListener();
        }


        val results = realm.where(DateRecord::class.java).findAll();
        val allRecords : MutableList<DateRecord> = mutableListOf();
        for(item:DateRecord in results){
            allRecords.add(item);
        }


    }

    override fun onDestroy() {
        super.onDestroy();
        realm.close();
    }

    fun addRecord(theBeginningOfTheDay: String, theEndingOfTheDay: String, date: String, weekNumber: Int){
        realm.executeTransaction { Realm.Transaction {
                fun execute(realm: Realm){
                    var record = realm.createObject(DateRecord::class.java);
                    record.TheBeginningOfDayTime = theBeginningOfTheDay;
                    record.TheEndingOfDayTime = theEndingOfTheDay;
                    record.Date = date;
                    record.WeekNumber = weekNumber;
                }
            }
        }
        Log.d("addRecords","Records Added");
    }

    fun calculateHoursForWeek(week:Int):Int{
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

                //TO BE CONTINUE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            }
        }
       return 1;
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

}

