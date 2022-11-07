package com.code4copy.filedownloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class Utils {
    public static boolean concatFiles(File targetFile, List<File> files) throws Exception {
        OutputStream out = new FileOutputStream(targetFile);
        byte[] buf = new byte[1024*8];
        for (File file : files) {
            InputStream in = new FileInputStream(file);
            int b = 0;
            while ( (b = in.read(buf)) >= 0)
                out.write(buf, 0, b);
            in.close();
        }
        out.close();
        return true;

    }

    public static boolean isEmpty(String str){
        return str == null || str.trim().length()==0;
    }
}
