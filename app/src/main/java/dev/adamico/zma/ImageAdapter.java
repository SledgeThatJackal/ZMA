package dev.adamico.zma;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private LiveData<List<File>> images;
    private final FileViewModel fileViewModel;
    private final RemoveImageListener removeImageListener;

    public interface RemoveImageListener {
        void removeImage(File image);
    }

    public ImageAdapter(LiveData<List<File>> images, RemoveImageListener removeImageListener, FileViewModel fileViewModel) {
        this.images = images;
        this.fileViewModel = fileViewModel;
        this.removeImageListener = removeImageListener;

        this.images.observeForever(obImages -> notifyDataSetChanged());
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_layout, parent, false);

        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        File image = Objects.requireNonNull(images.getValue()).get(position);

        Log.d("Image", image.getName());

        holder.imageText.setText(image.getName());

        holder.imageRemoveButton.setOnClickListener(v -> removeImageListener.removeImage(image));
    }

    @Override
    public int getItemCount() {
        return images.getValue() != null ? images.getValue().size() : 0;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder{
        private TextView imageText;
        private ImageButton imageRemoveButton;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            imageText = itemView.findViewById(R.id.imageText);
            imageRemoveButton = itemView.findViewById(R.id.imageRemoveButton);
        }
    }
}
