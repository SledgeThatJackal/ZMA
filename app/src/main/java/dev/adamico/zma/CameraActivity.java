package dev.adamico.zma;

import static androidx.camera.core.ImageCapture.*;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.journeyapps.barcodescanner.BarcodeView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dev.adamico.zma.databinding.ActivityCameraBinding;

public class CameraActivity extends AppCompatActivity {
    private ActivityCameraBinding binding;
    private LifecycleCameraController cameraController;
    private PreviewView cameraView;
    private BarcodeView scannerView;
    private File rootFolder;
    private List<File> barcodeFolders;
    private Button captureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cameraView = binding.cameraView;
        scannerView = binding.scannerView;
        captureButton = binding.captureButton;

        rootFolder = setupRootFolder();
        barcodeFolders = new ArrayList<>();

        captureButton.setOnClickListener(v -> takePhoto());

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        } else {
            startScanning();
            startCamera();
        }
    }

    private File setupRootFolder(){
        File file = new File(getExternalFilesDir(null), "BarcodeFolder");

        if(!file.exists()){
            if(file.mkdir()){
                Log.d("RootFolder", "Created");
                Toast.makeText(getApplicationContext(), "RootFolder: Created", Toast.LENGTH_LONG).show();
            } else {
                Log.d("RootFolder", "Failed to create folder");
                Toast.makeText(getApplicationContext(), "RootFolder: Failed to create", Toast.LENGTH_LONG).show();
            }
        }

        return file;
    }

    private void startScanning(){
        scannerView.decodeContinuous(result -> {
            String scannedValue = result.getText();

            if(scannedValue != null){
                scannerView.pause();

                createBarcodeFolder(scannedValue);
            }
        });
    }

    private void createBarcodeFolder(String barcodeValue){
        File barcodeFolder = new File(rootFolder, barcodeValue);

        if(!barcodeFolder.exists()){
            if(barcodeFolder.mkdir()){
                Log.d("BarcodeFolder", "Created Folder: " + barcodeValue);
                Toast.makeText(getApplicationContext(), "Created Folder: " + barcodeFolder.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } else {
                Log.d("BarcodeFolder", "Failed to create folder");
                Toast.makeText(getApplicationContext(), "Failed to create folder", Toast.LENGTH_LONG).show();
            }
        }

        barcodeFolders.add(barcodeFolder);

        turnScannerOff();
        turnCameraOn();
    }

    private void takePhoto(){
        File currentFolder = barcodeFolders.get(barcodeFolders.size() - 1);
        File imageLocation = new File(currentFolder, currentFolder.getName() + "_Image_" + System.currentTimeMillis() + ".jpg");


        cameraController.takePicture(
                new OutputFileOptions.Builder(imageLocation).build(),
                getMainExecutor(),
                new OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        // File is already saved directly to customFileLocation
                        // You can now access and process the saved file as needed
                        Toast.makeText(getApplicationContext(), "Photo saved directly to: " + imageLocation.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        // Handle the error
                        Toast.makeText(getApplicationContext(), "Failed to take photo", Toast.LENGTH_LONG).show();
                        exception.printStackTrace();
                    }
                });
    }

    private void startCamera(){
        cameraController = new LifecycleCameraController(this);
        cameraController.bindToLifecycle(this);

        cameraView.setController(cameraController);
    }

    public void toggleScanner(){
        scannerView.setVisibility(scannerView.getVisibility() == BarcodeView.VISIBLE ? BarcodeView.GONE : BarcodeView.VISIBLE);
    }

    public void turnScannerOff(){
        scannerView.setVisibility(BarcodeView.GONE);
    }

    public void turnCameraOn(){
        cameraView.setVisibility(PreviewView.VISIBLE);
        captureButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scannerView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.pause();
    }

    private void deleteFile(File file){
        if(file.isDirectory()){
            for(File child: Objects.requireNonNull(file.listFiles())){
                deleteFile(child);
            }
        }

        if(!file.delete()) Log.d("FileDelete", "Failed to delete files");
    }
}
