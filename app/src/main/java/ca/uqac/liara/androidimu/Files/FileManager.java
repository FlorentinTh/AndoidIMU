package ca.uqac.liara.androidimu.Files;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by FlorentinTh on 2/20/2017.
 */

public class FileManager {
    private final String filename;
    private final String format;

    private File folder;
    private File file;
    private FileWriter fileWriter;

    public FileManager(String filename) {
        this.filename = filename;
        this.format = FileConfiguration.getInstance().getFormat();
        createFileOnSDCard();
    }

    public void open() {
       try {
           fileWriter = new FileWriter(file, true);
       } catch (IOException e) {
           Log.e(getClass().getSimpleName(), e.getMessage());
       }
    }

    public void write(Object[] o) {
        try {
            for (int i = 0; i < o.length; i++) {
                if (i == o.length - 1) {
                    fileWriter.write(String.valueOf(o[i]) + FileConfiguration.getInstance().getLineSeparator());
                } else {
                    fileWriter.write(String.valueOf(o[i]) + FileConfiguration.getInstance().getRecordSeparator());
                }
            }
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), e.getMessage());
        }
    }

    public void close() {
        if (fileWriter != null) {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                Log.e(getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    private void createFileOnSDCard() {
        folder = new File(Environment.getExternalStorageDirectory().getPath(), FileConfiguration.getInstance().getBaseFolder());

        if (!folder.exists()) {
            folder.mkdir();
        }

        file = new File(folder, this.filename + this.format);

        try {
            if (!file.exists()) {
                file.createNewFile();
                open();
                write(FileConfiguration.getInstance().getHeaders());
                close();
            }
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), e.getMessage());
        }
    }
}
