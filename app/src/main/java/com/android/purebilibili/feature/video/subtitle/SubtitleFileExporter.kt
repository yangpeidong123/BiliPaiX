package com.android.purebilibili.feature.video.subtitle

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 字幕导出到公共 Downloads：`Download/BiliPai/subtitles/<fileName>`。
 *
 * Android 10+ 走 MediaStore（无需存储权限）；9 及以下直接写公共目录。
 * 纯 IO 操作挂 Dispatchers.IO，结果用 Toast 反馈——调用方无需自行处理线程。
 */
object SubtitleFileExporter {

    private const val SUBTITLE_RELATIVE_PATH = "Download/BiliPai/subtitles"
    private const val FALLBACK_DIR_NAME = "BiliPai/subtitles"

    suspend fun exportSubtitle(
        context: Context,
        fileName: String,
        content: String,
        format: SubtitleExportFormatter.SubtitleExportFormat,
    ) {
        val appContext = context.applicationContext
        val savedLocation = withContext(Dispatchers.IO) {
            runCatching { writeToPublicDownloads(appContext, fileName, content) }.getOrNull()
        }
        val message = if (savedLocation != null) {
            "字幕已导出：$savedLocation"
        } else {
            "导出失败，请检查存储空间"
        }
        Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun writeToPublicDownloads(
        context: Context,
        fileName: String,
        content: String,
    ): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(
                    MediaStore.Downloads.MIME_TYPE,
                    if (fileName.endsWith(".srt", ignoreCase = true)) {
                        "application/x-subrip"
                    } else {
                        "text/vtt"
                    },
                )
                put(MediaStore.Downloads.RELATIVE_PATH, SUBTITLE_RELATIVE_PATH)
            }
            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues,
            ) ?: return null
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(content.toByteArray())
            } ?: return null
            "$SUBTITLE_RELATIVE_PATH/$fileName"
        } else {
            @Suppress("DEPRECATION")
            val downloadDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS,
            )
            val targetDir = File(downloadDir, FALLBACK_DIR_NAME)
            if (!targetDir.exists() && !targetDir.mkdirs()) return null
            val target = File(targetDir, fileName)
            target.writeText(content)
            "${Environment.DIRECTORY_DOWNLOADS}/$FALLBACK_DIR_NAME/$fileName"
        }
    }
}
