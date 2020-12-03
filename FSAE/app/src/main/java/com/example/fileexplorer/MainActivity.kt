package com.example.fileexplorer

import FileChangeBroadcastReceiver
import FileModel
import FileType
import FileUtilsDeleteFile
import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.system.Os
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


// Sources :
// https://github.com/PhilJay/MPAndroidChart
// http://thetechnocafe.com/build-a-file-explorer-in-kotlin-part-1-introduction-and-set-up/
// https://stackoverflow.com/questions/11015833/getting-list-of-all-files-of-a-specific-type

//TODO prepare zip folder for test all function app
//TODO Error : first lauch app

//TODO choice name folder in setting

//TODO improve : managment PieChart, PieData, PieDataSet and legend (maybe DataSet in variable class)
//TODO improve : Fix warning !
//TODO imporve : SAME ? getCurrentPath =? backStackManager.top.path
//TODO imporve : SAME ? updateFileList =? updateContentOfCurrentFragment



//TO KNOW: PieChart's Legend can't to have more X label entries ( X = different colors (now 19))
//TO KNOW: when PieChart display with too much elements, its ugly (now max 10)
//TO KNOW: to put files in Android emulator, USE Terminal -> "adb push  <Your/path> /storage/emulated/0/<path>" ( Ex : "adb push D:\Master\S1\T-MobOp\Test_img /storage/emulated/0"

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
    // notify user
    override fun notifyUserOnPieChart(notif_ID: Int) {

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
            .setOnlyAlertOnce(true)
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
        fab.setOnClickListener { _ ->

            // go to mode Pie Chart
            if (viewFiles){

                // get current fileModel
                val fileDir = this.mBreadcrumbRecyclerAdapter.files[this.mBreadcrumbRecyclerAdapter.files.size - 1]

                // Get and check list files and size from current directory
                val path = fileDir.path
                val files = getFileModelsFromFiles(getFilesFromPath(path))
                if (files.isEmpty()){
                    notifyUserOnPieChart(1)
                    return@setOnClickListener
                } else if (fileDir.sizeInMB.equals(0.0) and !fileDir.name.equals("/")){
                    notifyUserOnPieChart(0)
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

    fun createSymLink(symLinkFilePath: String, originalFilePath: String): Boolean {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Os.symlink(originalFilePath, symLinkFilePath)
                return true
            }
            val libcore = Class.forName("libcore.io.Libcore")
            val fOs = libcore.getDeclaredField("os")
            fOs.isAccessible = true
            val os = fOs.get(null)
            val method = os.javaClass.getMethod("symlink", String::class.java, String::class.java)
            method.invoke(os, originalFilePath, symLinkFilePath)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    // Handle action bar item clicks here
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //TODO implement setting
            R.id.action_settings -> {
                true
            }

            // -- SEEK AND CLASS ALL IMAGE FILES
            R.id.action_seekAndClass -> {

                // Ask a confirmation
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setMessage("Are you sure you want to seek&class all image files ?")
                    .setCancelable(false)
                    .setPositiveButton("Yes") { dialog, _ ->
                        // seek and classify all image files
                        seekAndClassImageFiles()
                        dialog.dismiss()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        // Dismiss the dialog
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()


            }

            // -- CHANGE SORT DIRECTION --
            R.id.SortingAscending -> {
                if (!isEven(sortBy)) {
                    // display the choice direction
                    uncheckAllItemsOf(R.id.subMenuSortDirection)
                    item.setChecked(true)
                    // update list files
                    sortBy++
                    updateFileList()
                }
            }
            R.id.sortingDescending -> {
                if (isEven(sortBy)) {
                    // display the choice direction
                    uncheckAllItemsOf(R.id.subMenuSortDirection)
                    item.setChecked(true)
                    // update list files
                    sortBy--
                    updateFileList()
                }
            }

            // -- CHANGE SORT TYPE --
            R.id.SortingDefault -> {
                // display the choice type
                uncheckAllItemsOf(R.id.subMenuSortType)
                item.setChecked(true)

                if (isEven(sortBy))
                    sortBy = 0
                else
                    sortBy = -1
                updateFileList()
            }
            R.id.sortingSize -> {
                // display the choice type
                uncheckAllItemsOf(R.id.subMenuSortType)
                item.setChecked(true)

                if (isEven(sortBy))
                    sortBy = 2
                else
                    sortBy = 1
                updateFileList()
            }
            R.id.sortName -> {
                // display the choice type
                uncheckAllItemsOf(R.id.subMenuSortType)
                item.setChecked(true)

                if (isEven(sortBy))
                    sortBy = 4
                else
                    sortBy = 3
                updateFileList()
            }
            R.id.sortingExtension -> {
                // display the choice type
                uncheckAllItemsOf(R.id.subMenuSortType)
                item.setChecked(true)

                if (isEven(sortBy))
                    sortBy = 6
                else
                    sortBy = 5
                updateFileList()
            }
            R.id.sortingFileType -> {
                // display the choice type
                uncheckAllItemsOf(R.id.subMenuSortType)
                item.setChecked(true)

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

    private fun uncheckAllItemsOf(subMenuSortDirection: Int) {
        val submenu = menu.findItem(subMenuSortDirection).subMenu
        for (i in 0 until submenu.size()) {
            submenu.getItem(i).setChecked(false)
        }
    }

    private fun seekAndClassImageFiles() {


        // directory's name for class all image files
        val ourDirectoryName = "A_img_1"
        var seek_class = seekAndClassify(getCurrentPath(), ourDirectoryName, true)

        // seek all image files
        seek_class.seek()
        // get image files
        //seek_class.getAllImagesFiles()


        // do the progress notif in background
        Thread(Runnable {
            progressTask(seek_class)
        }).start()


        // Classify all files in background
        Thread(Runnable {
            classifyTask(seek_class)
        }).start()


    }

    private fun classifyTask(seek_class: seekAndClassify) {
        // Classify all files
        seek_class.classify()

        // update curent display
        updateContentOfCurrentFragment()

        // Get all stats of seek and classify
        val stats = seek_class.getStats()

        // Print all file name that have not been deciphered
        println("----------- Not deciphered : ")
        println(stats.listNameNoFound)

        // Print all file name that maybe can be deciphered
        println("----------- Maybe deciphered : ")
        println(stats.listNameMaybeFound)

        val printStat = seek_class.printStats()
        println("-----------printStat: ")
        println(printStat)
    }

    private fun progressTask(seek_class: seekAndClassify) {
        // Sets the maximum progress as 100
        val progressMax = seek_class.getAllImagesFiles().size
        // Creating a notification and setting its various attributes
        val notification =
            NotificationCompat.Builder(this, getString(R.string.channel_name_ID))
                .setSmallIcon(R.drawable.ic_notif_download)
                .setContentTitle("Seek & Classify all image files")
                .setContentText("Seeking and classify in progress")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress(progressMax, 0, true)


        // send first progress notif
        notification.setContentText(0.toString() + "%")
            .setProgress(progressMax, 0, false)
        with(NotificationManagerCompat.from(this)) {
            notify(5, notification.build())
        }

        var progress = 0
        while (progress < progressMax) {
            SystemClock.sleep(300)
            progress = seek_class.getProgress()

            // update progress notif
            notification.setContentText((progress * 100 / progressMax).toString() + "%")
                .setProgress(progressMax, progress, false)
            with(NotificationManagerCompat.from(this)) {
                notify(5, notification.build())
            }
        }

        // send last notif
        notification.setContentText("Task termined !")
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setTimeoutAfter(5000)
            .setSmallIcon(R.drawable.ic_notif_end)
        with(NotificationManagerCompat.from(this)) {
            notify(5, notification.build())
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



