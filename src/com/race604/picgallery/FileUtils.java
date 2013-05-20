
package com.race604.picgallery;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.Context;
import android.util.Log;

public class FileUtils {
	
	private static final String TAG = "FileUtils";

    private static final int BUFFER_SIZE = 4096;

    private static final String OBJECT_CACHE_DIR = "obj_cache";

    private static final String OBJECT_PERSISTANCE_DIR = "obj";

    public static String InputStreamTOString(InputStream in) throws IOException {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[BUFFER_SIZE];
        int count = -1;
        while ((count = in.read(data, 0, BUFFER_SIZE)) != -1)
            outStream.write(data, 0, count);

        data = null;
        byte[] ret = outStream.toByteArray();
        outStream.close();
        return new String(ret, "ISO-8859-1");
    }

    public static InputStream StringTOInputStream(String in) throws Exception {

        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes("ISO-8859-1"));
        return is;
    }

    public static byte[] InputStreamTOByte(InputStream in) throws IOException {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[BUFFER_SIZE];
        int count = -1;
        while ((count = in.read(data, 0, BUFFER_SIZE)) != -1)
            outStream.write(data, 0, count);

        data = null;
        return outStream.toByteArray();
    }

    public static InputStream byteTOInputStream(byte[] in) throws Exception {

        ByteArrayInputStream is = new ByteArrayInputStream(in);
        return is;
    }

    public static String byteTOString(byte[] in) throws Exception {

        InputStream is = byteTOInputStream(in);
        return InputStreamTOString(is);
    }

    public static Object readObjectFromCache(Context ctx, String fileName) {
        try {
            String absolutePath = ctx.getCacheDir().getAbsolutePath() + File.separator
                    + OBJECT_CACHE_DIR;
            File path = new File(absolutePath);
            if (!path.exists()) {
                path.mkdirs();
            }
            FileInputStream is = new FileInputStream(new File(path.getAbsolutePath()
                    + File.separator + fileName));
            ObjectInputStream in = new ObjectInputStream(is);
            Object object = null;
            try {
                object = in.readObject();
            } catch (ClassNotFoundException e) {
                // not found;
                Log.e(TAG, "class not found!" + fileName);
            }
            in.close();
            is.close();
            return object;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean saveObjectToCache(Context ctx, String fileName, Object obj) {
        try {
            ByteArrayOutputStream mem_out = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(mem_out);
            out.writeObject(obj);
            out.close();
            String absolutePath = ctx.getCacheDir().getAbsolutePath() + File.separator
                    + OBJECT_CACHE_DIR;
            File path = new File(absolutePath);
            if (!path.exists()) {
                path.mkdirs();
                path.createNewFile();
            }
            FileOutputStream fs = new FileOutputStream(new File(path.getAbsolutePath()
                    + File.separator + fileName));
            fs.write(mem_out.toByteArray());
            mem_out.close();
            fs.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void removeObjectCacheFile(Context ctx, String fileName) {
        String path = ctx.getCacheDir().getAbsolutePath() + File.separator + OBJECT_CACHE_DIR
                + File.separator + fileName;
        File file = new File(path);
        file.delete();
    }

    public static Object readObjectFromFile(Context ctx, String fileName) {
        try {
            String absolutePath = ctx.getFilesDir().getAbsolutePath() + OBJECT_PERSISTANCE_DIR;
            File f = new File(absolutePath);
            if (!f.exists()) {
                f.mkdirs();
            }
            FileInputStream is = new FileInputStream(new File(f.getAbsolutePath() + File.separator
                    + fileName));
            ObjectInputStream in = new ObjectInputStream(is);
            Object object = null;
            try {
                object = in.readObject();
            } catch (ClassNotFoundException e) {
                // not found;
            }
            in.close();
            is.close();
            return object;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean saveObjectToFile(Context ctx, String fileName, Object obj) {
        try {
            ByteArrayOutputStream mem_out = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(mem_out);
            out.writeObject(obj);
            out.close();
            String absolutePath = ctx.getFilesDir().getAbsolutePath() + OBJECT_PERSISTANCE_DIR;
            File path = new File(absolutePath);
            if (!path.exists()) {
                path.mkdirs();
            }
            FileOutputStream fs = new FileOutputStream(new File(path.getAbsolutePath()
                    + File.separator + fileName));
            fs.write(mem_out.toByteArray());
            mem_out.close();
            fs.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static long cacheSizeInBytes(Context ctx) {
        File cache = ctx.getCacheDir();
        return fileSizeInBytes(cache);
    }

    public static boolean clearCache(Context ctx) {
        File cacheDirFile = ctx.getCacheDir();
        return clearDir(cacheDirFile);
    }

    public static long fileSizeInBytes(File file) {
        if (file.isFile()) {
            return file.length();
        } else {
            long total = 0;
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    total += fileSizeInBytes(f);
                }
            }
            return total;
        }
    }

    private static boolean clearDir(File dir) {
        if (!dir.isDirectory()) {
            return true;
        }

        if (!dir.canWrite()) {
            return false;
        }

        File[] list = dir.listFiles();
        int len = 0;
        if (list != null) {
            len = list.length;
        }
        boolean ret = true;
        for (int i = 0; i < len; ++i) {
            if (list[i].isDirectory()) {
                if (!clearDir(list[i])) {
                    Log.d(TAG, "delete fail  File:" + list[i].getAbsolutePath());
                    ret = false;
                }
            } else if (list[i].canWrite()) {
                if (!list[i].delete()) {
                	Log.d(TAG, "delete fail  File:" + list[i].getAbsolutePath());
                    ret = false;
                }
            } else {
                ret = false;
                Log.d(TAG, "delete fail  File:" + list[i].getAbsolutePath());
            }
        }
        return ret;
    }

    public static boolean renameOnOverrite(Context ctx, String oldName, String newName) {
        String oldPath = ctx.getFilesDir().getAbsolutePath() + File.pathSeparator + oldName;
        String newPath = ctx.getFilesDir().getAbsolutePath() + File.pathSeparator + newName;
        File newFile = new File(newPath);
        File oldFile = new File(oldPath);
        if (!oldFile.renameTo(newFile)) {
            newFile.delete();
            return oldFile.renameTo(newFile);
        }
        return true;
    }

    public static void saveStreamToFile(Context ctx, String fileName, InputStream is) {
        byte[] data = new byte[BUFFER_SIZE];
        int count = -1;
        FileOutputStream os = null;
        String tmpFile = fileName + "_temp";
        try {
            os = ctx.openFileOutput(fileName, Context.MODE_PRIVATE);
            while ((count = is.read(data, 0, BUFFER_SIZE)) != -1) {
                os.write(data, 0, count);
            }
            renameOnOverrite(ctx, tmpFile, fileName);
        } catch (IOException e) {
        } finally {
            data = null;
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
