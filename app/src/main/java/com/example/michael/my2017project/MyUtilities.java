package com.example.michael.my2017project;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Michael on 7/5/2017.
 */

public class MyUtilities {
    static String temp = "a";

    public static void saveToCustomDirectory(Context context, Context appContext, ContentResolver resolver,
                                             Uri imgUri, String dirName) throws IOException {
        Bitmap bm = MediaStore.Images.Media.getBitmap(resolver, imgUri);

        /* get phone resolution */
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) appContext.
                getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        Bitmap b2 = Bitmap.createScaledBitmap(bm, screenWidth, screenHeight, false);
    /* get other album directory and write to it */
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        //final File imageRoot = new File(Environment.getExternalStoragePublicDirectory
        //        (Environment.DIRECTORY_PICTURES), dirName);
        //content://media/external/images/media
        final File imageRoot = new File(root, File.separator + dirName);

        //imageRoot.delete();
        if(!imageRoot.exists()) {
            imageRoot.mkdirs();
        }
        final File image = new File(imageRoot, imgUri.getLastPathSegment() + ".jpg");
        if(image.exists())
            image.delete();

        FileOutputStream fOutputStream = new FileOutputStream(image);
        b2.compress(Bitmap.CompressFormat.JPEG, 100, fOutputStream);
        fOutputStream.flush();
        fOutputStream.close();

        MediaScannerConnection.scanFile(context, new String[]{image.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("External Storage", "Scanned " + path);
                        Log.i("External Storage", "-> uri= " + uri);
                        final String temp2 = uri.toString();
                        temp = temp2;
                        temp = temp.substring(temp.lastIndexOf("/") + 1);
                        Log.i("Ext:", "temp: " + temp);
                    }
                });

        Toast.makeText(appContext,"Image Copied", Toast.LENGTH_SHORT).show();
    }
}
