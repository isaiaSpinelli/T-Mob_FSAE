import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

fun getFileModelsFromFiles(files: List<File>): List<FileModel> {

    return files.map {
        FileModel(it.path, FileType.getFileType(it), it.name,

                if(FileType.getFileType(it) != FileType.FOLDER){
                    convertFileSizeToMB(it.length())
                }else{
                    convertFileSizeToMB(getFolderSize(it))
                },

                it.extension, it.listFiles()?.size
                ?: 0)
    }
}

fun getFolderSize(directory: File): Long {
    var size_length = 0.toLong()
    for (file in directory.listFiles()) {
        if (file.isFile) size_length += file.length() else size_length += getFolderSize(file)
    }
    return size_length
}
fun getFilesFromPath(path: String, showHiddenFiles: Boolean = false, onlyFolders: Boolean = false): List<File> {
    val file = File(path)
    return file.listFiles()
            .filter { showHiddenFiles || !it.name.startsWith(".") }
            .filter { !onlyFolders || it.isDirectory }
            .toList()
}

fun convertFileSizeToMB(sizeInBytes: Long): Double {
    return (sizeInBytes.toDouble()) / (1024 * 1024)
}


fun Context.launchFileIntent(fileModel: FileModel) {
    val intent = Intent(Intent.ACTION_VIEW)

    intent.data = FileProvider.getUriForFile(this, packageName, File(fileModel.path))
    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    startActivity(Intent.createChooser(intent, "Select Application"))

}

fun createNewFile(fileName: String, path: String, callback: (result: Boolean, message: String) -> Unit) {
    val fileAlreadyExists = File(path).listFiles().map { it.name }.contains(fileName)
    if (fileAlreadyExists) {
        callback(false, "'${fileName}' already exists.")
    } else {
        val file = File(path, fileName)
        try {
            val result = file.createNewFile()
            if (result) {
                callback(result, "File '${fileName}' created successfully.")
            } else {
                callback(result, "Unable to create file '${fileName}'.")
            }
        } catch (e: Exception) {
            callback(false, "Unable to create file. Please try again.")
            e.printStackTrace()
        }
    }
}

fun createNewFolder(folderName: String, path: String, callback: (result: Boolean, message: String) -> Unit) {
    val folderAlreadyExists = File(path).listFiles().map { it.name }.contains(folderName)
    if (folderAlreadyExists) {
        callback(false, "'${folderName}' already exists.")
    } else {
        val file = File(path, folderName)
        try {
            val result = file.mkdir()
            if (result) {
                callback(result, "Folder '${folderName}' created successfully.")
            } else {
                callback(result, "Unable to create folder '${folderName}'.")
            }
        } catch (e: Exception) {
            callback(false, "Unable to create folder. Please try again.")
            e.printStackTrace()
        }
    }
}

fun FileUtilsDeleteFile(path: String) {
    val file = File(path)
    if (file.isDirectory) {
        file.deleteRecursively()
    } else {
        file.delete()
    }
}