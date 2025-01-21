package dev.adamico.zma;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.io.File;
import java.util.Objects;

import dev.adamico.zma.databinding.FragmentCreateBinding;


public class CreateFragment extends Fragment {
    private FragmentCreateBinding binding;
    private TextView scannerLink;
    private TextView inputLink;
    private EditText barcodeInput;
    private ImageButton confirmButton;
    private TextView instructions;
    private LinearLayout inputLayout;

    private TextView barcodeValue;
    private Button addImageButton;
    private TextView imageNames;

    private Button doneButton;

    private FileViewModel fileViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateBinding.inflate(inflater, container, false);

        scannerLink = binding.scannerLink;
        inputLink = binding.inputLink;
        barcodeInput = binding.barcodeIdInput;
        confirmButton = binding.confirmButton;
        instructions = binding.instructions;
        inputLayout = binding.inputLayout;
        barcodeValue = binding.barcodeValue;
        addImageButton = binding.addImageButton;
        imageNames = binding.imageNames;
        doneButton = binding.doneButton;

        setupLinks();

        return binding.getRoot();
    }

    @SuppressLint("RestrictedApi")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fileViewModel = new ViewModelProvider(requireActivity()).get(FileViewModel.class);
        NavController navController = Navigation.findNavController(this.requireView());

        scannerLink.setOnClickListener(v -> {
            inputLayout.setVisibility(View.GONE);
            navController.navigate(R.id.action_createFragment_to_scannerFragment);
        });

        inputLink.setOnClickListener(v -> {
            inputLayout.setVisibility(inputLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        addImageButton.setOnClickListener(v -> {
            navController.navigate(R.id.action_createFragment_to_cameraFragment);
        });

        doneButton.setOnClickListener(v -> {
            navController.navigate(R.id.action_createFragment_to_zipFragment);
        });

        confirmButton.setOnClickListener(v -> {
            String value = barcodeInput.getText().toString();

            if(value.isEmpty()){
                Toast.makeText(requireContext(), "Barcode ID cannot be blank", Toast.LENGTH_LONG).show();
            } else {
                fileViewModel.createBarcodeFolder(value);
                handleInputSubmit();
            }
        });

        NavBackStackEntry previousEntry = navController.getPreviousBackStackEntry();

        if(fileViewModel.getCurrentFolder() != null) {
            if (previousEntry == null || !previousEntry.getDestination().getDisplayName().contains("zipFragment")) {
                showFolders();
            }
        }
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

    private void handleInputSubmit(){
        inputLayout.setVisibility(View.GONE);
        barcodeInput.setText("", TextView.BufferType.EDITABLE);
        showFolders();
    }

    private void hideBarcodeButtons(){
        inputLink.setVisibility(View.GONE);
        scannerLink.setVisibility(View.GONE);
        instructions.setVisibility(View.GONE);
    }

    private void showFolders(){
        hideBarcodeButtons();

        File currentFolder = fileViewModel.getCurrentFolder();

        String text = "Barcode: " + currentFolder.getName();
        barcodeValue.setText(text);

        File[] files = currentFolder.listFiles();
        StringBuilder fileNames = new StringBuilder();
        if(files != null && files.length > 0){
            for(File file: files){
                fileNames.append(file.getName()).append("\n");
            }
        }

        imageNames.setText(fileNames);

        barcodeValue.setVisibility(View.VISIBLE);
        imageNames.setVisibility(View.VISIBLE);
        addImageButton.setVisibility(View.VISIBLE);
    }
}
