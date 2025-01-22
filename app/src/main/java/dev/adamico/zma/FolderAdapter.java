package dev.adamico.zma;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {
    private LiveData<List<File>> folders;
    private final NavController navController;
    private final FileViewModel fileViewModel;

    public FolderAdapter(LiveData<List<File>> folders, NavController navController, FileViewModel fileViewModel) {
        this.folders = folders;
        this.navController = navController;
        this.fileViewModel = fileViewModel;

        this.folders.observeForever(obFolders -> notifyDataSetChanged());
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.folder_layout, parent, false);

        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        File folder = Objects.requireNonNull(folders.getValue()).get(position);

        StringBuilder builder = new StringBuilder();
        builder.append("Barcode: ").append(folder.getName());

        File[] nestedFolders = folder.listFiles();
        if(nestedFolders != null){
            for(File file: nestedFolders){
                Log.d("Adapter", file.getName());
                builder.append("\n").append(file.getName());
            }
        }

        holder.folderText.setText(builder);

        holder.itemView.setOnClickListener(v -> {
            fileViewModel.setFolder(folder);

            navController.navigate(R.id.action_zipFragment_to_createFragment);
        });
    }

    @Override
    public int getItemCount() {
        return folders.getValue() != null ? folders.getValue().size() : 0;
    }

    public static class FolderViewHolder extends RecyclerView.ViewHolder {
        private TextView folderText;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);

            folderText = itemView.findViewById(R.id.folderText);
        }
    }
}

