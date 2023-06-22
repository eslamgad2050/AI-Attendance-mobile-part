package eslam.gad.attendenceapp;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

public class Camera_activity extends AppCompatActivity {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    boolean permission_granted = false;
    PreviewView previewView;
    String TAG = "camera_activity";
    boolean ready_to_bind = false, photo_ready = false;
    ImageCapture imageCapture;
    Button take_photo, send_photo;
    ImageView taken_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_camera);

        previewView = (PreviewView) findViewById(R.id.previewView);
        take_photo = (Button) findViewById(R.id.take_photo);
        send_photo = (Button) findViewById(R.id.send_photo);
        taken_image = (ImageView) findViewById(R.id.taken_image);

        get_permission();
        Log.d(TAG, "onCreate: " + this);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                ready_to_bind = true;
                Log.d(TAG, "onCreate: test " + this.getLifecycle().getCurrentState());
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
                Toast.makeText(this, "did not get the cam", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));

        Toast.makeText(this, this.getLifecycle().getCurrentState().toString(), Toast.LENGTH_SHORT).show();
        take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (photo_ready) {
                    photo_ready = false;
                    take_photo.setText("take photo");
                    taken_image.setVisibility(View.GONE);
                    previewView.setVisibility(View.VISIBLE);
                } else {
                    photo_ready = true;
                    Toast.makeText(Camera_activity.this, "send", Toast.LENGTH_SHORT).show();
                    take_photo.setText("cancel");
                    imageCapture.takePicture(ContextCompat.getMainExecutor(Camera_activity.this),
                            new ImageCapture.OnImageCapturedCallback() {
                                @Override
                                public void onCaptureSuccess(@NonNull ImageProxy image) {
                                    super.onCaptureSuccess(image);
                                    taken_image.setImageBitmap(toBitmap(image));
                                    Toast.makeText(Camera_activity.this, "photo have been taken", Toast.LENGTH_SHORT).show();
                                    taken_image.setVisibility(View.VISIBLE);
                                    previewView.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError(@NonNull ImageCaptureException exception) {
                                    super.onError(exception);

                                    Toast.makeText(Camera_activity.this, "error", Toast.LENGTH_SHORT).show();
                                }
                            }
                    );

                }
            }
        });

    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Log.d(TAG, "bindPreview: " + this);
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        Camera camera;

        imageCapture = new ImageCapture.Builder().setTargetRotation(Surface.ROTATION_0).build();

        if (this.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview);

    }

    private Bitmap toBitmap(ImageProxy image) {
        ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
        byteBuffer.rewind();
        byte[] bytes = new byte[byteBuffer.capacity()];
        byteBuffer.get(bytes);
        byte[] clonedBytes = bytes.clone();
        return BitmapFactory.decodeByteArray(clonedBytes, 0, clonedBytes.length);
    }

    void get_permission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.
                checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.
                PERMISSION_GRANTED) {
            ActivityResultLauncher<String[]> locationPermissionRequest =
                    registerForActivityResult(new ActivityResultContracts
                                    .RequestMultiplePermissions(), result -> {
                                Boolean fineLocationGranted = result.getOrDefault(
                                        CAMERA, false);
                                Boolean coarseLocationGranted = result.getOrDefault(
                                        WRITE_EXTERNAL_STORAGE, false);
                                if (fineLocationGranted != null && fineLocationGranted) {
                                    // Precise location access granted.
                                    permission_granted = true;
                                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                    // Only approximate location access granted.
                                } else {
                                    // No location access granted.
                                }
                            }
                    );
            locationPermissionRequest.launch(new String[]{
                    CAMERA,
                    WRITE_EXTERNAL_STORAGE
            });
        } else {
            permission_granted = true;
        }

    }
}