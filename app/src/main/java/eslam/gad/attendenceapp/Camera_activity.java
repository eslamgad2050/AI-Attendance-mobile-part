package eslam.gad.attendenceapp;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class Camera_activity extends AppCompatActivity {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    String TAG = "camera_activity";
    boolean ready_to_bind = false, photo_ready = false, permission_granted = false, in_or_out;
    ImageCapture imageCapture;
    Button take_photo, send_photo, retry;
    ImageView taken_image, image_done;
    TextView text_done;
    Bitmap image_bitmap;
    long national_id;
    private ManagedChannel channel;
    Location location;
    boolean do_retry = true;
    ConstraintLayout main_layout, assigning_layout, registered_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_camera);
        getSupportActionBar().hide();

        Intent intent = getIntent();
        in_or_out = intent.getBooleanExtra("in_or_out", true);
        double[] x = intent.getDoubleArrayExtra("location");
        location = new Location("dummyprovider");
        location.setLatitude(x[0]);
        location.setLongitude(x[1]);

        previewView = (PreviewView) findViewById(R.id.previewView);
        take_photo = (Button) findViewById(R.id.take_photo);
        send_photo = (Button) findViewById(R.id.send_photo);
        taken_image = (ImageView) findViewById(R.id.taken_image);
        main_layout = (ConstraintLayout) findViewById(R.id.main_constraint_layout);
        assigning_layout = (ConstraintLayout) findViewById(R.id.assigning_layout);
        registered_layout = (ConstraintLayout) findViewById(R.id.registered_layout);
        image_done = (ImageView) findViewById(R.id.image_done);
        text_done = (TextView) findViewById(R.id.text_done);
        retry = (Button) findViewById(R.id.retry);

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
//                Toast.makeText(this, "did not get the cam", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));

        national_id = getIntent().getLongExtra("national_id", 0);
        take_photo.setOnClickListener(take_photo_listener);

        send_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check_grpc();
            }
        });
        retry.setOnClickListener(retry_listener);
    }

    View.OnClickListener take_photo_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (photo_ready) {
                photo_ready = false;
                take_photo.setText("take photo");
                take_photo.setBackgroundResource(R.drawable.round_button);
                taken_image.setVisibility(View.GONE);
                send_photo.setVisibility(View.GONE);
                previewView.setVisibility(View.VISIBLE);
            } else {
                photo_ready = true;
                take_photo.setText("cancel");
                take_photo.setBackgroundResource(R.drawable.cancel_button);
                send_photo.setVisibility(View.VISIBLE);
                ImageCapture.OutputFileOptions outputFileOptions =
                        new ImageCapture.OutputFileOptions.Builder(new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "face_image.jpg")).build();
                image_bitmap = previewView.getBitmap();
                taken_image.setImageBitmap(image_bitmap);
                taken_image.setVisibility(View.VISIBLE);
                previewView.setVisibility(View.GONE);
            }
        }
    };

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Log.d(TAG, "bindPreview: " + this);
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        Camera camera;
        imageCapture = new ImageCapture.Builder().setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();
        if (this.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview);

    }


    void get_permission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.
                checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.
                PERMISSION_GRANTED && ActivityCompat.
                checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.
                PERMISSION_GRANTED) {
            ActivityResultLauncher<String[]> locationPermissionRequest =
                    registerForActivityResult(new ActivityResultContracts
                                    .RequestMultiplePermissions(), result -> {
                                Boolean fineLocationGranted = result.getOrDefault(
                                        CAMERA, false);
                                Boolean coarseLocationGranted = result.getOrDefault(
                                        WRITE_EXTERNAL_STORAGE, false);
                                Boolean read_image = result.getOrDefault(
                                        READ_EXTERNAL_STORAGE, false);
                                if (read_image && fineLocationGranted != null && fineLocationGranted) {
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

    void check_grpc() {
        main_layout.setVisibility(View.GONE);
        registered_layout.setVisibility(View.GONE);
        assigning_layout.setVisibility(View.VISIBLE);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                channel = ManagedChannelBuilder.forAddress("172.31.47.225", 50051).usePlaintext().build();
                FaceIdAiGrpc.FaceIdAiBlockingStub stub = FaceIdAiGrpc.newBlockingStub(channel);
                FaceIdAiData.EmployeeId employeeId = FaceIdAiData.EmployeeId.newBuilder().setId(national_id).build();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image_bitmap.compress(Bitmap.CompressFormat.WEBP, 100, stream);
                byte[] byteArray = stream.toByteArray();
                ByteString byte_string = ByteString.copyFrom(byteArray);
                FaceIdAiData.Image image = FaceIdAiData.Image.newBuilder().setEncodedImage(byte_string).build();
                FaceIdAiData.EmployeeInfo employeeInfo = FaceIdAiData.EmployeeInfo.newBuilder().setImage(image).setId(employeeId).build();
                FaceIdAiData.RemoteEmployee remoteEmployee = FaceIdAiData.RemoteEmployee.newBuilder().setLatitude(location.getLatitude())
                        .setLongitude(location.getLongitude()).setInOrOut(in_or_out).setEmployeeInfo(employeeInfo).build();
                try {
                    do_retry = true;
                    retry.setText("Retry");
                    FaceIdAiData.RemoteEmployeeState remoteEmployeeState = stub.withDeadlineAfter(20, TimeUnit.SECONDS).registerRemoteEmployee(remoteEmployee);
                    if (remoteEmployeeState.getState() == FaceIdAiData.RemoteEmployeeState.State.STATE_SUCCESS) {
                        do_retry = false;
                        retry.setText("OK");
                        registered_layout.setVisibility(View.VISIBLE);
                        main_layout.setVisibility(View.GONE);
                        assigning_layout.setVisibility(View.GONE);
                    } else if (remoteEmployeeState.getState() == FaceIdAiData.RemoteEmployeeState.State.STATE_ERROR) {
                        image_done.setImageResource(R.drawable.baseline_error_24);
                        text_done.setText("Error Happened");
                        registered_layout.setVisibility(View.VISIBLE);
                        main_layout.setVisibility(View.GONE);
                        assigning_layout.setVisibility(View.GONE);

                    } else if (remoteEmployeeState.getState() == FaceIdAiData.RemoteEmployeeState.State.STATE_INVALID_LOCATION) {
                        image_done.setImageResource(R.drawable.baseline_wrong_location_24);
                        text_done.setText("Invalid Location");
                        registered_layout.setVisibility(View.VISIBLE);
                        main_layout.setVisibility(View.GONE);
                        assigning_layout.setVisibility(View.GONE);


                    } else if (remoteEmployeeState.getState() == FaceIdAiData.RemoteEmployeeState.State.STATE_NON_MATCHING_ID) {
                        image_done.setImageResource(R.drawable.baseline_person_off_24);
                        text_done.setText("Non Matching ID");
                        registered_layout.setVisibility(View.VISIBLE);
                        main_layout.setVisibility(View.GONE);
                        assigning_layout.setVisibility(View.GONE);


                    } else if (remoteEmployeeState.getState() == FaceIdAiData.RemoteEmployeeState.State.STATE_UNSPECIFIED) {
                        image_done.setImageResource(R.drawable.baseline_error_24);
                        text_done.setText("Error Happened");
                        registered_layout.setVisibility(View.VISIBLE);
                        main_layout.setVisibility(View.GONE);
                        assigning_layout.setVisibility(View.GONE);
                    } else if (remoteEmployeeState.getState() == FaceIdAiData.RemoteEmployeeState.State.STATE_INVALID_TIME) {
                        image_done.setImageResource(R.drawable.baseline_timer_off_24);
                        text_done.setText("Invalid Time");
                        registered_layout.setVisibility(View.VISIBLE);
                        main_layout.setVisibility(View.GONE);
                        assigning_layout.setVisibility(View.GONE);
                    } else if (remoteEmployeeState.getState() == FaceIdAiData.RemoteEmployeeState.State.STATE_NON_MATCHING_FACE) {
                        image_done.setImageResource(R.drawable.baseline_person_off_24);
                        text_done.setText("Non Matching Face");
                        registered_layout.setVisibility(View.VISIBLE);
                        main_layout.setVisibility(View.GONE);
                        assigning_layout.setVisibility(View.GONE);
                    }

                } catch (Exception e) {
                    do_retry = true;
                    Log.d(TAG, "run: exception" + e);
                    image_done.setImageResource(R.drawable.baseline_error_24);
                    text_done.setText("Connection Error");
                    registered_layout.setVisibility(View.VISIBLE);
                    main_layout.setVisibility(View.GONE);
                    assigning_layout.setVisibility(View.GONE);
                }
            }
        };
        Handler handler = new Handler();
        handler.post(runnable);
    }

    View.OnClickListener retry_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (do_retry) {
                retry.setText("Retry");
                main_layout.setVisibility(View.VISIBLE);
                registered_layout.setVisibility(View.GONE);
                assigning_layout.setVisibility(View.GONE);
            } else {
                Camera_activity.this.finish();
            }
        }
    };
}