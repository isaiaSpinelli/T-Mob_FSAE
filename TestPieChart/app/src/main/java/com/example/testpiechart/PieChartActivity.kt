package com.example.testpiechart

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.core.view.get
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate


// Doit connaitre plusieurs choses :
// 1. Le nom du repertoire (path) dans lequel on est
// 2. le noms de tous les repertoires qu'il y a (peu etre rechercher via le nom du repertoire (path))
// 3. La taille de tous les repertoires         (peu etre rechercher via le nom du repertoire (path))

// https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/main/java/com/xxmassdeveloper/mpchartexample/PieChartActivity.java
class PieChartActivity : AppCompatActivity(), OnChartValueSelectedListener {

    private var pieChart: PieChart? = null
    //private var data: PieData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pie_chart)

        var path = intent.getSerializableExtra("path")
        //Toast.makeText(this, "Path = "+ path, Toast.LENGTH_LONG).show()

        var count = intent.getIntExtra("count", 0)
        var listName = intent.getStringArrayListExtra("dataName")
        var listSize = intent.getStringArrayListExtra("dataSize")


        pieChart = findViewById(R.id.piechart) as PieChart

        // Ajoute les listener
        pieChart!!.setOnChartValueSelectedListener(this);

        // Offset de la légende
        pieChart!!.setExtraOffsets(0f, 0f, 0f, 0f);



        // ----- AJOUT DES DONNEES -----
        val NoOfEmp = mutableListOf<PieEntry>()


        NoOfEmp.add(PieEntry(95f, "images"))
        NoOfEmp.add(PieEntry(1040f, "telechargement"))
        NoOfEmp.add(PieEntry(11303f, "rep3"))
        NoOfEmp.add(PieEntry(1240f, "rep4"))
        NoOfEmp.add(PieEntry(1369f, "rep5"))
        NoOfEmp.add(PieEntry(1487f, "rep6"))
        NoOfEmp.add(PieEntry(1501f, "rep7"))
        NoOfEmp.add(PieEntry(1645f, "rep8"))
        NoOfEmp.add(PieEntry(1578f, "rep9"))
        NoOfEmp.add(PieEntry(1695f, "rep10"))
        // ajout les données et le label
        val dataSet = PieDataSet(NoOfEmp, "Name directory")
        // ajoute un nombre après la virgule
        dataSet.setValueFormatter(PercentFormatter())


        // Crée le PieData
        val data = PieData(dataSet)

        // ----- INFORMATIONS SUR LES LABEL DANS LE PIE -----
        // entry label styling
        pieChart!!.setEntryLabelColor(Color.BLACK);
        //pieChart.setEntryLabelTypeface(tfRegular);
        pieChart!!.setEntryLabelTextSize(20f);

        // ----- INFORMATIONS SUR LES VALEURS DANS LE PIE -----
        dataSet.sliceSpace = 3f
        dataSet.setSelectionShift(5f)

        data.setValueTextSize(25f)
        data.setValueTextColor(Color.BLACK)

        // Methode 1 : valeur le long d'une barre OU valeur dans la PIE (commenter)
        dataSet.setValueLinePart1OffsetPercentage(20f);
        dataSet.setValueLinePart1Length(0.1f);
        dataSet.setValueLinePart2Length(0.2f);
        dataSet.valueTextColor = Color.BLACK;
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        // Applique les données sur le Pie chart
        pieChart!!.setData(data)

        // Crée les couleurs a utiliser
        val colors: ArrayList<Int> = ArrayList()
        for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)
        // Applique les couleurs
        dataSet.setColors(colors)
        //dataSet.setColors(*ColorTemplate.COLORFUL_COLORS)




        // ----- INFORMATION SUR LE PIE CHART -----
        // Ajout une animation
        pieChart!!.animateXY(1500, 1500)
        // gère la taille (offset) du Pie chart
        pieChart!!.setExtraOffsets(30f, 0f, 30f, 0f);

        // Gère la description du PIE chart
        pieChart!!.getDescription().setEnabled(true);
        pieChart!!.getDescription().text = ("Size of directories");
        pieChart!!.getDescription().textSize = 25f;
        pieChart!!.description.xOffset = 0f
        pieChart!!.description.yOffset = 10f

        // Rend tournable
        pieChart!!.setRotationEnabled(true);
        // Rend clickable
        pieChart!!.setHighlightPerTapEnabled(true);

        // Par défaut, affiche en pourcentage
        pieChart!!.setUsePercentValues(true);

        // Gestion du texte du centre
        pieChart!!.setCenterText(generateCenterSpannableText());
        pieChart!!.setCenterTextSize(20f)


        // Gestion du trou du milieu
        pieChart!!.setHoleRadius(50f);
        pieChart!!.setTransparentCircleRadius(55f);


        // ----- GESTION DE LA LEGENDE -------

        val l: Legend = pieChart!!.getLegend()
        // Gère l'alignement
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        // Gère l'orientation
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);

        // Rend le label wrappable (retour à la ligne)
        l.setWordWrapEnabled(true);

        // Gère la forme des entrée
        //l.form = Legend.LegendForm.CIRCLE

        l.setDrawInside(false);

        // Escpace entre les lables
        l.setXEntrySpace(10f);
        //l.setYEntrySpace(10f);

        // offset de la légende
        l.setYOffset(10f);
        l.setXOffset(-18f);

        // Taille des icones de la légende
        l.setFormSize(18f);
        // Taille du texte de la légende
        l.setTextSize(18f);

        l.setEnabled(true);


    }

    // Gère le texte au centre du PIE chart
    private fun generateCenterSpannableText(): CharSequence? {
        //Toast.makeText(this, "generateCenterSpannableText fonction", Toast.LENGTH_LONG).show()

        var directoryName = "Image";

        val s = SpannableString("In directory\n"+directoryName)
        s.setSpan(RelativeSizeSpan(1.5f), 13, 13+directoryName.length, 0)
        s.setSpan(StyleSpan(Typeface.ITALIC), s.length - 5, s.length, 0)
        s.setSpan(ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length - 5, s.length, 0)
        return s
    }

    // si on clique pas sur une entrée ou si on déselectionne une entrée
    override fun onNothingSelected() {
        Toast.makeText(this, "onNothingSelected fonction", Toast.LENGTH_LONG).show()

        // Toggle X (labels) valeur
        pieChart?.setDrawEntryLabels(!pieChart!!.isDrawEntryLabelsEnabled());
        pieChart?.invalidate();

        // Toggle pourcent mode (data)
        pieChart?.setUsePercentValues(!pieChart!!.isUsePercentValuesEnabled());
        pieChart?.invalidate();
    }


    // Lorsqu'on clique sur une entrée (selectionne)
    override fun onValueSelected(e: Entry?, h: Highlight?) {
        // Recupère l'index de l'element selectionné
        var index = h!!.x;
        // Recuère la valeure de l'element (size)
        var size = h.y;

        Toast.makeText(this, "Index = "+index.toString()+"\t\tval = "+size.toString(), Toast.LENGTH_LONG).show()
        //Toast.makeText(this, "onValueSelected fonction\n"+e.toString()+"\n"+h.toString()+"\nindex = "+index.toString()+"val = "+size.toString(), Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.

        //menu.getItem(1).setEnabled(false);

        //menu.get(1).setEnabled(false)

        menuInflater.inflate(R.menu.menu_main, menu)

        // Rend invisible le menu Setting
        menu.getItem(0).isVisible = false;

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        when(item.itemId){
            R.id.action_analyse -> {
                Toast.makeText(this, "Analyse action ! ", Toast.LENGTH_LONG).show()
            }
            R.id.action_settings-> {
                Toast.makeText(this, "settings action ! ", Toast.LENGTH_LONG).show()
            }
            // Dans le cas ou on clique sur la fleche de retour
            else -> return super.onOptionsItemSelected(item);
        }

        return true;
    }



}
