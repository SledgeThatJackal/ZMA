package dev.adamico.zma;

import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceRequest;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.fragment.NavHostFragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import dev.adamico.zma.databinding.CreateBinding;


public class CreateFragment extends Fragment {
    private CreateBinding binding;
    private TextView scannerLink;
    private TextView inputLink;
    private PreviewView previewView;
    private ImageAnalysis imageAnalysis;
    private ProcessCameraProvider cameraProvider;
    private Preview preview;
    private Executor cameraExecutor;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = CreateBinding.inflate(inflater, container, false);

        scannerLink = binding.scannerLink;
        inputLink = binding.inputLink;
        previewView = binding.scannerView;

        setupLinks();

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scannerLink.setOnClickListener(v -> {
            inputLink.setClickable(true);
            scannerLink.setClickable(false);
            binding.barcodeIdInput.setVisibility(View.GONE);
            previewView.setVisibility(View.VISIBLE);
            isBarcodeScanning = true;
            startCamera();
        });

        inputLink.setOnClickListener(v -> {
            binding.barcodeIdInput.setVisibility(View.VISIBLE);
            inputLink.setClickable(false);
            scannerLink.setClickable(true);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupLinks() {
        SpannableStringBuilder spannableScanner = new SpannableStringBuilder("Scanner");
        SpannableStringBuilder spannableInput = new SpannableStringBuilder("Input");

        spannableScanner.setSpan(new UnderlineSpan(), 0, spannableScanner.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableInput.setSpan(new UnderlineSpan(), 0, spannableInput.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        scannerLink.setText(spannableScanner);
        inputLink.setText(spannableInput);
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraExecutor = Executors.newSingleThreadExecutor();

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
                                .build();

                imageAnalysis.setAnalyzer(cameraExecutor, image -> {
                    if(isBarcodeScanning && image.getImage() != null && previewView.getDisplay() != null) {
                        InputImage inputImage = InputImage.fromMediaImage(image.getImage(), previewView.getDisplay().getRotation());

                        startBarcodeScanning(inputImage);
                    }
                });

                cameraProvider.bindToLifecycle(requireActivity(), cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e){
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));

    }

    private void startBarcodeScanning(InputImage image){
        if(barcodeDetected) return;

        BarcodeScannerOptions options  = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_UPC_A,
                        Barcode.FORMAT_UPC_E,
                        Barcode.FORMAT_CODE_128,
                        Barcode.FORMAT_CODE_39
                )
                .build();

        BarcodeScanner scanner = BarcodeScanning.getClient(options);

        Image mediaImage = image.getMediaImage();

        Log.d("Test", "Got here");

        scanner.process(image).addOnSuccessListener(barcodes -> {
                    if(!barcodes.isEmpty()){
                        String rawValue = barcodes.get(0).getRawValue();
                        Log.d("CreateFragment", "Barcode detected: " + rawValue);

                        cameraProvider.unbindAll();
                        isBarcodeScanning = false;
                        barcodeDetected = true;
                    }

                })
                .addOnFailureListener(Throwable::printStackTrace)
                .addOnCompleteListener(task -> {
                    if(mediaImage != null){
                        mediaImage.close();
                    }
                });
    }

    private boolean isBarcodeScanning = false;
    private boolean barcodeDetected = false;

    private void enableBarcodeScanning() {
        isBarcodeScanning = true;
        rebindCamera();
    }

    private void disableBarcodeScanning() {
        isBarcodeScanning = false;
        rebindCamera();
    }

    private void rebindCamera() {
        try{
            cameraProvider.unbindAll();

            CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

            if(isBarcodeScanning) {
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
            } else {
                cameraProvider.bindToLifecycle(this, cameraSelector, preview);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
