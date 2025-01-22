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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

import dev.adamico.zma.databinding.FragmentZipBinding;

public class ZipFragment extends Fragment {
    private FragmentZipBinding binding;

    private Button addMoreButton;
    private Button createButton;
    private RecyclerView recyclerView;

    private FileViewModel fileViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentZipBinding.inflate(inflater, container, false);

        addMoreButton = binding.addMoreButton;
        createButton = binding.createButton;

        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fileViewModel = new ViewModelProvider(requireActivity()).get(FileViewModel.class);
        NavController navController = Navigation.findNavController(requireView());

        FolderAdapter adapter = new FolderAdapter(fileViewModel.getLiveFolders(), navController, fileViewModel);
        recyclerView.setAdapter(adapter);

        fileViewModel.getLiveFolders().observe(getViewLifecycleOwner(), folders -> {
            if(folders != null){
                Log.d("Test", folders.toString());
                adapter.notifyDataSetChanged();
            }
        });

        addMoreButton.setOnClickListener(v -> {
            fileViewModel.setFolder(null);

            navController.navigate(R.id.action_zipFragment_to_createFragment);
        });

        createButton.setOnClickListener(v -> {
            fileViewModel.createZip();

            navController.navigate(R.id.action_zipFragment_to_requestFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
