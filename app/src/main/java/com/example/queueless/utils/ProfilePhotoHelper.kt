package com.example.queueless.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import com.example.queueless.R
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream

object ProfilePhotoHelper {

    private fun getAvatarFile(context: Context): File {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        return File(context.filesDir, "profile_avatar_$uid.jpg")
    }

    fun saveAvatarFromUri(context: Context, uri: Uri): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return false
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (originalBitmap == null) return false

            val maxDim = 512
            val scale = minOf(
                maxDim.toFloat() / originalBitmap.width,
                maxDim.toFloat() / originalBitmap.height,
                1.0f
            )
            val targetW = (originalBitmap.width * scale).toInt().coerceAtLeast(1)
            val targetH = (originalBitmap.height * scale).toInt().coerceAtLeast(1)
            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, targetW, targetH, true)

            val file = getAvatarFile(context)
            FileOutputStream(file).use { out ->
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
            }
            if (resizedBitmap != originalBitmap) {
                originalBitmap.recycle()
            }
            resizedBitmap.recycle()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun loadAvatarInto(context: Context, imageView: ImageView) {
        val file = getAvatarFile(context)
        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
                return
            }
        }
        imageView.setImageResource(R.drawable.ic_default_avatar)
    }
}