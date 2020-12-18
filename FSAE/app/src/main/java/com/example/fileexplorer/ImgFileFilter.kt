package com.example.fileexplorer

import java.io.File
import java.io.FileFilter
import java.util.*

class ExtensionFileFilter(recursive: Boolean = false, DirNotCheck: String = "") : FileFilter {

    // all files found
    private var fountFiles = mutableListOf<File>()
    // all extensions file we want
    private var arrayExtension = mutableListOf<String>()

    // init extensions list
    init {
        addAllExtension(SupportedFileFormat.values())
    }


    /**
     * for recursive seek
     */
    private var allowDirectories = recursive

    private var DirectoryNotCheck = DirNotCheck


    fun getAllFilesFound(): MutableList<File> {
        return fountFiles
    }

    override fun accept(f: File): Boolean {
        if (f.isHidden || !f.canRead()) {
            return false
        }
        return if (f.isDirectory ) {
            if (!f.name.equals(DirectoryNotCheck)){
                return checkDirectory(f)
            } else
                return false

        } else checkFileExtension(f)
    }

    private fun checkFileExtension(f: File): Boolean {
        val ext = getFileExtension(f) ?: return false
        try {
            if (arrayExtension.contains(ext.toUpperCase())) {
                fountFiles.add(f)
                return true
            }
        } catch (e: IllegalArgumentException) {
            //Not known enum value
            return false
        }
        return false
    }

    private fun checkDirectory(dir: File): Boolean {
        return if (!allowDirectories) {
            false
        } else {
            val subDirs = ArrayList<File>()
            var NbFilesFound = dir.listFiles { file ->
                if (file.isFile) {
                    if (file.name.equals(".nomedia")) false else checkFileExtension(file)
                } else if (file.isDirectory) {
                    subDirs.add(file)
                    false
                } else false
            }?.size

            for (subDir in subDirs)
                checkDirectory(subDir)

            if (NbFilesFound!! > 0)
                return true

            false
        }
    }

    fun getFileExtension(f: File): String? {
        return getFileExtension(f.name)
    }

    fun getFileExtension(fileName: String): String? {
        val i = fileName.lastIndexOf('.')
        return if (i > 0) {
            fileName.substring(i + 1)
        } else null
    }



    /**
     * Bases Files formats currently supported
     */
    enum class SupportedFileFormat(val filesuffix: String) {
        JPG("jpg"), JPEG("jpeg"), PNG("png"),TIFF("tiff"), TIF("tif"), GIF("gif"),BMP("bmp"), JFIF("jfif")   ;
    }
    private fun addAllExtension(values: Array<ExtensionFileFilter.SupportedFileFormat>) {
        values.forEach { arrayExtension.add(it.toString().toUpperCase()) }
    }

    // Functions for change extensions set

    fun addExt(extension: String) {
        arrayExtension.add(extension.toUpperCase())
    }
    fun clearExt() {
        arrayExtension.clear()
    }
    fun removeExt(extension: String) {
        arrayExtension.remove(extension.toUpperCase())
    }

}
