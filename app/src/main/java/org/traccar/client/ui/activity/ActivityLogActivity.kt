package org.traccar.client.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import org.traccar.client.R
import org.traccar.client.data.model.ActivityModel
import org.traccar.client.data.source.sqlite.DatabaseHelper
import org.traccar.client.databinding.ActivityLogBinding
import java.util.ArrayList

class ActivityLogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogBinding
    private lateinit var adapter: LogAdapter
    private lateinit var database : DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init(){
        binding.rvLog.layoutManager = LinearLayoutManager(this)
        database = DatabaseHelper(this)
        database.getActivityLogAsync(object : DatabaseHelper.DatabaseHandler<ArrayList<ActivityModel>?>{
            override fun onComplete(success: Boolean, result: ArrayList<ActivityModel>?) {
                adapter = LogAdapter(result!!)
                binding.rvLog.adapter = adapter
            }
        })
    }
    }
