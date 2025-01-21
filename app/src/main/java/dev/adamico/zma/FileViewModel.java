package dev.adamico.zma;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileViewModel extends AndroidViewModel {
    private File rootFolder;
    private List<File> barcodeFolders;
    private File zipFile;

    public FileViewModel(@NonNull Application application) {
        super(application);
        rootFolder = createBarcodeFolder("BarcodeFolders");
        barcodeFolders = new ArrayList<>(Arrays.asList(Objects.requireNonNull(rootFolder.listFiles())));
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

        if(rootFolder != null) barcodeFolders.add(barcodeFolder);

        return barcodeFolder;
    }

    public void deleteFile(File file){
        if(file.isDirectory()){
            for(File child: Objects.requireNonNull(file.listFiles())){
                deleteFile(child);
            }
        }

        if(!file.delete()) Log.d("FileDelete", "Failed to delete files");
    }

    public File getCurrentFolder(){
        if(barcodeFolders.isEmpty()) return null;

        return barcodeFolders.get(barcodeFolders.size() - 1);
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
        FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream zos = new ZipOutputStream(fos);

        File[] directories = rootFolder.listFiles(File::isDirectory);
        if(directories != null){
            for(File dir: directories){
                zipContents(dir, zos, dir.getName() + "/");
            }
        }
    }

    private void zipContents(File folder, ZipOutputStream zos, String folderName) throws IOException {
        File[] files = folder.listFiles();
        if(files != null){
            for(File file: files) {
                FileInputStream fis = new FileInputStream(file);
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
