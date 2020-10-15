package com.example.testpiechart

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import java.io.File
import android.provider.DocumentsProvider as DocumentsProvider1


class BarChartActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bar_chart)

        val barchart =  findViewById(R.id.barchart) as BarChart


        println("\n\n--------------------------\n\n")
        File("/vendor").walk().forEach { println(" "+ it.absolutePath + " " + it.length() + " " + it.isDirectory)}
        println("\n\n--------------------------\n\n")
        File("/vendor").list().forEach { println(" "+ it + " " + it.length )}
        println("\n\n--------------------------\n\n")
        File("/vendor").listFiles().forEach { println(" "+ it + " " + it.totalSpace + " " + it.freeSpace + " " + it.absolutePath + " " + it.usableSpace + " " + it.length() + " " + it.isDirectory) }
        println("\n\n--------------------------\n\n")
        File("/vendor").walkTopDown().forEach { println(" "+ it + " " + it.totalSpace + " " + it.freeSpace + " " + it.absolutePath + " " + it.usableSpace + " " + it.length() + " " + it.isDirectory) }
        println("\n\n--------------------------\n\n")

        /*

        val NoOfEmp = mutableListOf<BarEntry>()

        NoOfEmp.add(BarEntry(945f, 0f))
        NoOfEmp.add(BarEntry(1040f, 1f))
        NoOfEmp.add(BarEntry(1133f, 2f))
        NoOfEmp.add(BarEntry(1240f, 3f))
        NoOfEmp.add(BarEntry(1369f, 4f))
        NoOfEmp.add(BarEntry(1487f, 5f))
        NoOfEmp.add(BarEntry(1501f, 6f))
        NoOfEmp.add(BarEntry(1645f, 7f))
        NoOfEmp.add(BarEntry(1578f, 8f))
        NoOfEmp.add(BarEntry(1695f, 9f))

        val year = ArrayList<Any>()

        year.add("2008")
        year.add("2009")
        year.add("2010")
        year.add("2011")
        year.add("2012")
        year.add("2013")
        year.add("2014")
        year.add("2015")
        year.add("2016")
        year.add("2017")

        val bardataset = BarDataSet(NoOfEmp, "No Of Employee")
        barchart.animateY(5000)

        val yearDataSet = BarDataSet(NoOfEmp, "No Of Employee")
        val data = BarData(yearDataSet, bardataset)

        bardataset.setColors(*ColorTemplate.COLORFUL_COLORS)
        barchart.setData(data)*/


    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 777) {
            val filePath = data?.data?.path
            Toast.makeText(this, "filePath : " + filePath, Toast.LENGTH_LONG).show()
        }
    }


}


