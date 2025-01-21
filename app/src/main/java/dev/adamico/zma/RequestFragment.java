package dev.adamico.zma;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import dev.adamico.zma.databinding.FragmentRequestBinding;

public class RequestFragment extends Fragment {
    private FragmentRequestBinding binding;
    private Button doneButton;
    private Button saveButton;
    private Button serverButton;

    private FileViewModel fileViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        binding = FragmentRequestBinding.inflate(inflater, container, false);

        doneButton = binding.doneButton;
        saveButton = binding.saveButton;
        serverButton = binding.serverButton;

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        fileViewModel = new ViewModelProvider(requireActivity()).get(FileViewModel.class);
        NavController navController = Navigation.findNavController(requireView());

//        fileViewModel.login();

        doneButton.setOnClickListener(v -> {
            fileViewModel.deleteZip();

            navController.navigate(R.id.action_requestFragment_to_createFragment);
        });

        saveButton.setOnClickListener(v -> {
            fileViewModel.saveZipLocally();
        });

        serverButton.setOnClickListener(v -> {
            fileViewModel.makeHttpRequest();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
