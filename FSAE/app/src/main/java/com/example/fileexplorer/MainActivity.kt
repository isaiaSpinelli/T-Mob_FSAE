package com.example.fileexplorer

import FileChangeBroadcastReceiver
import FileModel
import FileType
import FileUtilsDeleteFile
import ImageFileModel
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fileexplorer.fileslist.FilesListFragment
import com.example.fileexplorer.utils.BackStackManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import createNewFile
import createNewFolder
import getFileModelsFromFiles
import getFilesFromPath
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_enter_name.view.*
import launchFileIntent
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.ResolverStyle
import java.time.temporal.ChronoField
import java.util.*


// Sources :
// https://github.com/PhilJay/MPAndroidChart
// http://thetechnocafe.com/build-a-file-explorer-in-kotlin-part-1-introduction-and-set-up/
// https://stackoverflow.com/questions/11015833/getting-list-of-all-files-of-a-specific-type

//TODO Error : first lauch app

//TODO add : function for seek and classe all photos

//TODO improve : managment PieChart, PieData, PieDataSet and legend (maybe DataSet in variable class)
//TODO improve : Fix warning !
//TODO improve : icon notification change to warning



//TO KNOW: PieChart's Legend can't to have more X label entries ( X = different colors (now 19))
//TO KNOW: when PieChart display with too much elements, its ugly (now max 10)
//TO KNOW: pass file to emulated -> adb push  D:\Master\S1\T-MobOp\Test_img /storage/emulated/0

class MainActivity : AppCompatActivity(), FilesListFragment.OnItemClickListener, PieChartFragment.OnHeadlineSelectedListener  {
    // TODO delete
    private var notAllowed = true
    private lateinit var filesListFragment: FilesListFragment
    private lateinit var menu: Menu
    private val backStackManager = BackStackManager()
    private lateinit var mBreadcrumbRecyclerAdapter: BreadcrumbRecyclerAdapter
    private var viewFiles = true

    // Link callbacks for PieChart Fragment
    override fun onAttachFragment(fragment: Fragment) {
        if (fragment is PieChartFragment) {
            fragment.setOnHeadlineSelectedListener(this)
        }
    }

    // Get files sorted for a path of entry selected
    override fun onArticleSelected(path: String): List<FileModel> {
        val files = getFileModelsFromFiles(getFilesFromPath(path))
        // sort files
        val filesSort = files.sortedByDescending { it.sizeInMB }

        return filesSort

    }

    // is a file
    // is emtpy
    override fun notifGo(notif_ID: Int) {

        var title = "title"
        var text = "text"

        when(notif_ID){
            0 -> {
                title = "Size null"
                text = "This folder content only files or folder empty"
            }
            1 -> {
                title = "Folder empty"
                text = "This folder is empty"
            }
            2 -> {
                title = "Too many entries"
                text = "Entries are limited to 10"
            }
        }

        // Build the notif
        var builder = NotificationCompat.Builder(this, getString(R.string.channel_name_ID))
            .setSmallIcon(R.drawable.ic_button_explorer)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setTimeoutAfter(1000)

        // Display the notif
        with(NotificationManagerCompat.from(this)) {
            notify(notif_ID, builder.build())
        }

    }

    // update the back stack from the PieChart
    override fun updateBackStack(fileModel: FileModel?) {
        if (fileModel != null) {
            backStackManager.addToStack(fileModel)
        }
    }

    companion object {
        private const val OPTIONS_DIALOG_TAG: String = "com.example.fileexplorer.options_dialog"
        public var sortBy = 0
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility.or(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Check permission
        notAllowed = !setupPermissionsOK()

        if (!notAllowed){
            createChannel()
            doInit()
        } else {
            // TODO error for first lauch app
            // launch a new coroutine in background and continue, allow to catch answer for permissons
//            GlobalScope.launch {
//                var counter = 0
//                // wait while permission accepted
//                while(notAllowed){
//                    delay(100)
//                    counter++
//                    if (counter >= 50)
//                        return@launch
//                }
//                doInit()
//            }
        }


    }

    // Call upon get answer permissons
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                // Permisson accepted
                if (grantResults.size == 2 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED
                ) {
                    notAllowed = false

                    // TODO error for first lauch app
                    //doInit()
                    Toast.makeText(
                        this,
                        "Permission accepted, PLEASE RELAUCH APP",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()


                    // Permisson denied
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                    finish()
                }
                return
            }
        }
    }

    // Initialize main activity
    private fun doInit() {

        // create and display file list fragment
        filesListFragment = FilesListFragment.build {
            path = Environment.getExternalStorageDirectory().absolutePath
        }
        supportFragmentManager.beginTransaction()
            .add(R.id.container, filesListFragment)
            .addToBackStack(Environment.getExternalStorageDirectory().absolutePath)
            .commit()

        initViews()
        initBackStack()

        // press on flotting button (change mode : Explorer or PieChart)
        fab.setOnClickListener { view ->

            // go to mode Pie Chart
            if (viewFiles){

                // get current fileModel
                val fileDir = this.mBreadcrumbRecyclerAdapter.files[this.mBreadcrumbRecyclerAdapter.files.size - 1]

                // Get and check list files and size from current directory
                val path = fileDir.path
                val files = getFileModelsFromFiles(getFilesFromPath(path))
                if (files.isEmpty()){
                    notifGo(1)
                    return@setOnClickListener
                } else if (fileDir.sizeInMB.equals(0.0) and !fileDir.name.equals("/")){
                    notifGo(0)
                    return@setOnClickListener
                }

                goToPieChartMode(path)



            // return to mode explorer files
            } else {

                var rootFile = FileModel(
                    Environment.getExternalStorageDirectory().absolutePath,
                    FileType.FOLDER,
                    "/",
                    0.0
                )

                goToFileListMode(rootFile)
            }
        }
    }

    private fun goToFileListMode(rootFile: FileModel) {
        PieChartToFileList()
        displayFileListFrom(rootFile)
    }

    private fun goToPieChartMode(path: String) {
        // send path to PieChart
        val bundle = Bundle()
        bundle.putString("path", path)

        FileListToPieChart()

        // Enable mode PieChart
        val PieChartFragment = PieChartFragment()
        PieChartFragment.setArguments(bundle)

        // change fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, PieChartFragment).addToBackStack("test")
            .commit()
    }

    // display the list of files from a FileModel
    private fun displayFileListFrom(fileFrom: FileModel) {
        // update files list
        filesListFragment = FilesListFragment.build {
            path = fileFrom.path
        }

        // change fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, filesListFragment).addToBackStack(fileFrom.path)
            .commit()

        // update stack manager
        backStackManager.popFromStackTill(fileFrom)
    }
    // add a depth in current directory
    private fun addFileFragment(fileModel: FileModel) {
        val filesListFragment = FilesListFragment.build {
            path = fileModel.path
        }

        backStackManager.addToStack(fileModel)

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, filesListFragment)
        fragmentTransaction.addToBackStack(fileModel.path)
        fragmentTransaction.commit()
    }


    // Setup permissons
    // return true if all permissons are accepts
    private fun setupPermissionsOK(): Boolean {
        var ret = true;
        val PERMISSION_ALL = 1
        val PERMISSIONS = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (!hasPermissions(this, *PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL)
            ret = false
        }
        return ret
    }



    // Check if all permissons are accept
    fun hasPermissions(context: Context, vararg permissions: String): Boolean =
        permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    // initialize view
    private fun initViews() {
        setSupportActionBar(toolbar)
        // initialize breadcrumb
        breadcrumbRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mBreadcrumbRecyclerAdapter = BreadcrumbRecyclerAdapter()
        breadcrumbRecyclerView.adapter = mBreadcrumbRecyclerAdapter

        // Click on item to BreadCrumb
        mBreadcrumbRecyclerAdapter.onItemClickListener = {

            // if we need to update desgin
           if (viewFiles == false){
               PieChartToFileList()
            }
            displayFileListFrom(it)
        }
    }

    // initialize back stack
    private fun initBackStack() {

        // for update bach stack
        backStackManager.onStackChangeListener = {
            updateAdapterData(it)
        }


        // initialize back stack with root directory
        backStackManager.addToStack(
            fileModel = FileModel(
                Environment.getExternalStorageDirectory().absolutePath,
                FileType.FOLDER,
                "/",
                0.0
            )
        )

    }

    // Switch view from PieChart to Files list
    private fun PieChartToFileList() {
        viewFiles = true
        // toggle visibility "add file/dir" menu
        menu.setGroupVisible(R.id.overFlowItemsToHide, viewFiles)
        // toggle visibility "sort" menu
        menu.setGroupVisible(R.id.SortFile, viewFiles)
        // change button icon
        fab.setImageResource(R.drawable.ic_button_explorer)
    }
    // Switch view from Files list to PieChart
    private fun FileListToPieChart() {
        viewFiles = false
        // toggle visibility "add file/dir" menu
        menu.setGroupVisible(R.id.overFlowItemsToHide, viewFiles)
        // toggle visibility "sort" menu
        menu.setGroupVisible(R.id.SortFile, viewFiles)
        // change button icon
        fab.setImageResource(R.drawable.ic_button_piechart);
    }

    // update data bread crumb
    private fun updateAdapterData(files: List<FileModel>) {
        mBreadcrumbRecyclerAdapter.updateData(files)
        if (files.isNotEmpty()) {
            breadcrumbRecyclerView.smoothScrollToPosition(files.size - 1)
        }
    }

    // Initialize menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        this.menu = menu;
        return true
    }

    // Handle action bar item clicks here
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //TODO implement setting
            R.id.action_settings -> {

                var dir = File(getCurrentPath())

                // Get all images files
                var imgFilter = ExtensionFileFilter(true)
                dir.listFiles(imgFilter)
                val imgFiles = imgFilter.getAllFilesFound()


                var i = 0
                for (file in imgFiles) {
                    // get attribut's file
                    val attr = Files.readAttributes<BasicFileAttributes>(
                        file.toPath(),
                        BasicFileAttributes::class.java
                    )

                    // get data from file
                    var date = Date()
                    val name = file.name
                    val size = attr.size().toDouble()
                    val extension = file.extension

                    val path = file.absolutePath
                    //val abs = file.absolutePath
                    //val re = file.canonicalPath
                    //val pa = file.path
                    //val pa1 = file.toPath()

                    // Get date of image file

                    // if name file include the date
                    var startDate = getstartDate(name)
                    if (startDate != -1) { // IMG_20190110_210549.jpg
                        val dateStr = name.substring(startDate, 15 + startDate)

                        var form = DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .parseLenient()
                            .appendValue(ChronoField.YEAR, 4)
                            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                            .appendValue(ChronoField.DAY_OF_MONTH, 2)


                        // TODO Test not alpha
                        var charSepate: Char
                        if (dateStr.none { it !in 'A'..'Z' && it !in 'a'..'z' }) {
                            charSepate = '-'
                        }else if  (dateStr.matches("[a-zA-Z]+".toRegex())){
                            charSepate = '-'
                        }
                        else if (dateStr.contains('-') ){
                            charSepate = '-'
                            form.appendLiteral(charSepate)
                                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                        } else if (dateStr.contains('_')){
                            charSepate = '_'
                            form.appendLiteral(charSepate)
                                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                        } else {
                            continue
                        }





                        var localTime = LocalDateTime.parse(dateStr, form.toFormatter())

                        date = Date
                            .from(
                                localTime.atZone(ZoneId.systemDefault())
                                    .toInstant()
                            );


                        val test12 = 3

                        // if name file does not include the date (so take last modified)
                    } else {

                        val lastModified = file.lastModified()
                        //val l1 =  attr.lastModifiedTime()
                        //val l2 =  attr.lastAccessTime()

                        date = Date(lastModified)
                    }

                    // get & add image file
                    val imageFile = ImageFileModel(
                        path, name, size, extension, date
                    )


                    i++


                }


                Toast.makeText(
                    this,
                    "found =" + imgFiles.size,
                    Toast.LENGTH_LONG
                ).show()


            }

            // -- CHANGE SORT DIRECTION --
            R.id.SortingAscending -> {
                if (!isEven(sortBy)) {
                    sortBy++
                    updateFileList()
                }
            }
            R.id.sortingDescending -> {
                if (isEven(sortBy)) {
                    sortBy--
                    updateFileList()
                }
            }

            // -- CHANGE SORT TYPE --
            R.id.SortingDefault -> {
                if (isEven(sortBy))
                    sortBy = 0
                else
                    sortBy = -1
                updateFileList()
            }
            R.id.sortingSize -> {
                if (isEven(sortBy))
                    sortBy = 2
                else
                    sortBy = 1
                updateFileList()
            }
            R.id.sortName -> {
                if (isEven(sortBy))
                    sortBy = 4
                else
                    sortBy = 3
                updateFileList()
            }
            R.id.sortingExtension -> {
                if (isEven(sortBy))
                    sortBy = 6
                else
                    sortBy = 5
                updateFileList()
            }
            R.id.sortingFileType -> {
                if (isEven(sortBy))
                    sortBy = 8
                else
                    sortBy = 7
                updateFileList()
            }

            // -- CREATE NEW ITEM --
            R.id.menuNewFile -> createNewFileInCurrentDirectory()
            R.id.menuNewFolder -> createNewFolderInCurrentDirectory()
            //else -> super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getstartDate(name: String): Int {
        if ( name.startsWith("IMG")){
            return 4
        } else if (name.startsWith("19") or name.startsWith("20")){
            return 0
        } else {
            return -1
        }
    }

    private fun getCurrentPath(): String {
        return this.mBreadcrumbRecyclerAdapter.files[this.mBreadcrumbRecyclerAdapter.files.size - 1].path
    }

    // Allow to know if the sort is descending or ascending
    private fun isEven(sortBy: Int): Boolean {
        return (sortBy % 2 == 0)
    }

    // update the files list for change sortby
    private fun updateFileList() {
        var pathNow = this.mBreadcrumbRecyclerAdapter.files[this.mBreadcrumbRecyclerAdapter.files.size - 1].path
        val filesListFragment = FilesListFragment.build {
            path = pathNow
        }

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, filesListFragment)
        fragmentTransaction.addToBackStack(pathNow)
        fragmentTransaction.commit()
    }

    // create channel for notification
    private fun createChannel(){

        val notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val CHANNEL_ID = getString(R.string.channel_name_ID)
            val name: CharSequence = getString(R.string.channel_name)
            val Description = getString(R.string.channel_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = Description
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            mChannel.setShowBadge(false)
            notificationManager.createNotificationChannel(mChannel)
        }
    }




    // create a new File in current Dir
    private fun createNewFileInCurrentDirectory() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_enter_name, null)
        view.createButton.setOnClickListener {
            val fileName = view.nameEditText.text.toString()
            if (fileName.isNotEmpty()) {
                createNewFile(fileName, backStackManager.top.path) { _, _ ->
                    bottomSheetDialog.dismiss()
                    updateContentOfCurrentFragment()
                }
            }
        }
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    // create a new folder in current Dir
    private fun createNewFolderInCurrentDirectory() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_enter_name, null)
        view.createButton.setOnClickListener {
            val fileName = view.nameEditText.text.toString()
            if (fileName.isNotEmpty()) {
                createNewFolder(fileName, backStackManager.top.path) { _, _ ->
                    bottomSheetDialog.dismiss()
                    updateContentOfCurrentFragment()
                }
            }
        }
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    // update content of fragment
    private fun updateContentOfCurrentFragment() {
        val broadcastIntent = Intent()
        broadcastIntent.action = applicationContext.getString(R.string.file_change_broadcast)
        broadcastIntent.putExtra(
            FileChangeBroadcastReceiver.EXTRA_PATH,
            backStackManager.top.path
        )
        sendBroadcast(broadcastIntent)
    }

    // Click on file/filder list
    override fun onClick(fileModel: FileModel) {
        if (fileModel.fileType == FileType.FOLDER) {
            addFileFragment(fileModel)
        } else {
            launchFileIntent(fileModel)
        }
    }

    // lonf click on file/filder list
    override fun onLongClick(fileModel: FileModel) {
        // ask for delete
        val optionsDialog = FileOptionsDialog.build {}
        optionsDialog.onDeleteClickListener = {
            FileUtilsDeleteFile(fileModel.path)
            updateContentOfCurrentFragment()
        }
        optionsDialog.show(supportFragmentManager, OPTIONS_DIALOG_TAG)
    }


    // click back
    override fun onBackPressed() {
        super.onBackPressed()
        backStackManager.popFromStack()

        if (supportFragmentManager.backStackEntryCount == 0) {
            finish()
        }
    }



}



