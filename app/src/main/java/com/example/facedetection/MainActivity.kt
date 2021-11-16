package com.example.facedetection

import android.content.Intent
import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import android.graphics.Bitmap
import android.os.Build
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi


class MainActivity : AppCompatActivity() {
    private val PIC_IMAGE = 111
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button2.setOnClickListener{
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Image"), PIC_IMAGE)

        }
//        val crashButton = Button(this)
//        crashButton.text = "Test Crash"
//        crashButton.setOnClickListener {
//            throw RuntimeException("Test Crash") // Force a crash
//        }
//
//        addContentView(crashButton, ViewGroup.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT))
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode === PIC_IMAGE) {
//            imageView2.setImageURI(data?.data!!)
            textView.setText("")

            val image: FirebaseVisionImage
            try {
//                image = FirebaseVisionImage.fromFilePath(applicationContext, data?.data!!)

                var bmp :Bitmap? = null
                    bmp = MediaStore.Images.Media.getBitmap(this.contentResolver,data?.data)
                val mutableBmp = bmp!!.copy(Bitmap.Config.ARGB_8888,true)

                val aspectRatio: Float = mutableBmp.getWidth() /
                        mutableBmp.getHeight().toFloat()
                val width = 480
                val height = Math.round(width / aspectRatio)
                val resizedBitmap = Bitmap.createScaledBitmap(
                    mutableBmp, width, height, false
                )
                imageView2.setImageBitmap(resizedBitmap)
                image = FirebaseVisionImage.fromBitmap(resizedBitmap)
                val canvas = Canvas(resizedBitmap)

                val highAccuracyOpts = FirebaseVisionFaceDetectorOptions.Builder()
                    .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                    .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                    .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                    .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                    .build()
                val detector = FirebaseVision.getInstance()
                    .getVisionFaceDetector(highAccuracyOpts)

                val result = detector.detectInImage(image)
                    .addOnSuccessListener { faces ->
                        // Task completed successfully
                        // ...
//      to recognize face contour

//                        faces.forEach{
//                            val contour = it.getContour(FirebaseVisionFaceContour.FACE)
//                            contour.points.forEach {
//                                Log.d("TagMc","Point at X ${it.x}, Y ${it.y}")
//                            }
//                            val canvas = Canvas(resizedBitmap)
//                            val mp = Paint(Paint.ANTI_ALIAS_FLAG)
//                            mp.color = Color.parseColor("#99ff0000")
//
//                            val path = Path()
//                            path.moveTo(contour.points[0].x, contour.points[0].y)
//                            contour.points.forEach {
//                                path.lineTo(it.x, it.y)
//                            }
//                            path.close()
//                            canvas.drawPath(path,mp)
//                            imageView2.setImageBitmap(resizedBitmap)
//                        }
                        for (face in faces) {
                            val bounds = face.boundingBox
                            val rotY = face.headEulerAngleY // Head is rotated to the right rotY degrees
                            val rotZ = face.headEulerAngleZ // Head is tilted sideways rotZ degrees

                            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                            // nose available)
                            var p : Paint = Paint()
                            p.color = Color.YELLOW
                            p.style = Paint.Style.STROKE
                            canvas.drawRect(bounds,p)
                            imageView2.setImageBitmap(resizedBitmap)
                            val leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR)
                            leftEar?.let{
                                val leftEarPos = leftEar.position
                                p.color = Color.BLUE
                                val rect = Rect(((leftEarPos.x - 20).toInt()), ((leftEarPos.y - 20).toInt()),
                                    ((leftEarPos.x + 20).toInt()), ((leftEarPos.y + 20).toInt()))
                                canvas.drawRect(rect,p)
                                imageView2.setImageBitmap(resizedBitmap)
                            }
                            val rightEar = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EAR)
                            rightEar?.let{
                                val rightEar = rightEar.position
                                p.color = Color.BLUE
                                val rect = Rect(((rightEar.x - 20).toInt()), ((rightEar.y - 20).toInt()),
                                    ((rightEar.x + 20).toInt()), ((rightEar.y + 20).toInt()))
                                canvas.drawRect(rect,p)
                                imageView2.setImageBitmap(resizedBitmap)
                            }

                            // If contour detection was enabled:
                            val leftEyeContour = face.getContour(FirebaseVisionFaceContour.LEFT_EYE).points
                            val upperLipBottomContour = face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).points

                            // If classification was enabled:
                            if (face.smilingProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                val smileProb = face.smilingProbability
                                var p2 = Paint()
                                    p2.color = Color.CYAN
                                    p2.textSize = 25f
                                Log.d("TagMc","Smile Proba ==> $smileProb")
                                if (smileProb > 0.6){
                                    canvas.drawText("Smiling", bounds.top.toFloat(),
                                        bounds.top.toFloat(),p2)
                                }else{
                                    canvas.drawText("Serious", bounds.top.toFloat(),
                                        bounds.top.toFloat(),p2)

                                }

                            }
                            if (face.rightEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                val rightEyeOpenProb = face.rightEyeOpenProbability
                                Log.d("TagMc","rightEyeOpenProbability==> $rightEyeOpenProb")
                            }

                            if (face.leftEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                val leftEyeOpenProb = face.leftEyeOpenProbability
                                Log.d("TagMc","leftEyeOpenProbability==> $leftEyeOpenProb")
                            }

                            // If face tracking was enabled:
                            if (face.trackingId != FirebaseVisionFace.INVALID_ID) {
                                val id = face.trackingId
                            }
                        }






                    }
                    .addOnFailureListener { e ->
                        // Task failed with an exception
                        // ...
                    }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}