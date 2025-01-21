package dev.adamico.zma;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.io.File;
import java.util.List;

import dev.adamico.zma.databinding.FragmentZipBinding;

public class ZipFragment extends Fragment {
    private FragmentZipBinding binding;

    private TextView foldersView;
    private Button addMoreButton;
    private Button createButton;

    private FileViewModel fileViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentZipBinding.inflate(inflater, container, false);

        foldersView = binding.foldersView;
        addMoreButton = binding.addMoreButton;
        createButton = binding.createButton;

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fileViewModel = new ViewModelProvider(requireActivity()).get(FileViewModel.class);
        NavController navController = Navigation.findNavController(requireView());

        addMoreButton.setOnClickListener(v -> {
            navController.navigate(R.id.action_zipFragment_to_createFragment);
        });

        createButton.setOnClickListener(v -> {
            fileViewModel.createZip();

            navController.navigate(R.id.action_zipFragment_to_requestFragment);
        });

        setupFolderText();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupFolderText(){
        List<File> folders = fileViewModel.getFolders();

        StringBuilder stringBuilder = new StringBuilder();

        folders.forEach(folder -> {
            File[] files = folder.listFiles();

            stringBuilder.append("Barcode: ").append(folder.getName()).append("\n");

            if(files != null){
                for(File file: files){
                    stringBuilder.append(file.getName()).append("\n");
                }

                stringBuilder.append("\n");
            }
        });

        foldersView.setText(stringBuilder);
    }
}
