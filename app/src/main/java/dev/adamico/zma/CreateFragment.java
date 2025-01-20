package dev.adamico.zma;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import dev.adamico.zma.databinding.CreateBinding;


public class CreateFragment extends Fragment {
    private CreateBinding binding;
    private TextView scannerLink;
    private TextView inputLink;
    private TextView barcodeInput;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = CreateBinding.inflate(inflater, container, false);

        scannerLink = binding.scannerLink;
        inputLink = binding.inputLink;
        barcodeInput = binding.barcodeIdInput;

        setupLinks();

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scannerLink.setOnClickListener(v -> {
            barcodeInput.setVisibility(View.GONE);
            if(getActivity() instanceof CameraActivity activity){
                activity.toggleScanner();
            }
        });

        inputLink.setOnClickListener(v -> {
            barcodeInput.setVisibility(barcodeInput.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            if(getActivity() instanceof CameraActivity activity){
                activity.turnScannerOff();
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
}
