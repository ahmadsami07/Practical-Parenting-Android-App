/*This activity is to add the name of the child
and add the portrait for the child*/
//learn how to take photo: https://www.youtube.com/watch?v=RaOyw84625w
//learn how to save image and use: https://stackoverflow.com/questions/6612263/converting-input-stream-into-bitmap

package com.e.practicalparentlavateam.UI;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import com.e.practicalparentlavateam.Model.Children;
import com.e.practicalparentlavateam.Model.ChildrenManager;
import com.e.practicalparentlavateam.R;

import com.google.gson.Gson;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ChildDetails extends AppCompatActivity {
    private EditText editName;
    private ChildrenManager children;
    private ImageView image;
    private String path;
    private final static int SELECT_FROM_GALLERY = 007;

    public static Intent makeIntentForAdd(Context context) {
        return new Intent(context, ChildDetails.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_details3);
        Toolbar toolbar = findViewById(R.id.add_child_toolbar);
        setSupportActionBar(toolbar);

        children = ChildrenManager.getInstance();
        editName = findViewById(R.id.edit_name);

        takePhotoForChild();
        setupButtonCancel();
        setupButtonOk();
        setupButtonGallery();

    }



    //https://www.youtube.com/watch?v=RaOyw84625w
    private void takePhotoForChild() {
        if(ContextCompat.checkSelfPermission(ChildDetails.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ChildDetails.this,
                    new String[]{
                            Manifest.permission.CAMERA
                    },
                    100);
        }

        image = findViewById(R.id.child_image);
        Button takePhoto = findViewById(R.id.take_photo);

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 100);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null) {
            if (requestCode == 100) {
                Bitmap captureImage = (Bitmap) data.getExtras().get("data");
                image.setImageBitmap(captureImage);
            }
            //If the resultcode is result_OK, and we send in the request code select from gallery
            //which was predetermined, then we understand that the data send is an image from the gallery
            else if (requestCode == SELECT_FROM_GALLERY
                    && resultCode == RESULT_OK
                    && data != null) {
                //Now, we create an inputstream from the URI data we obtained. Then we decade it, and set on image.
                //https://stackoverflow.com/questions/6612263/converting-input-stream-into-bitmap
                try {
                    InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
                    Bitmap captureImage = BitmapFactory.decodeStream(inputStream);
                    image.setImageBitmap(captureImage);
                } catch (FileNotFoundException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    private void setupButtonCancel() {
        Button button = findViewById(R.id.button_cancel);
        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ChildDetails.this.finish();
                    }
                }
        );
    }

    private void setupButtonOk() {
        Button button = findViewById(R.id.button_save);
        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Extract data from screen
                        String name = editName.getText().toString();
                        if(name.equals("")){
                            Toast.makeText(ChildDetails.this,R.string.hint_for_name,Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }

                        //https://stackoverflow.com/questions/17674634/saving-and-reading-bitmaps
                        // -images-from-internal-memory-in-android
                        BitmapDrawable drawable = (BitmapDrawable) image.getDrawable();
                        Bitmap bitmapImage = drawable.getBitmap();
                        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
                        // path to /data/data/yourapp/app_data/imageDir
                        File directory = contextWrapper.getDir("imageDir", Context.MODE_PRIVATE);
                        // Create imageDir
                        File myPath=new File(directory, editName.getText().toString() + ".jpg");

                        FileOutputStream fileOutputStream = null;
                        try {
                            fileOutputStream = new FileOutputStream(myPath);
                            // Use the compress method on the BitMap object to write image to the OutputStream
                            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        } finally {
                            try {
                                fileOutputStream.close();
                            } catch (IOException exception) {
                                exception.printStackTrace();
                            }
                        }
                        path = directory.getAbsolutePath();

                        // Create new data object
                        children = ChildrenManager.getInstance();
                        children.add(new Children(name));
                        children.setPath(path);
                        saveChildDetails();
                        finish();
                    }
                }
        );




    }
/*
This button allows us to setup a button to access the gallery, while selecting a picture.
 */
    private void setupButtonGallery() {
        Button galleryButton=findViewById(R.id.gallery_button);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //This allows us to sent a particular type of intent i.e. pick from the gallery.
                //This customizes the intent's data we are sending in.
                //We also send in the particular request code, so that we can select from the gallery.
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_FROM_GALLERY);


            }
        });
    }


    public void saveChildDetails(){
        SharedPreferences prefs = this.getSharedPreferences("childPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(children);
        editor.putString("childPrefs", json);
        System.out.println(json);
        editor.commit();     // This line is IMPORTANT !!!
    }
}