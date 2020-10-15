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
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fileexplorer.fileslist.FilesListFragment
import com.example.fileexplorer.utils.BackStackManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import createNewFile
import createNewFolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_enter_name.view.*
import launchFileIntent


class MainActivity : AppCompatActivity(), FilesListFragment.OnItemClickListener {

    private val backStackManager = BackStackManager()
    private lateinit var mBreadcrumbRecyclerAdapter: BreadcrumbRecyclerAdapter

    companion object {
        private const val OPTIONS_DIALOG_TAG: String = "com.example.fileexplorer.options_dialog"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility.or(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        // Si les permissons ont déjà été acceptées
        if (setupPermissionsOK()){
            doInit()
        }

    }

    // Initilise l'acticité principales
    private fun doInit() {
        val filesListFragment = FilesListFragment.build {
            path = Environment.getExternalStorageDirectory().absolutePath
        }

        supportFragmentManager.beginTransaction()
            .add(R.id.container, filesListFragment)
            .addToBackStack(Environment.getExternalStorageDirectory().absolutePath)
            .commit()

        initViews()
        initBackStack()

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    // Setup les permissions
    // Renvoie True si toutes les permissons sont bonnes
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

    // Appeler lors de la réponse aux demanddes des permissons
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.size == 2 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED )
                {
                    doInit()
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                    finish()
                }
                return
            }
        }
    }

    // Check si toutes les permissons sont acceptées
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
        mBreadcrumbRecyclerAdapter.onItemClickListener = {
            supportFragmentManager.popBackStack(it.path, 2);
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

    private fun updateAdapterData(files: List<FileModel>) {
        mBreadcrumbRecyclerAdapter.updateData(files)
        if (files.isNotEmpty()) {
            breadcrumbRecyclerView.smoothScrollToPosition(files.size - 1)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
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



