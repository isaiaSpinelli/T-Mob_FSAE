package com.example.fileexplorer

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
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


class PieChartFragment : Fragment(), OnChartValueSelectedListener {

    //private var mListener: Nothing?
    private var pieChart: PieChart? = null

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

        pieChart = view.findViewById(com.example.fileexplorer.R.id.piechart)

        // Ajoute les listener
        pieChart!!.setOnChartValueSelectedListener(this);

        // Offset de la légende
        pieChart!!.setExtraOffsets(0f, 0f, 0f, 0f);

        // get arguments (name et size )
        val listNameFiles = arguments!!.getStringArrayList("listNameFiles")
        val listSizeFiles = arguments!!.getFloatArray("listSizeFiles")

        if ( listNameFiles.isNullOrEmpty() ){
            //TODO
            Toast.makeText(this.context, "There isn't files or directory here", Toast.LENGTH_LONG).show()
        }

        // ----- AJOUT DES DONNEES -----
        val NoOfEmp = mutableListOf<PieEntry>()

        var i = 0;
        for (item in listNameFiles!!) {
            NoOfEmp.add(PieEntry(listSizeFiles!!.get(i++), item))
        }


        // ajout les données et le label
        var dataSet = PieDataSet(NoOfEmp, "Name directory")
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
        // nombers of colors = max nombers entries (now = 5+5+4+5 = 19)
        val colors: ArrayList<Int> = ArrayList()
        for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)  // 5
        for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)    // 5
        for (c in ColorTemplate.MATERIAL_COLORS) colors.add(c)  // 4
        for (c in ColorTemplate.VORDIPLOM_COLORS) colors.add(c) // 5
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
        pieChart!!.description.xOffset = 60f
        pieChart!!.description.yOffset = 10f

        // Rend tournable
        pieChart!!.setRotationEnabled(true);
        // Rend clickable
        pieChart!!.setHighlightPerTapEnabled(true);

        // Par défaut, affiche en pourcentage
        pieChart!!.setUsePercentValues(true);
        // n'affiche pas les lables
        pieChart!!.setDrawEntryLabels(false)

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

        return view;
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialisation des variables
        this.retainInstance = true

    }

    // Gère le texte au centre du PIE chart
    private fun generateCenterSpannableText(): CharSequence? {
        //Toast.makeText(this, "generateCenterSpannableText fonction", Toast.LENGTH_LONG).show()


        var s = SpannableString("[%]")

        val text = pieChart!!.centerText.toString()
        if (text.equals("[%]")){
            s = SpannableString("[MB]")
        }

        s.setSpan(RelativeSizeSpan(2f), 0, s.length, 0)

        // For display current Directory :
        //var directoryName = "Image";
        //val s = SpannableString("In directory\n" + directoryName)
        //s.setSpan(RelativeSizeSpan(1.5f), 13, 13 + directoryName.length, 0)
        //s.setSpan(RelativeSizeSpan(1.5f), 0, 4, 0)
        //s.setSpan(StyleSpan(Typeface.ITALIC), s.length - 5, s.length, 0)
        //s.setSpan(ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length - 5, s.length, 0)
        return s
    }

    companion object {

        fun newInstance(): PieChartFragment {
            return PieChartFragment()
        }
    }



    // call upon the fragment is ready and displayed
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }
    // Call upon the fragment is link with activity
    override fun onAttach(activity: Context) {
        super.onAttach(activity)

        try {
            //mListener = activity as OnFragmentInteractionListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                activity.toString() +
                        " must implement onFragmentInteractionListener"
            )
        }
    }
    override fun onDetach() {
        super.onDetach()
        //mListener = null
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        // Recupère l'index de l'element selectionné
        var index = h!!.x;
        // Recuère la valeure de l'element (size)
        var size = h.y;

        Toast.makeText(this.context, "Index = "+index.toString()+"\t\tval = "+size.toString(), Toast.LENGTH_LONG).show()
        //Toast.makeText(this, "onValueSelected fonction\n"+e.toString()+"\n"+h.toString()+"\nindex = "+index.toString()+"val = "+size.toString(), Toast.LENGTH_LONG).show()

    }

    override fun onNothingSelected() {
        //Toast.makeText(this, "onNothingSelected fonction", Toast.LENGTH_LONG).show()

        // Toggle X (labels) valeur
        //pieChart?.setDrawEntryLabels(!pieChart!!.isDrawEntryLabelsEnabled());
        //pieChart?.invalidate();

        // Toggle pourcent mode (data)
        pieChart?.setUsePercentValues(!pieChart!!.isUsePercentValuesEnabled());
        pieChart?.invalidate();

        pieChart!!.setCenterText(generateCenterSpannableText());

    }

}