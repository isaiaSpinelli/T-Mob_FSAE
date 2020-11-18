package com.example.fileexplorer

import FileChangeBroadcastReceiver
import FileModel
import FileType
import FileUtilsDeleteFile
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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

//TODO don t display BreadcrumbRecycler when lauch app fisrt time
//TODO Refactor gestion PieChart and legend (maybe DataSet in variable class)
//TODO when PieChart display with 0 element (add notif more clearly)
//TODO when PieChart display with elements empty

//TO KNOW: PieChart's Legend can't to have more X label entries ( X = different colors)
class MainActivity : AppCompatActivity(), FilesListFragment.OnItemClickListener, PieChartFragment.OnHeadlineSelectedListener  {

    private lateinit var filesListFragment: FilesListFragment
    private lateinit var menu: Menu
    private val backStackManager = BackStackManager()
    private lateinit var mBreadcrumbRecyclerAdapter: BreadcrumbRecyclerAdapter
    private var viewFiles = true

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

    // update the back stack
    override fun updateBackStack(fileModel: FileModel?) {
        if (fileModel != null) {
            backStackManager.addToStack(fileModel)
        }
    }

    companion object {
        private const val OPTIONS_DIALOG_TAG: String = "com.example.fileexplorer.options_dialog"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility.or(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Check permission
        if (setupPermissionsOK()){
            doInit()
        }

    }

    // Call upon get answer permissons
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                if (grantResults.size == 2 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED
                ) {
                    //TODO call here don t display BreadcrumbRecycler
                    doInit()
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                    finish()
                }
                return
            }
        }
    }

    // Init main activity
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
                FileListToPieChart()

                // Get list files and size from current directory
                val path = this.mBreadcrumbRecyclerAdapter.files[this.mBreadcrumbRecyclerAdapter.files.size - 1].path

                // send path to PieChart
                val bundle = Bundle()
                bundle.putString("path", path)


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

                // change fragment
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, filesListFragment).addToBackStack(Environment.getExternalStorageDirectory().absolutePath)
                    .commit()

                // update stack manager
                backStackManager.popFromStackTill(FileModel(
                    Environment.getExternalStorageDirectory().absolutePath,
                    FileType.FOLDER,
                    "/",
                    0.0
                ))
            }
        }
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

    private fun initViews() {
        setSupportActionBar(toolbar)
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

            // update files list of entry choice //supportFragmentManager.popBackStack(it.path, 2);
            filesListFragment = FilesListFragment.build {
                path = it.path
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, filesListFragment)
                .addToBackStack(it.path)
                .commit()

            // update stack top design
            backStackManager.popFromStackTill(it)

        }
    }

    private fun initBackStack() {
        backStackManager.onStackChangeListener = {
            updateAdapterData(it)
        }

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

    private fun updateAdapterData(files: List<FileModel>) {
        mBreadcrumbRecyclerAdapter.updateData(files)
        if (files.isNotEmpty()) {
            breadcrumbRecyclerView.smoothScrollToPosition(files.size - 1)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        this.menu = menu;
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            //TODO implement setting
            R.id.action_settings -> true
            R.id.menuNewFile -> createNewFileInCurrentDirectory()
            R.id.menuNewFolder -> createNewFolderInCurrentDirectory()
            //else -> super.onOptionsItemSelected(item)
        }

        return super.onOptionsItemSelected(item)
    }

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

    private fun updateContentOfCurrentFragment() {
        val broadcastIntent = Intent()
        broadcastIntent.action = applicationContext.getString(R.string.file_change_broadcast)
        broadcastIntent.putExtra(
            FileChangeBroadcastReceiver.EXTRA_PATH,
            backStackManager.top.path
        )
        sendBroadcast(broadcastIntent)
    }

    override fun onClick(fileModel: FileModel) {
        if (fileModel.fileType == FileType.FOLDER) {
            addFileFragment(fileModel)
        } else {
            launchFileIntent(fileModel)
        }
    }

    override fun onLongClick(fileModel: FileModel) {

        val optionsDialog = FileOptionsDialog.build {}

        optionsDialog.onDeleteClickListener = {
            FileUtilsDeleteFile(fileModel.path)
            updateContentOfCurrentFragment()
        }

        optionsDialog.show(supportFragmentManager, OPTIONS_DIALOG_TAG)
    }


    override fun onBackPressed() {
        super.onBackPressed()
        backStackManager.popFromStack()

        if (supportFragmentManager.backStackEntryCount == 0) {
            finish()
        }
    }

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

}



