package com.example.fileexplorer

import createNewFolder
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*

//TODO remove decipher Time (HH:mm:ss) and comments not useful

class seekAndClassify(
    currentPath: String,
    ourDirectoryName: String = "images_Classify",
    recursive: Boolean = false
) {
    private var nbFiles: Int = 0

    // Save many stats for seek & classify
    private var stats = Stats()
    // All image files found
    private var imgFiles = mutableListOf<File>()

    /**
     * path to seek
     */
    private var seekPath = currentPath
    /**
     * for recursive seek
     */
    private var recursive = recursive

    /**
     * for directory classify
     */
    private var ourDirectoryName = ourDirectoryName


    // seek all images files (not in ourDirectoryName)
    fun seek() {
        var dir = File(seekPath)
        var imgFilter = ExtensionFileFilter(true, ourDirectoryName)
        dir.listFiles(imgFilter)
        imgFiles = imgFilter.getAllFilesFound()
        stats.allFound = imgFiles.size
    }

    fun getAllImagesFiles(): MutableList<File> {
        return imgFiles
    }


    // Classify all image files found
    fun classify() {

        nbFiles = 0

        // create a directory for classify all image files
        createNewFolder(ourDirectoryName, seekPath) { _, _ ->
            // Do something
        }

        // Initialize structure of stats
        initStats()

        // for each image file found
        for (file in imgFiles) {
            // get attribut's file
//            val attr = Files.readAttributes<BasicFileAttributes>(
//                file.toPath(),
//                BasicFileAttributes::class.java
//            )

            // get info from file

            var date = Date()
            val name = file.name
            //val size = attr.size().toDouble()
            //val extension = file.extension

            val path = file.absolutePath
            //val path2 = file.canonicalPath
            //val path3 = file.path
            //val path4 = file.toPath()


            // if name file include a date
            var startDate = getstartDate(name)
            if (startDate != -1) {
                // reduce the seek field

                var dateStr = ""

                try {
                    dateStr = name.substring(startDate, 15 + startDate)
                } catch (siobe: StringIndexOutOfBoundsException) {
                    dateStr = name.replace('_', '+')
                    dateStr = dateStr.replace('-', '*')
                }


                // WITCH KIND OF DATE NAME

                // with 4 dash like : Screenshot_2015-05-25-05-08-26
                if (dateStr.count { it == '-' } == 4) {

                    date = getDateFrome4Dash(name, startDate)

                // with -WA like : IMG-20200815-WA0010.jpg
                } else if (dateStr.contains("-WA")) {

                    date = getDateFromeWA(dateStr)

                // with - between date like : Screenshot_20190324-111621
                } else if (dateStr.contains('-') && dateStr.length == 15) {


                    date = getDateFromeOneDash(dateStr, '-')

                    // with _ between date like :  IMG_20190110_210549.jpg  or 20171016_183246.jpg or Screenshot_20190803_154322_com.whatsapp
                    // new : 20181229231833_picture.jpg
                } else if (dateStr.contains('_') && dateStr.length == 15) {

                    if (dateStr.get(dateStr.length-1)== '_' ) {
                        date = getDateFromeOneDashLast(dateStr, '_')
                    } else if (dateStr.get(8)== '_' ) {
                        date = getDateFromeOneDash(dateStr, '_')
                    }


                // a kind not yet knew
                } else {
                    stats.elseFile++
                    date = getLastModifOf(file)

                    // add file name maybe can deciphered
                    stats.listNameMaybeFound.add(name + "\n")
                }

            // if name file does not include the date (so take last modified)
            } else {
                stats.lastModified++
                date = getLastModifOf(file)

                // add file name without deciphered
                stats.listNameNoFound.add(name + "\n")
            }

            // add each image file in correct directory
            classifyFile(path, date, name)

            nbFiles++
        }

        // sort all image files with date not fount or kind not knew
        stats.listNameNoFound.sortBy { it }
        stats.listNameMaybeFound.sortBy { it }

        // remove duplicate
        stats.listNameNoFound = stats.listNameNoFound.distinct().toMutableList()
        stats.listNameMaybeFound = stats.listNameMaybeFound.distinct().toMutableList()

        // calcul number files with date deciphered
        stats.remains = stats.allFound - stats.lastModified
    }

    private fun getDateFromeOneDashLast(dateStr: String, char: Char): Date {
        // add for stats
        if (char == '-')
            stats.dash_h++
        else if (char == '_')
            stats.dash_L++

        // get date from name
        var form = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .parseLenient()
            .appendValue(ChronoField.YEAR, 4)
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .appendLiteral(char)

        var localTime = LocalDateTime.parse(dateStr, form.toFormatter())

        return Date.from(
            localTime.atZone(ZoneId.systemDefault())
                .toInstant()
        )
    }

    private fun getDateFromeOneDash(dateStr: String, char: Char): Date {
        // add for stats
        if (char == '-')
            stats.dash_h++
        else if (char == '_')
            stats.dash_L++

        // get date from name
        var form = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .parseLenient()
            .appendValue(ChronoField.YEAR, 4)
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral(char)
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)

        var localTime = LocalDateTime.parse(dateStr, form.toFormatter())

        return Date.from(
            localTime.atZone(ZoneId.systemDefault())
                .toInstant()
        )
    }

    private fun getDateFromeWA(nameDateStr: String): Date {
        // add for stats
        stats.wa++

        // get date from name
        var dateStr = nameDateStr.substring(0, 8)
        var form = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .parseLenient()
            .appendValue(ChronoField.YEAR, 4)
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendValue(ChronoField.DAY_OF_MONTH, 2)

        var localTime = LocalDate.parse(dateStr, form.toFormatter())

        return Date.from(localTime.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }

    private fun getDateFrome4Dash(nameFile: String, startDate: Int): Date {
        // add for stats
        stats.dash_4++

        // get date from name
        var form = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .parseLenient()
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral('-')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral('-')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral('-')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)

        var dateStr = nameFile.substring(startDate, 19 + startDate)
        var localTime = LocalDateTime.parse(dateStr, form.toFormatter())
        return Date.from(
            localTime.atZone(ZoneId.systemDefault())
                .toInstant()
        )
    }

    // class file with overwrite (kill duplicate)
    private fun classifyFile(path: String, date: Date, name: String) {
        val datePath = DateToPath(date)

        // copy the image file to our directory create
        var fileImgFrom = File(path)
        val linkPath = seekPath + '/' + ourDirectoryName + '/' + datePath + name
        var fileImgTo = File(linkPath)


        try {
            //createSymLink(linkPath, originalImage) // Symbolic link impossible, Only in /data directory
            fileImgFrom.copyTo(fileImgTo, true)
        } catch (e : IOException) {
            // Error - Ex : no place storage
            println(e)
        }
    }

    // Get date from last modifed
    private fun getLastModifOf(file: File): Date {
        // TODO check if always the same
        val lastModified = file.lastModified()
        //val l1 =  attr.lastModifiedTime()
        //val l2 =  attr.lastAccessTime()

        return Date(lastModified)
    }


    // convert a date to path
    private fun DateToPath(date: Date): Any {
        val year1 = date.year + 1900
        val year = String.format(
            "%04d/",
            year1
        )

        val month_date = SimpleDateFormat("MMM")
        val month_name: String = month_date.format(date.getTime())
        val month = String.format("%02d_%s/", date.month + 1, month_name)


        var localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val day = String.format("%02d/", localDate.getDayOfMonth())


        return year+month+day
    }

    // determines the type of file name
    private fun getstartDate(name: String): Int {

        // IMG_20190110_210549.jpg // IMG-20200815-WA0010.jpg
        if ( name.startsWith("IMG")){
            return 4
            // 20171016_183246.jpg
        } else if (name.startsWith("19") or name.startsWith("20")){
            return 0
            // Screenshot_2015-05-25-05-08-26  / Screenshot_20190324-111621  / Screenshot_20190803_154322_com.whatsapp
        } else if (name.startsWith("Screenshot_")){
            return 11
        } else {
            return -1
        }
    }


    // init le struct Stats
    private fun initStats() {
        // counter for stats
        stats.dash_4 = 0
        stats.wa = 0
        stats.dash_h= 0
        stats.dash_L= 0
        stats.lastModified= 0
        stats.elseFile = 0

        // for save image file name not deciphered
        stats.listNameNoFound.clear()
        // for save image file name maybe can deciphered
        stats.listNameMaybeFound.clear()
    }
    fun getStats(): Stats {
        return stats
    }
    // Display stats
    fun printStats(): String {
        return "Found = " + stats.allFound + "\n\t" +"Last M = " + stats.lastModified +"\n\t" +"Other = " + stats.remains +"\n\t" +
                "Dash_4 = " + stats.dash_4 + "\n\t" +"-WA = " + stats.wa +"\n\t" + "Dash_h = " + stats.dash_h + "\n\t" +"Dash_l = " +
                stats.dash_L + "\n\t" + "Else = " + stats.elseFile
    }

    // indicate how many files is done yet
    fun getProgress(): Int {
        return nbFiles
    }

}

// Allows to know more about the processus to seek image files
data class Stats(
    var allFound: Int = 0,
    var lastModified: Int = 0,
    var remains: Int = 0,
    var dash_4: Int = 0,
    var dash_h: Int = 0,
    var dash_L: Int = 0,
    var wa: Int = 0,
    var elseFile: Int = 0,
    var listNameMaybeFound: MutableList<String> = mutableListOf<String>(),
    var listNameNoFound: MutableList<String> = mutableListOf<String>()
)
