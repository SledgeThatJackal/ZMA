package dev.adamico.zma;

import android.app.Application;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FileViewModel extends AndroidViewModel {
    private File rootFolder;
    private List<File> barcodeFolders;
    private File zipFile;

    private MutableLiveData<List<File>> folders = new MutableLiveData<>();
    private MutableLiveData<File> folder = new MutableLiveData<>();

    public FileViewModel(@NonNull Application application) {
        super(application);
        rootFolder = createBarcodeFolder("BarcodeFolders");
        barcodeFolders = new ArrayList<>(Arrays.asList(Objects.requireNonNull(rootFolder.listFiles())));

        setLiveFolders(barcodeFolders);

        if(!barcodeFolders.isEmpty()) setFolder(barcodeFolders.get(barcodeFolders.size() - 1));
    }

    public LiveData<List<File>> getLiveFolders(){
        return folders;
    }

    public void setLiveFolders(List<File> folders){
        this.folders.setValue(folders);
    }

    public LiveData<File> getSelectedFolder(){
        return folder;
    }

    public void setFolder(File selectedFolder){
        folder.setValue(selectedFolder);
    }

    public void deleteFolder(){
        File folderToRemove = folder.getValue();

        if(folderToRemove != null){
            barcodeFolders.remove(folderToRemove);
            setLiveFolders(barcodeFolders);
            deleteFile(folderToRemove);
        }
    }

    public File createBarcodeFolder(String barcodeValue){
        File currentFolder = rootFolder == null ? getApplication().getExternalFilesDir(null) : rootFolder;

        File barcodeFolder = new File(currentFolder, barcodeValue);

        if(!barcodeFolder.exists()){
            if(barcodeFolder.mkdir()){
                Log.d("BarcodeFolder", "Created Folder: " + barcodeValue);
                Toast.makeText(getApplication().getApplicationContext(), "Created Folder: " + barcodeFolder.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } else {
                Log.d("BarcodeFolder", "Failed to create folder");
                Toast.makeText(getApplication().getApplicationContext(), "Failed to create folder", Toast.LENGTH_LONG).show();
            }
        }

        if(rootFolder != null) {
            barcodeFolders.add(barcodeFolder);

            setLiveFolders(barcodeFolders);
        }

        return barcodeFolder;
    }

    public void renameFile(String barcodeValue) {
        File newFolder = new File(rootFolder, barcodeValue);
        File currentFolder = folder.getValue();

        if(currentFolder != null && currentFolder.renameTo(newFolder)){
            barcodeFolders.remove(currentFolder);
            barcodeFolders.add(newFolder);

            setLiveFolders(barcodeFolders);
            setFolder(newFolder);
        }
    }

    public void deleteFile(File file){
        if(file.isDirectory()){
            for(File child: Objects.requireNonNull(file.listFiles())){
                deleteFile(child);
            }
        }

        if(!file.delete()) Log.d("FileDelete", "Failed to delete files");
    }

    public void deleteZip(){
        deleteFile(rootFolder);
        deleteFile(zipFile);

        rootFolder = null;
        zipFile = null;
    }

    public File getSpecificFolder(int index){
        return barcodeFolders.get(index);
    }

    public List<File> getFolders(){
        return barcodeFolders;
    }

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void createZip(){
        if(rootFolder == null) throw new IllegalArgumentException("Root folder is missing");

        executorService.submit(() -> {
           try{
               zipFile = new File(getApplication().getExternalFilesDir(null), "/containerItems.zip");

               zipFolders();


           } catch (Exception e){
               e.printStackTrace();
           }
        });
    }

    private void zipFolders() throws IOException {
        try(FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos)) {

            File[] directories = rootFolder.listFiles(File::isDirectory);
            if(directories != null){
                for(File dir: directories){
                    zipContents(dir, zos, dir.getName() + "/");
                }
            }
        }
    }

    private void zipContents(File folder, ZipOutputStream zos, String folderName) throws IOException {
        File[] files = folder.listFiles();
        if(files != null){
            for(File file: files) {
                try(FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(folderName + file.getName());

                    zos.putNextEntry(zipEntry);

                    byte[] bytes = new byte[1024];
                    int length;
                    while((length = fis.read(bytes)) >= 0){
                        zos.write(bytes, 0, length);
                    }

                    zos.closeEntry();
                }
            }
        }
    }

    public void saveZipLocally(){
        File targetDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "ZMA");

        if(!targetDir.exists()){
            targetDir.mkdir();
        }

        File targetFile = new File(targetDir, zipFile.getName());

        try{
            Files.copy(zipFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            zipFile.delete();
            deleteFile(rootFolder);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void makeHttpRequest(){
        OkHttpClient client = new OkHttpClient();

        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", zipFile.getName(),
                        RequestBody.create(zipFile, MediaType.parse("application/zip")))
                .build();

        Request request = new Request.Builder()
                .url("http://192.168.1.201:8888/api/zip")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    Log.d("Request", call.request().url().toString());
                }
            }
        });
    }
}
