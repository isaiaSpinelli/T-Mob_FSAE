package com.example.fileexplorer

import FileChangeBroadcastReceiver
import FileModel
import FileType
import FileUtilsDeleteFile
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


// Sources :
// https://github.com/PhilJay/MPAndroidChart
// http://thetechnocafe.com/build-a-file-explorer-in-kotlin-part-1-introduction-and-set-up/

//TODO CHANNEL_ID share betwenn MainActivity and PisChartFragment
//TODO Error first lauch app
//TODO Refactor gestion PieChart and legend (maybe DataSet in variable class)


//TODO add function to sort

//TO KNOW: PieChart's Legend can't to have more X label entries ( X = different colors (now 19))
//TO KNOW: when PieChart display with too much elements, its ugly (now max 10)

class MainActivity : AppCompatActivity(), FilesListFragment.OnItemClickListener, PieChartFragment.OnHeadlineSelectedListener  {
    lateinit var CHANNEL_ID: String

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

    // update the back stack from the PieChart
    override fun updateBackStack(fileModel: FileModel?) {
        if (fileModel != null) {
            backStackManager.addToStack(fileModel)
        }
    }

    companion object {
        private const val OPTIONS_DIALOG_TAG: String = "com.example.fileexplorer.options_dialog"
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
                    Toast.makeText(this, "Permission accepted, PLEASE RELAUCH APP", Toast.LENGTH_LONG).show()
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

            // return to mode explorer files
            } else {
                PieChartToFileList()

                var rootFile = FileModel(
                    Environment.getExternalStorageDirectory().absolutePath,
                    FileType.FOLDER,
                    "/",
                    0.0
                )

                displayFileListFrom(rootFile)

            }
        }
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
        // set visibility "add file/dir" menu
        menu.setGroupVisible(R.id.overFlowItemsToHide, viewFiles);
        // change button icon
        fab.setImageResource(R.drawable.ic_button_explorer);
    }
    // Switch view from Files list to PieChart
    private fun FileListToPieChart() {
        viewFiles = false
        // toggle visibility "add file/dir" menu
        menu.setGroupVisible(R.id.overFlowItemsToHide, viewFiles)
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
            R.id.action_settings -> true

            R.id.menuNewFile -> createNewFileInCurrentDirectory()
            R.id.menuNewFolder -> createNewFolderInCurrentDirectory()
            //else -> super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    // create channel for notification
    private fun createChannel(){
        CHANNEL_ID = "FSAE_channel_0"

        val notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val CHANNEL_ID = "FSAE_channel_0"
            val name: CharSequence = "FSAE channel"
            val Description = "This is channel for FSAE"
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


    // is a file
    // is emtpy
    private fun notifGo(noitf_ID: Int) {

        var title = "title"
        var text = "text"

        when(noitf_ID){
            0 -> {
                title = "Size null"
                text = "This folder content only files or folder empty"
            }

            1 -> {
                title = "Folder empty"
                text = "This folder is empty"
            }
        }


        // implementation "com.android.support:support-compat:28.0.0"
        // Build the notif
        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_button_explorer)
        .setContentTitle(title)
        .setContentText(text)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Display the notif
        with(NotificationManagerCompat.from(this)) {
            notify(noitf_ID, builder.build())
        }

    }

    // create a new File in current Dir
    private fun createNewFileInCurrentDirectory() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_enter_name, null)
        view.createButton.setOnClickListener {
            val fileName = view.nameEditText.text.toString()
            if (fileName.isNotEmpty()) {
                createNewFile(fileName, backStackManager.top.path) { _, message ->
                    bottomSheetDialog.dismiss()
                    //coordinatorLayout.createShortSnackbar(message)
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
                createNewFolder(fileName, backStackManager.top.path) { _, message ->
                    bottomSheetDialog.dismiss()
                    //coordinatorLayout.createShortSnackbar(message)
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



