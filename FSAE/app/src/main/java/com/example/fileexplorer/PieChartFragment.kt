package com.example.fileexplorer

import FileModel
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.listener.PieRadarChartTouchListener
import com.github.mikephil.charting.utils.ColorTemplate


class PieChartFragment : Fragment(), OnChartValueSelectedListener {
    // max number entry (if more 20 => add color in legend)
    private val MAX_ELEMENT = 11
    private var path: String = ""
    private var filesList: List<FileModel>? = null
    private var pieChart: PieChart? = null
    internal lateinit var callback: OnHeadlineSelectedListener

    // allow to link callback with activity
    fun setOnHeadlineSelectedListener(callback: OnHeadlineSelectedListener) {
        this.callback = callback
    }

    // interface implemented by Activity
    interface OnHeadlineSelectedListener {
        fun onArticleSelected(path: String): List<FileModel>
        fun updateBackStack(fileModel: FileModel?)
        fun notifyUserOnPieChart(notif_ID: Int)
    }

    // TODO delete
    companion object {
        fun newInstance(): PieChartFragment {
            return PieChartFragment()
        }
    }

    // Call upon the fragment is link with activity (1)
    override fun onAttach(activity: Context) {
        super.onAttach(activity)
        //Toast.makeText(activity, "onAttach", Toast.LENGTH_SHORT).show()

    }
    // (2)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.retainInstance = true
    }
    // (3)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            com.example.fileexplorer.R.layout.fragment_pie_chart,
            container,
            false
        )
        // link with layout
        pieChart = view.findViewById(com.example.fileexplorer.R.id.piechart)
        // add listener
        pieChart!!.setOnChartValueSelectedListener(this);

        // get path of current directory
        path = arguments!!.getString("path", "/") // /storage/emulated/0/
        // get files list and apply
        initDataPieChar( callback.onArticleSelected(path) )

        // when the PieChart is clicked
        pieChart!!.onTouchListener = object : PieRadarChartTouchListener(pieChart){
            override fun onSingleTapUp(e: MotionEvent?): Boolean {

                // Toggle pourcent mode
                pieChart?.setUsePercentValues(!pieChart!!.isUsePercentValuesEnabled());
                pieChart?.invalidate();

                // update center text
                pieChart!!.setCenterText(generateCenterSpannableText());

                // if a entry is clieck, call onValueSelected
                super.onSingleTapUp(e)
                return super.onSingleTapUp(e)
            }
        }


        return view;
    }
    // call upon the fragment is ready and displayed (4)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //Toast.makeText(activity, "onActivityCreated", Toast.LENGTH_SHORT).show()
    }
    // TODO delete
    // call upon the fragment is detached
    override fun onDetach() {
        super.onDetach()
        //Toast.makeText(activity, "onDetach", Toast.LENGTH_SHORT).show()
    }

    // Gère le texte au centre du PIE chart
    private fun generateCenterSpannableText(): CharSequence? {
        // Display % or MB
        var s : SpannableString
        if ( !pieChart!!.isUsePercentValuesEnabled)
            s = SpannableString("[MB]")
        else
            s = SpannableString("[%]")

        s.setSpan(RelativeSizeSpan(2f), 0, s.length, 0)

        // Display current Directory :
        //var directoryName = "Image";
        //val s = SpannableString("In directory\n" + directoryName)
        //s.setSpan(RelativeSizeSpan(1.5f), 13, 13 + directoryName.length, 0)
        //s.setSpan(RelativeSizeSpan(1.5f), 0, 4, 0)
        //s.setSpan(StyleSpan(Typeface.ITALIC), s.length - 5, s.length, 0)
        //s.setSpan(ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length - 5, s.length, 0)
        return s
    }

    // when a entry is selected
    override fun onValueSelected(e: Entry?, h: Highlight?) {

        // Recupère l'index de l'element selectionné
        var index = h!!.x;
        // Recuère la valeure de l'element (size)
        var size = h.y;


        // if selected a folder and it isn't emtpy
        if (filesList?.get(index.toInt())!!.fileType.equals(FileType.FOLDER) and !size.equals(0.0)) {
            // get path
            path = filesList?.get(index.toInt())!!.path // path + "/storage/emulated/0/DCIM"
            // update stack files
            callback.updateBackStack(filesList?.get(index.toInt()))
            // update data PieCHart
            initDataPieChar( callback.onArticleSelected(path) )
            // update center text
            //pieChart!!.setCenterText(generateCenterSpannableText());
        }

    }

    // TODO delete
    override fun onNothingSelected() {
        // Toggle X (labels) valeur
        //pieChart?.setDrawEntryLabels(!pieChart!!.isDrawEntryLabelsEnabled());
        //pieChart?.invalidate();
    }

    // init data to Pia Chart
    private fun initDataPieChar(onArticleSelected: List<FileModel>) {
        // test limits entries
        if (onArticleSelected.size >= MAX_ELEMENT ){
            filesList = onArticleSelected.subList(0,MAX_ELEMENT-1)
            // Send a notif to warn
            callback.notifyUserOnPieChart(2)
        } else {
            filesList = onArticleSelected
        }

        // Display only not empty entries (min = 0.05 MB)
        filesList = filesList!!.filter { it.sizeInMB >= 0.05  }

        // Applique les données sur le Pie chart
        pieChart!!.setData(setupData())

        // ----- INFORMATION SUR LE PIE CHART -----
        setupPieChart();

        // ----- GESTION DE LA LEGENDE -------
        setupLegend();

    }

    // ---- LIE LES DONNEE ---
    private fun setupData(): PieData? {
        // Crée les data pour le Pie Chart
        val data = PieData(setupDataSet())
        // configure la couleur et la taille du texte des valeurs
        data.setValueTextSize(25f)
        data.setValueTextColor(Color.BLACK)
        return data
    }

    // ----- AJOUT DES DONNEES -----
    private fun setupDataSet(): PieDataSet? {

        val NoOfEmp = mutableListOf<PieEntry>()

        for (file in filesList!!){
            NoOfEmp.add(PieEntry(file.sizeInMB.toFloat(), file.name))
        }

        // ajout les données et le label
        var dataSet = PieDataSet(NoOfEmp, "Name directory")

        // ----- INFORMATIONS SUR LES VALEURS DANS LE PIE -----
        dataSet.sliceSpace = 3f
        dataSet.setSelectionShift(5f)

        // ajoute un nombre après la virgule
        dataSet.setValueFormatter(PercentFormatter())

        // Valeur le long d'une barre
        dataSet.setValueLinePart1OffsetPercentage(20f);
        dataSet.setValueLinePart1Length(0.1f);
        dataSet.setValueLinePart2Length(0.2f);
        dataSet.valueTextColor = Color.BLACK;
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);


        // Applique les couleurs
        dataSet.setColors(setupColors())

        return dataSet
    }

    // ---- ADD COLORS FOR ENTRIES ---
    private fun setupColors(): ArrayList<Int> {
        // Crée les couleurs a utiliser
        // nombers of colors = max nombers entries (now = 5+5+4+5 = 19)
        val colors: ArrayList<Int> = ArrayList()
        for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)  // 5
        for (c in ColorTemplate.VORDIPLOM_COLORS) colors.add(c) // 5
        for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)    // 5
        for (c in ColorTemplate.MATERIAL_COLORS) colors.add(c)  // 4
        return colors
    }

    // ----- INFORMATION SUR LE PIE CHART -----
    private fun setupPieChart(){

        // Offset de la légende
        pieChart!!.setExtraOffsets(0f, 0f, 0f, 0f);

        // ----- INFORMATIONS SUR LES LABEL DANS LE PIE -----
        // entry label styling
        pieChart!!.setEntryLabelColor(Color.BLACK);
        //pieChart.setEntryLabelTypeface(tfRegular);
        pieChart!!.setEntryLabelTextSize(20f);


        // Ajout une animation
        pieChart!!.animateXY(1500, 1500)
        // gère la taille (offset) du Pie chart
        pieChart!!.setExtraOffsets(30f, -25f, 30f, 0f);

        // Gère la description du PIE chart
        pieChart!!.getDescription().setEnabled(true);
        pieChart!!.getDescription().text = ("Size of directories");
        pieChart!!.getDescription().textSize = 20f;
        pieChart!!.description.xOffset = 60f
        pieChart!!.description.yOffset = 3f

        // Rend tournable
        pieChart!!.setRotationEnabled(true);
        // Rend clickable
        pieChart!!.setHighlightPerTapEnabled(true);

        // Par défaut, affiche en pourcentage
        pieChart!!.setUsePercentValues(true);
        // n'affiche pas les lables des data sur le chart
        pieChart!!.setDrawEntryLabels(false)

        // Gestion du texte du centre
        pieChart!!.setCenterText(generateCenterSpannableText());
        pieChart!!.setCenterTextSize(20f)


        // Gestion du trou du milieu
        pieChart!!.setHoleRadius(50f);
        pieChart!!.setTransparentCircleRadius(55f);
    }

    // ----- GESTION DE LA LEGENDE -------
    private fun setupLegend(){

        val l: Legend = pieChart!!.getLegend()
        // Gère l'alignement
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
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
        //l.setYOffset(50f);
        l.setXOffset(-50f);

        // Taille des icones de la légende
        l.setFormSize(18f);
        // Taille du texte de la légende
        l.setTextSize(18f);

        l.setEnabled(true);

    }
}