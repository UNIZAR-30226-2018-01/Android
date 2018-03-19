package SongManager;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Carlos on 18/03/2018.
 */

public class SongManager {

    final String MEDIA_PATH = Environment.getExternalStorageDirectory()+"";

    private static ArrayList<HashMap<String,String>> fileList;
    // Constructor
    public void SongManager(){

    }

    /**
     * Function to read all mp3 files from sdcard
     * and store the details in ArrayList
     * */
    public static ArrayList<HashMap<String,String>> refreshPlaylist(String rootPath) {
        fileList = new ArrayList<>();
        try{
            File rootFolder = new File(rootPath);
            File[] files = rootFolder.listFiles(); //here you will get NPE if directory doesn't contains  any file,handle it like this.
            for (File file : files) {
                if (file.isDirectory()) {
                    if (refreshPlaylist(file.getAbsolutePath()) != null) {
                        fileList.addAll(refreshPlaylist(file.getAbsolutePath()));
                    } else {
                        break;
                    }
                } else if (file.getName().endsWith(".mp3")) {
                    HashMap<String, String> song = new HashMap<>();
                    song.put("file_path", file.getAbsolutePath());
                    song.put("file_name", file.getName());
                    fileList.add(song);
                }
            }
            return fileList;
        } catch (Exception e) {
            return null;
        }
    }

    public static  ArrayList<HashMap<String,String>> getPlaylist() {
        return fileList;
    }
}
