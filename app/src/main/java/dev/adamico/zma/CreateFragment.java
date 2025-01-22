package dev.adamico.zma;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private LinearLayout barcodeValueLayout;
    private Button addImageButton;
    private RecyclerView imageRecyclerView;

    private Button doneButton;
    private Button deleteButton;
    private Button changeButton;
    private LinearLayout containerOptionsLayout;

    private FileViewModel fileViewModel;

    private MutableLiveData<List<File>> imageFolder;

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
        barcodeValueLayout = binding.barcodeValueLayout;
        addImageButton = binding.addImageButton;
        doneButton = binding.doneButton;
        deleteButton = binding.deleteButton;
        changeButton = binding.changeButton;
        containerOptionsLayout = binding.containerOptionsLayout;

        imageRecyclerView = binding.imageRecyclerView;
        imageRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        imageFolder = new MutableLiveData<>();

        setupLinks();

        return binding.getRoot();
    }

    @SuppressLint("RestrictedApi")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fileViewModel = new ViewModelProvider(requireActivity()).get(FileViewModel.class);
        NavController navController = Navigation.findNavController(this.requireView());

        ImageAdapter adapter = new ImageAdapter(imageFolder, this::removeImage, fileViewModel);
        imageRecyclerView.setAdapter(adapter);

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

        deleteButton.setOnClickListener(v -> {
            fileViewModel.deleteFolder();

            navController.navigate(R.id.action_createFragment_to_zipFragment);
        });

        changeButton.setOnClickListener(v -> {
            EditText editText = new EditText(requireContext());
            editText.setText(fileViewModel.getSelectedFolder().getValue().getName(), TextView.BufferType.EDITABLE);
            editText.setFocusable(true);
            editText.setFocusableInTouchMode(true);
            editText.setInputType(EditorInfo.TYPE_CLASS_TEXT);

            barcodeValue.setVisibility(View.GONE);
            barcodeValueLayout.addView(editText);
            imageRecyclerView.setVisibility(View.GONE);
            addImageButton.setVisibility(View.GONE);

            editText.requestFocus();

            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

            ImageButton button = new ImageButton(requireContext());
            button.setImageResource(R.drawable.ic_check);
            button.setBackground(null);

            barcodeValueLayout.addView(button);

            button.setOnClickListener(v1 -> {
                fileViewModel.renameFile(editText.getText().toString());
                barcodeValue.setVisibility(View.VISIBLE);
                imageRecyclerView.setVisibility(View.VISIBLE);
                addImageButton.setVisibility(View.VISIBLE);

                barcodeValueLayout.removeView(editText);
                barcodeValueLayout.removeView(button);

                imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0);
            });
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

        fileViewModel.getSelectedFolder().observe(getViewLifecycleOwner(), file -> {
            if(file != null) {
                imageFolder.setValue(Arrays.asList(Objects.requireNonNull(file.listFiles())));
                showFolders();
            }
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

    public void removeImage(File image){
        List<File> currentImages = imageFolder.getValue();

        if(currentImages != null){
            List<File> updatedImages = new ArrayList<>(currentImages);
            updatedImages.remove(image);
            imageFolder.setValue(updatedImages);

            fileViewModel.deleteFile(image);
        }
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

        File currentFolder = fileViewModel.getSelectedFolder().getValue();

        String value = "Barcode: " + Objects.requireNonNull(currentFolder).getName();
        barcodeValue.setText(value);

        barcodeValueLayout.setVisibility(View.VISIBLE);
        addImageButton.setVisibility(View.VISIBLE);
        imageRecyclerView.setVisibility(View.VISIBLE);
        containerOptionsLayout.setVisibility(View.VISIBLE);
    }
}
