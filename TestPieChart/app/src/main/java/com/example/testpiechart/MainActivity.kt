package com.example.testpiechart

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }



        // val barChart = findViewById(R.id.barchart) as BarChart

        var btnBarChart: Button? = null;
        btnBarChart = findViewById(R.id.btnBarChart);

        var btnPieChart:Button? = null;
        btnPieChart = findViewById(R.id.btnPieChart);


        btnBarChart.setOnClickListener {
            Toast.makeText(this, "btnBarChart Works", LENGTH_LONG).show()
/*
            val intent = Intent(this@MainActivity, BarChartActivity::class.java)
            intent.putExtra("key", "Kotlin")
            */


            val intent = Intent(this, BarChartActivity::class.java).apply {
                putExtra("key", "Kotlin")
            }



            startActivity(intent)
        }


        btnPieChart.setOnClickListener {
            //Toast.makeText(this, "btnPieChart Works", LENGTH_LONG).show()

            val intent = Intent(this@MainActivity, PieChartActivity::class.java)
            intent.putExtra("key", "Kotlin")
            var path = "/sd/images/"
            intent.putExtra("path", path )


            // ----- AJOUT DES DONNEES -----
            val listName =  ArrayList< String>()
            listName.add("images")
            listName.add("telechargement")
            listName.add("rep3")

            val listSize = ArrayList<Float>()
            listSize.add(95f)
            listSize.add(1040f)
            listSize.add(11303f)

            val SizeElement = 3;


            intent.putExtra("dataName", listName )
            intent.putExtra("dataSize", listSize )
            intent.putExtra("count", SizeElement)

            startActivity(intent)
            // finish()


        }


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        // Rend invisible le menu analyse
        menu.getItem(1).isVisible = false;
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                Toast.makeText(this, "settings action ! ", Toast.LENGTH_LONG).show()
                return true;
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // if retour

    // startActivityForResult(intent, SONG_INDEX_RESULT, null)

    // setResult(SONG_INDEX_RESULT, playerActivityIntent)

    // get Extra : https://stackoverflow.com/questions/3913592/start-an-activity-with-a-parameter
    // Get/set List Extra :  https://stackoverflow.com/questions/11340776/passing-a-list-from-one-activity-to-another

    /*
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == SONG_INDEX_RESULT) {
        songIndex = data!!.extras.getInt("song_index", 0)
        // or data.getIntExtra ("song_index", 0)
        }
    }
     */
}

