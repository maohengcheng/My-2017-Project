package com.example.michael.my2017project;

import android.*;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileNotFoundException;
import java.io.IOException;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 100;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE = 200;
    public static final int MY_PERMISSION_ACCESS_COURSE_LOCATION = 300;
    public static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 400;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 500;
    public static final String IMAGE_FOLDER_REF = "Images";

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference firebaseRef = database.getReference();
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void upload(View view){
        /*
        EditText uploadCode = (EditText) findViewById(R.id.editText);
        String code = uploadCode.getText().toString();

        firebaseRef.child(code).setValue(1);

        Intent output = new Intent();
        setResult(RESULT_OK, output);
        */

        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE);
    }

    public void download(View view){
        EditText downloadCode = (EditText) findViewById(R.id.editText);
        String code = downloadCode.getText().toString();

        firebaseRef.child(code).setValue(1);

        Intent output = new Intent();
        setResult(RESULT_OK, output);
        //firebaseRef.child(code).setValue(taskSnapshot.getDownloadUrl().toString());

    }

    public String getUploadCode(){
        EditText uploadCode = (EditText) findViewById(R.id.editText);
        String code = uploadCode.getText().toString();
        return code;
    }


    @SuppressWarnings("VisibleForTests")
    @Override
    //@TargetApi(Build.VERSION_CODES.M)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                // get permission to read from data
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]
                                    {android.Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE);
                    return;
                }

                // get course location access permission for accessing location
                if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
                    requestPermissions(new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            MY_PERMISSION_ACCESS_COURSE_LOCATION);
                    return;
                }

                // get fine location access permission for accessing location
                if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
                    requestPermissions(new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSION_ACCESS_FINE_LOCATION);
                    return;
                }

                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]
                                    {android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                    return;
                }

                if (imageUri !=null) {
                    final ProgressDialog dialog = new ProgressDialog(this);
                    dialog.setTitle("Uploading image");
                    dialog.show();

                    StorageReference ref = storageReference.child(getUploadCode());
                    //Add file
                    ref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //Dismiss dialog when done
                            dialog.dismiss();
                            //Save image info into firebase database
                            String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                            ImageData imageData = new ImageData(taskSnapshot.getDownloadUrl().toString());
                            firebaseRef.child(getUploadCode()).setValue(imageData);
                            Toast.makeText(getApplicationContext(), "Image uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //Dismiss dialog and show an error
                                    dialog.dismiss();
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    //Show upload progress
                                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                    dialog.setMessage("Uploaded " + (int)progress+"0");
                                }
                            });
                }else {
                    Toast.makeText(getApplicationContext(), "Please select an image", Toast.LENGTH_SHORT).show();
                }


            } catch(Throwable e){
                e.printStackTrace();
            }
            /*catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }*/


        }
    }
}
