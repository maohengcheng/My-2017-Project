package com.example.michael.my2017project;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 100;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE = 200;
    public static final int MY_PERMISSION_ACCESS_COURSE_LOCATION = 300;
    public static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 400;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 500;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference firebaseRef = database.getReference();
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mAuth.signInAnonymously();
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

    @SuppressWarnings("VisibleForTests")
    public void download(View view){
        /*
        EditText downloadCode = (EditText) findViewById(R.id.editText);
        String code = downloadCode.getText().toString();

        firebaseRef.child(code).setValue(1);

        Intent output = new Intent();
        setResult(RESULT_OK, output);
        */
        //firebaseRef.child(code).setValue(taskSnapshot.getDownloadUrl().toString());

        final Context context = this;
        String code = getCode();
        if(code.length() < 1 || code == null){
            Toast.makeText(MainActivity.this, "Please enter a non null code", Toast.LENGTH_SHORT).show();
            return;
        }
        final DatabaseReference mRef = firebaseRef.child(code);

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot == null || dataSnapshot.getValue() == null)
                    Toast.makeText(MainActivity.this, "No image with such code found", Toast.LENGTH_SHORT).show();
                else{
                    System.out.println(dataSnapshot.getValue().toString());
                    //String imageUrl = dataSnapshot.child("url").getValue(String.class);
                    String imageUrl = dataSnapshot.child("url").getValue(String.class);
                    System.out.println(imageUrl + "\n\n\n");
                    StorageReference imageStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                    final ProgressDialog dialog = new ProgressDialog(context);
                    try{
                        final File localFile = File.createTempFile("images", "jpg");
                        imageStorageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>()
                        {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                Uri localUri = Uri.fromFile(localFile);
                                try {
                                    MyUtilities.saveToCustomDirectory(context, getApplicationContext(), getContentResolver(),
                                            localUri, "My 2017 Project Images");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                                .addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        //Show upload progress
                                        double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                        dialog.setMessage("Downloaded " + (int)progress);
                                    }
                                });
                        ;
                        imageStorageRef.delete();
                        mRef.removeValue();
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void helpDialog(View view){

        final Context context = this;
        /*
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        final EditText et = new EditText(context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(et);

        // set dialog message
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
        */
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        builder.setTitle("How to Use")
                .setMessage("To Use this app, type a code into the text field above, then choose to " +
                        "upload or download an image.\n\n" +
                        "The 'UPLOAD IMAGE' button will then prompt you to choose an image to, and " +
                        "the selected image will be uploaded to the database will the code associated with it.\n\n" +
                        "The 'DOWNLOAD IMAGE' button will download the image associated with the " +
                        "entered code above from the database.")
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .show();

    }

    public String getCode(){
        EditText uploadCode = (EditText) findViewById(R.id.editText);
        String code = uploadCode.getText().toString();
        return code;
    }

    @SuppressWarnings("VisibleForTests")
    @Override
    //@TargetApi(Build.VERSION_CODES.M)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String code = getCode();
        if (code.length() < 1 || code == null){
            Toast.makeText(MainActivity.this, "Please enter a non null code", Toast.LENGTH_SHORT).show();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                checkPermissions();

                if (imageUri !=null) {
                    final ProgressDialog dialog = new ProgressDialog(this);
                    dialog.setTitle("Uploading image");
                    dialog.show();
                    StorageReference ref = storageReference.child(getCode());
                    //Add file
                    ref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //Dismiss dialog when done
                            dialog.dismiss();
                            //Save image info into firebase database
                            String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                            ImageData imageData = new ImageData(taskSnapshot.getDownloadUrl().toString());
                            firebaseRef.child(getCode()).setValue(imageData);
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
                                    dialog.setMessage("Uploaded " + (int)progress);
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

    public void checkPermissions(){
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
    }
}
