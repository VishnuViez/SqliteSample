package com.example.sqlitesample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.example.sqlitesample.databinding.ActivityDetailsBinding;
import com.example.sqlitesample.sqlite.DbHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Details extends AppCompatActivity implements LocationListener, AdapterView.OnItemSelectedListener {
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int PICK_IMAGE_REQUEST = 2;
    private static final int REQUEST_VIDEO_CAPTURE = 3;
    private ActivityDetailsBinding activityDetailsBinding;
    private DbHelper DB;
    Context context;
    String address;
    byte[] image;
    ContentValues values = new ContentValues();
    Bitmap selectedImage;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityDetailsBinding = DataBindingUtil.setContentView(this, R.layout.activity_details);
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        DB = new DbHelper(Details.this);
        activityDetailsBinding.spinner.setPrompt("Select an option");
        activityDetailsBinding.spinner.setOnItemSelectedListener(this);
        //loading spinner data from database
        loadSpinnerData();
        address = DB.getlocation().toString();


        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Display the location in the TextView
                        activityDetailsBinding.addressEditText.setText(String.format("Current address: %f,%f",
                                location.getLatitude(), location.getLongitude()));
                        SQLiteDatabase db = DB.getWritableDatabase();
                        //String sql = "INSERT INTO location (latitude, longitude) VALUES (?)";
                        //db.execSQL(sql, new String[]{String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude())});
                    }
                });

        if (ContextCompat.checkSelfPermission(Details.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Details.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        }

        //Image button click listener
        activityDetailsBinding.uploadImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        //video button click listener
        activityDetailsBinding.uploadVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an Intent to open the video gallery or camera app
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                intent.setType("video/*");
                startActivityForResult(intent, REQUEST_VIDEO_CAPTURE);
            }
        });
        activityDetailsBinding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Details.this, HomeActivity.class);

                activityDetailsBinding.genderRB.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        RadioButton selectedRadioButton = group.findViewById(checkedId);
                        String selectedRadioButtonValue = selectedRadioButton.getText().toString();
                        // Open a connection to the SQLite database
                        SQLiteDatabase db = DB.getWritableDatabase();
                        String sql = "INSERT INTO tableGender (selected) VALUES (?)";
                        db.execSQL(sql, new String[]{selectedRadioButtonValue});
                        Log.d("RadioGroupInsert", "Inserted value: " + selectedRadioButtonValue);
                        // Insert the selected radio button value into the "options" table
                        values.put("gender", selectedRadioButtonValue);
                        db.insert("radio", null, values);
                        // Close the connection to the database
                        db.close();
                    }
                });

                startActivity(intent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Address picker
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(this, data);
            String address = String.valueOf(place.getAddress());
            // Set the address in the EditText
            activityDetailsBinding.addressEditText.setText(address);
        }
        //Image picker
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImage = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                activityDetailsBinding.ivUpload.setVisibility(View.VISIBLE);
                activityDetailsBinding.ivUpload.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            //image.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] imageByteArray = stream.toByteArray();

            SQLiteDatabase db = DB.getWritableDatabase();
            // Add the image to the ContentValues object
            values.put("image", String.valueOf(image));

            // Insert the values into the table
            db.insert("imagetable", null, values);
            String sql = "INSERT INTO imagetable (image) VALUES (?)";
            db.execSQL(sql, new byte[][]{imageByteArray});
            try {
                db.execSQL(sql, new byte[][]{imageByteArray});
            } catch (SQLException e) {
                Log.e("SaveButton", "Error saving image: " + e.getMessage());
            }
        }
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            // Get the video URI from the result Intent
            Uri videoUri = data.getData();

            // Read the video file into a byte array
            byte[] videoData = new byte[0];
            try {
                videoData = readVideoFile(videoUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Connect to the database and execute an INSERT query
            DB.getWritableDatabase();

            values.put("video", videoData);
            DB.insertVideo("insertvideo", null, values);


            // Show a message when the upload is successful
            activityDetailsBinding.tvUploadVideo.setVisibility(View.VISIBLE);
            Toast.makeText(Details.this, "Video uploaded successfully", Toast.LENGTH_SHORT).show();
        }
    }

    //Profession spinner function
    private void loadSpinnerData() {
        DbHelper db = new DbHelper(this);
        db.getReadableDatabase();
        List<String> labels = db.getProfessionsAsList();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activityDetailsBinding.spinner.setAdapter(dataAdapter);
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "" + location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_SHORT).show();
        activityDetailsBinding.addressEditText.setText(String.format("Latitude: %f, Longitude: %f", location.getLatitude(), location.getLongitude()));

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //Image function
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    //video function
    private byte[] readVideoFile(Uri videoUri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(videoUri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String label = parent.getItemAtPosition(position).toString();
        // Get the selected item from the Spinner
        String selectedItem = parent.getItemAtPosition(position).toString();
        activityDetailsBinding.spinner.setPrompt("Select an option");
        // Set the text of the TextView to the selected item
        activityDetailsBinding.label.setText(selectedItem);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void getlocation(){

    }

}