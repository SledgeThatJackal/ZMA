package dev.adamico.zma;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.journeyapps.barcodescanner.BarcodeView;

import dev.adamico.zma.databinding.FragmentBarcodeBinding;

public class BarcodeFragment extends Fragment {
    private FragmentBarcodeBinding binding;
    private BarcodeView scannerView;
    private TextView barcodeValue;
    private ImageButton confirmButton;
    private ImageButton cancelButton;

    private FileViewModel fileViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        binding = FragmentBarcodeBinding.inflate(inflater, container, false);

        fileViewModel = new ViewModelProvider(requireActivity()).get(FileViewModel.class);

        scannerView = binding.scannerView;
        barcodeValue = binding.barcodeValue;
        confirmButton = binding.confirmButton;
        cancelButton = binding.cancelButton;

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(this.requireView());

        startScanning();

        confirmButton.setOnClickListener(v -> {
            fileViewModel.createBarcodeFolder((String) barcodeValue.getText());
            navController.navigate(R.id.action_scannerFragment_to_createFragment);
        });

        cancelButton.setOnClickListener(v -> {
            hideBarcode();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void startScanning(){
        scannerView.decodeContinuous(result -> {
            String scannedValue = result.getText();

            if(scannedValue != null){
                scannerView.pause();
                barcodeValue.setText(scannedValue);
                showBarcode();
            }
        });
    }

    private void showBarcode(){
        barcodeValue.setVisibility(View.VISIBLE);
        confirmButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);
    }

    private void hideBarcode(){
        barcodeValue.setVisibility(View.GONE);
        confirmButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);

        barcodeValue.setText("");
        scannerView.resume();
    }

    @Override
    public void onResume() {
        super.onResume();
        scannerView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        scannerView.pause();
    }
}
