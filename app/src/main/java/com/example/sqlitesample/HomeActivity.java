package com.example.sqlitesample;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.sqlitesample.databinding.ActivityHomeBinding;
import com.example.sqlitesample.sqlite.DbHelper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding activityHomeBinding;
    private Bundle dataBundle, data;

    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private Animator currentAnimator;

    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    private int shortAnimationDuration;

    private DbHelper DB;
    private SQLiteDatabase db;
    private String user;
    private String email;
    private String phone;
    private String latitude;
    private String longitude;
    private String pass;
    private String[] professions = {"Manager", "Frontend Developer", "Backend Developer", "Designer"};

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityHomeBinding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        if (activityHomeBinding.ivUpload != null) {
            activityHomeBinding.ivUpload.setVisibility(View.VISIBLE);
        }
        db = this.openOrCreateDatabase("details.db", Context.MODE_PRIVATE, null);

        //creating table for storing image
        db.execSQL("create table if not exists imagetable ( image blob )");
        activityHomeBinding.fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent fabIntent = new Intent(getApplicationContext(), Details.class);
                startActivity(fabIntent);
            }
        });
        Cursor c = db.rawQuery("select * from imagetable", null);
        if (c.moveToNext()) {
            byte[] image = c.getBlob(0);
            Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
            activityHomeBinding.ivUpload.setImageBitmap(bmp);
            Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show();
        }

        DB = new DbHelper(HomeActivity.this);
        if(DB.getProfessionsAsList().isEmpty()){
            insertProfessionValues();
        }

        if (DB.getGender().getExtras().isEmpty()){
            insertGender();
        }

        Cursor cursor = DB.fetch();

        if (cursor.moveToFirst()) {
            String user = cursor.getString(2);
            String email = cursor.getString(0);
            String phone = cursor.getString(3);
            this.user = user;
            this.email = email;
            this.phone = phone;
        }

        activityHomeBinding.nameEditText.setText(user);
        activityHomeBinding.emailEditText.setText(email);
        activityHomeBinding.phoneEditText.setText(phone);
/*

        Cursor locationcursor = DB.getlocation();
        if (cursor.moveToFirst()) {
            String latitude = locationcursor.getString(0);
            String longitude = locationcursor.getString(1);
            this.latitude = latitude;
            this.longitude = longitude;
        }
        if(DB.getlocation()!=null){
            activityHomeBinding.locationEditText.setVisibility(View.VISIBLE);
        }

        activityHomeBinding.locationEditText.setText(latitude, TextView.BufferType.valueOf(longitude));*/

        //imageView
        // Hook up clicks on the thumbnail views.
        activityHomeBinding.ivUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //zoomImageFromThumb(activityHomeBinding.ivUpload);
            }
        });

        // Retrieve and cache the system's default "short" animation time.
        shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
    }

    private void insertProfessionValues() {
        for(int i= 0;i<professions.length;i++){
            DB.insertProfessionValue(professions[i]);
        }
    }

    private void insertGender(){
        DB.insertGender();
    }

    private void zoomImageFromThumb(final View thumbView, int imageResId) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.

        activityHomeBinding.expandedImage.setImageResource(imageResId);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        activityHomeBinding.expandedImage.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        activityHomeBinding.expandedImage.setPivotX(0f);
        activityHomeBinding.expandedImage.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(activityHomeBinding.expandedImage, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(activityHomeBinding.expandedImage, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(activityHomeBinding.expandedImage, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(activityHomeBinding.expandedImage,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(shortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                currentAnimator = null;
            }
        });
        set.start();
        currentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        activityHomeBinding.expandedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentAnimator != null) {
                    currentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                                .ofFloat(activityHomeBinding.expandedImage, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(activityHomeBinding.expandedImage,
                                        View.Y, startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(activityHomeBinding.expandedImage,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(activityHomeBinding.expandedImage,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(shortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        activityHomeBinding.expandedImage.setVisibility(View.GONE);
                        currentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        activityHomeBinding.expandedImage.setVisibility(View.GONE);
                        currentAnimator = null;
                    }
                });
                set.start();
                currentAnimator = set;
            }
        });

        //videoview
        activityHomeBinding.videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams)
                        activityHomeBinding.videoView.getLayoutParams();
                params.width = metrics.widthPixels;
                params.height = metrics.heightPixels;
                params.leftMargin = 0;
                activityHomeBinding.videoView.setLayoutParams(params);
            }
        });

    }
}