package dev.adamico.zma;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.io.File;

import dev.adamico.zma.databinding.FragmentBarcodeBinding;
import dev.adamico.zma.databinding.FragmentCameraBinding;

public class CameraFragment extends Fragment {
    private FragmentCameraBinding binding;
    private LifecycleCameraController cameraController;
    private PreviewView cameraView;
    private Button captureButton;

    private FileViewModel fileViewModel;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        binding = FragmentCameraBinding.inflate(inflater, container, false);

        cameraView = binding.cameraView;
        captureButton = binding.captureButton;

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fileViewModel = new ViewModelProvider(requireActivity()).get(FileViewModel.class);
        startCamera();

        captureButton.setOnClickListener(v -> takePhoto());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void startCamera(){
        cameraController = new LifecycleCameraController(requireContext());
        cameraController.bindToLifecycle(this);

        cameraView.setController(cameraController);
    }

    private void takePhoto(){
        File currentFolder = fileViewModel.getSelectedFolder().getValue();
        File imageLocation = new File(currentFolder, currentFolder.getName() + "_Image_" + System.currentTimeMillis() + ".jpg");

        cameraController.takePicture(
                new ImageCapture.OutputFileOptions.Builder(imageLocation).build(),
                requireActivity().getMainExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Toast.makeText(requireContext(), "Photo saved directly to: " + imageLocation.getAbsolutePath(), Toast.LENGTH_LONG).show();
                        NavController navController = Navigation.findNavController(requireView());
                        navController.navigate(R.id.action_cameraFragment_to_createFragment);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        // Handle the error
                        Toast.makeText(requireContext(), "Failed to take photo", Toast.LENGTH_LONG).show();
                        exception.printStackTrace();
                    }
                }
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        if (cameraController != null) {
            startCamera();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (cameraController != null) {
            cameraController.unbind();
        }
    }
}
