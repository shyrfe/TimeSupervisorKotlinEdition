package com.example.vshurygin.timesupervisorkotlinedition

import io.realm.RealmObject
import io.realm.annotations.RealmClass


/**
 * Created by vshurygin on 13.01.2017.
 */
@RealmClass
open class DateRecord(
        open var TheBeginningOfDayTime:String = "",
        open var TheEndingOfDayTime:String = "",
        open var Date:String = "",
        open var WeekNumber:Int = 1
): RealmObject(){}