package it.polito.uniteam.gui.userprofile

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import android.graphics.Matrix
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import it.polito.uniteam.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun CameraView(
    vm: UserProfileScreen,
    outputDirectory: File,
    executor: Executor,
    onError: (ImageCaptureException) -> Unit
) {
    // 1
    val lensFacing = if(vm.isFrontCamera) { CameraSelector.LENS_FACING_FRONT } else { CameraSelector.LENS_FACING_BACK}
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }
    val imageCapture: ImageCapture = remember { ImageCapture.Builder().build() }
    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()

    // 2
    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )

        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    // 3
    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
        val configuration = LocalConfiguration.current
        IconButton(
            modifier = Modifier.padding(bottom = 20.dp).size(60.dp),
            onClick = {
                Log.i("kilo", "ON CLICK")
                takePhoto(
                    filenameFormat = "yyyy-MM-dd-HH-mm-ss-SSS",
                    imageCapture = imageCapture,
                    outputDirectory = outputDirectory,
                    executor = executor,
                    onImageCaptured = vm::handleImageCapture,
                    onError = onError,
                    flip = vm.isFrontCamera,
                    configuration = configuration
                )
            }) {
            Icon(
                painter = painterResource(R.drawable.camera),
                contentDescription = "Take picture",
                tint = Color.White,
                modifier = Modifier
                    .padding(1.dp)
                    .scale(0.8f)
            )
        }

        IconButton(
            modifier = Modifier.align(Alignment.TopStart).padding(start = 16.dp, top = 16.dp).size(60.dp),
            onClick = {
                Log.i("kilo", "ON CLICK")
                vm.setIsFrontCamera(!vm.isFrontCamera)
            }) {
            Icon(
                painter = painterResource(R.drawable.change_camera),
                contentDescription = "Change Camera",
                tint = Color.White,
                modifier = Modifier
                    .padding(1.dp)
                    .size(50.dp)
            )
        }
    }
}

private fun takePhoto(
    filenameFormat: String,
    imageCapture: ImageCapture,
    outputDirectory: File,
    executor: Executor,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit,
    flip: Boolean,
    configuration: Configuration
) {

    val photoFile = File(
        outputDirectory,
        SimpleDateFormat(filenameFormat, Locale.US).format(System.currentTimeMillis()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(outputOptions, executor, object: ImageCapture.OnImageSavedCallback {
        override fun onError(exception: ImageCaptureException) {
            Log.e("kilo", "Take photo error:", exception)
            onError(exception)
        }

        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            val screenWidthDp = configuration.screenWidthDp.dp
            val screenHeightDp = configuration.screenHeightDp.dp

            val savedUri = Uri.fromFile(photoFile)
            if (flip) {
                // Flip the image horizontally
                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                val matrix = Matrix().apply {
                    postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
                    if (screenHeightDp >= screenWidthDp) {
                        postRotate(90f)
                    }
                }
                val flippedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                // Save the flipped bitmap
                FileOutputStream(photoFile).use { outputStream ->
                    flippedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
            }
            // Notify the caller with the flipped image URI
            onImageCaptured(savedUri)
        }
    })
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { cameraProvider ->
        cameraProvider.addListener({
            continuation.resume(cameraProvider.get())
        }, ContextCompat.getMainExecutor(this))
    }
}