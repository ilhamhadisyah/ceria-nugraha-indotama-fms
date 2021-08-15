/*
 * Copyright 2015 - 2021 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("DEPRECATION", "StaticFieldLeak")

package org.traccar.client.data.source.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.AsyncTask
import org.traccar.client.data.model.ActivityModel
import org.traccar.client.data.model.Position
import java.sql.Date

class DatabaseHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    interface DatabaseHandler<T> {
        fun onComplete(success: Boolean, result: T)
    }

    private abstract class DatabaseAsyncTask<T>(val handler: DatabaseHandler<T?>) :
        AsyncTask<Unit, Unit, T?>() {

        private var error: RuntimeException? = null

        override fun doInBackground(vararg params: Unit): T? {
            return try {
                executeMethod()
            } catch (error: RuntimeException) {
                this.error = error
                null
            }
        }

        protected abstract fun executeMethod(): T

        override fun onPostExecute(result: T?) {
            handler.onComplete(error == null, result)
        }
    }

    private val db: SQLiteDatabase = writableDatabase

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE position (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "deviceId TEXT," +
                    "time INTEGER," +
                    "latitude REAL," +
                    "longitude REAL," +
                    "altitude REAL," +
                    "speed REAL," +
                    "course REAL," +
                    "accuracy REAL," +
                    "battery REAL," +
                    "mock INTEGER)"
        )

        db.execSQL(
            "CREATE TABLE activities (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "loading_material TEXT," +
                    "activity_type TEXT," +
                    "imei TEXT," +
                    "action TEXT," +
                    "created_at TEXT," +
                    "session_parent_number INTEGER," +
                    "session_child_number INTEGER," +
                    "lat REAL," +
                    "long REAL," +
                    "status INTEGER)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS position;")
        db.execSQL("DROP TABLE IF EXISTS activities;")
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS position;")
        db.execSQL("DROP TABLE IF EXISTS activities;")
        onCreate(db)
    }

    private fun insertPosition(position: Position) {
        val values = ContentValues()
        values.put("deviceId", position.deviceId)
        values.put("time", position.time.time)
        values.put("latitude", position.latitude)
        values.put("longitude", position.longitude)
        values.put("altitude", position.altitude)
        values.put("speed", position.speed)
        values.put("course", position.course)
        values.put("accuracy", position.accuracy)
        values.put("battery", position.battery)
        values.put("mock", if (position.mock) 1 else 0)
        db.insertOrThrow("position", null, values)
    }
    fun insertPositionAsync(position: Position, handler: DatabaseHandler<Unit?>) {
        object : DatabaseAsyncTask<Unit>(handler) {
            override fun executeMethod() {
                insertPosition(position)
            }
        }.execute()
    }
    private fun insertActivity(model: ActivityModel) {
        val values = ContentValues()
        values.put("loading_material", model.loadingMaterial)
        values.put("activity_type", model.activityType)
        values.put("imei", model.imei)
        values.put("action", model.action)
        values.put("created_at", model.createdAt)
        values.put("session_parent_number", model.sessionParentNumber)
        values.put("session_child_number", model.sessionChildNumber)
        values.put("lat", model.lat)
        values.put("long", model.long)
        values.put("status", model.status)
        db.insertOrThrow("activities", null, values)
    }

    fun insertActivityAsync(activity: ActivityModel, handler: DatabaseHandler<Unit?>) {
        object : DatabaseAsyncTask<Unit>(handler) {
            override fun executeMethod() {
                insertActivity(activity)
            }
        }.execute()
    }

    private fun selectPosition(): Position? {
        db.rawQuery("SELECT * FROM position ORDER BY id LIMIT 1", null).use { cursor ->
            if (cursor.count > 0) {
                cursor.moveToFirst()
                return Position(
                    id = cursor.getLong(cursor.getColumnIndex("id")),
                    deviceId = cursor.getString(cursor.getColumnIndex("deviceId")),
                    time = Date(cursor.getLong(cursor.getColumnIndex("time"))),
                    latitude = cursor.getDouble(cursor.getColumnIndex("latitude")),
                    longitude = cursor.getDouble(cursor.getColumnIndex("longitude")),
                    altitude = cursor.getDouble(cursor.getColumnIndex("altitude")),
                    speed = cursor.getDouble(cursor.getColumnIndex("speed")),
                    course = cursor.getDouble(cursor.getColumnIndex("course")),
                    accuracy = cursor.getDouble(cursor.getColumnIndex("accuracy")),
                    battery = cursor.getDouble(cursor.getColumnIndex("battery")),
                    mock = cursor.getInt(cursor.getColumnIndex("mock")) > 0
                )
            }
        }
        return null
    }
    fun selectPositionAsync(handler: DatabaseHandler<Position?>) {
        object : DatabaseAsyncTask<Position?>(handler) {
            override fun executeMethod(): Position? {
                return selectPosition()
            }
        }.execute()
    }

    private fun getActivities(): ArrayList<ActivityModel>? {
        val listData: ArrayList<ActivityModel> = arrayListOf()
        db.rawQuery("SELECT * FROM activities WHERE status = 0 ORDER BY id ASC", null)
            .use { cursor ->
                while (cursor.moveToNext()) {
                    listData.add(
                        ActivityModel(
                            loadingMaterial = cursor.getString(cursor.getColumnIndex("loading_material")),
                            activityType = cursor.getString(cursor.getColumnIndex("activity_type")),
                            imei = cursor.getString(cursor.getColumnIndex("imei")),
                            action = cursor.getString(cursor.getColumnIndex("action")),
                            createdAt = cursor.getString(cursor.getColumnIndex("created_at")),
                            sessionParentNumber = cursor.getInt(cursor.getColumnIndex("session_parent_number")),
                            sessionChildNumber = cursor.getInt(cursor.getColumnIndex("session_child_number")),
                            lat = cursor.getDouble(cursor.getColumnIndex("lat")),
                            long = cursor.getDouble(cursor.getColumnIndex("long")),
                            status = cursor.getInt(cursor.getColumnIndex("status")),
                            activityId = cursor.getInt(cursor.getColumnIndex("id"))
                        )
                    )
                }
            }
        return listData
    }
    fun getActivitiesQueueAsync(handler: DatabaseHandler<ArrayList<ActivityModel>?>) {
        object : DatabaseAsyncTask<ArrayList<ActivityModel>?>(handler) {
            override fun executeMethod(): ArrayList<ActivityModel>? {
                return getActivities()
            }
        }.execute()
    }

    private fun getActivityLog(): ArrayList<ActivityModel>? {
        val listData: ArrayList<ActivityModel> = arrayListOf()
        db.rawQuery("SELECT * FROM activities", null)
            .use { cursor ->
                while (cursor.moveToNext()) {
                    listData.add(
                        ActivityModel(
                            loadingMaterial = cursor.getString(cursor.getColumnIndex("loading_material")),
                            activityType = cursor.getString(cursor.getColumnIndex("activity_type")),
                            imei = cursor.getString(cursor.getColumnIndex("imei")),
                            action = cursor.getString(cursor.getColumnIndex("action")),
                            createdAt = cursor.getString(cursor.getColumnIndex("created_at")),
                            sessionParentNumber = cursor.getInt(cursor.getColumnIndex("session_parent_number")),
                            sessionChildNumber = cursor.getInt(cursor.getColumnIndex("session_child_number")),
                            lat = cursor.getDouble(cursor.getColumnIndex("lat")),
                            long = cursor.getDouble(cursor.getColumnIndex("long")),
                            status = cursor.getInt(cursor.getColumnIndex("status")),
                            activityId = cursor.getInt(cursor.getColumnIndex("id"))
                        )
                    )
                }
            }
        return listData
    }
    fun getActivityLogAsync(handler: DatabaseHandler<ArrayList<ActivityModel>?>) {
        object : DatabaseAsyncTask<ArrayList<ActivityModel>?>(handler) {
            override fun executeMethod(): ArrayList<ActivityModel>? {
                return getActivityLog()
            }
        }.execute()
    }


    private fun updateActivity(activity: ActivityModel) {
        val values = ContentValues()
        values.put("loading_material", activity.loadingMaterial)
        values.put("activity_type", activity.activityType)
        values.put("imei", activity.imei)
        values.put("action", activity.action)
        values.put("created_at", activity.createdAt)
        values.put("session_parent_number", activity.sessionParentNumber)
        values.put("session_child_number", activity.sessionChildNumber)
        values.put("lat", activity.lat)
        values.put("long", activity.long)
        values.put("status", 1)
        db.update("activities",values,"id = ?", arrayOf("${activity.activityId}"))
    }
    fun updateActivityAsync(activity: ActivityModel, handler: DatabaseHandler<Unit?>) {
        object : DatabaseAsyncTask<Unit>(handler) {
            override fun executeMethod() {
                updateActivity(activity)
            }
        }.execute()
    }




    fun deletePosition(id: Long) {
        if (db.delete("position", "id = ?", arrayOf(id.toString())) != 1) {
            throw SQLException()
        }
    }

    fun deletePositionAsync(id: Long, handler: DatabaseHandler<Unit?>) {
        object : DatabaseAsyncTask<Unit>(handler) {
            override fun executeMethod() {
                deletePosition(id)
            }
        }.execute()
    }

    companion object {
        const val DATABASE_VERSION = 3
        const val DATABASE_NAME = "traccar.db"
    }

}
