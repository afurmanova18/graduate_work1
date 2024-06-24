package com.example.coursework;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {
    TextView backTV;
    private GridView gridView;

    private ImageAdapter adapter;

    private ArrayList<DataClass> dataList;
    private DatabaseReference databaseReference;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gal_activity);

        backTV = findViewById(R.id.backTextView);

        gridView = findViewById(R.id.gridView);
        dataList = new ArrayList<>();
        adapter = new ImageAdapter(this, dataList);
        gridView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Получаем UID текущего пользователя
            String uid = currentUser.getUid();

            // Формируем путь к изображениям текущего пользователя в базе данных Firebase
            databaseReference = FirebaseDatabase.getInstance().getReference("Images").child(uid);
        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    DataClass dataClass = dataSnapshot.getValue(DataClass.class);
                    dataList.add(dataClass);
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        backTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GalleryActivity.this, MainPage.class);
                startActivity(intent);
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DataClass selectedData = dataList.get(position);
                showEnlargeImageDialog(selectedData.getImageURL());
            }
        });

    }

    private void showEnlargeImageDialog(String imageUrl) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_image);

        ImageView enlargedImageView = dialog.findViewById(R.id.dialog_image);
        Glide.with(this).load(imageUrl).into(enlargedImageView);

        ImageView saveButton = dialog.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable drawable = (BitmapDrawable) enlargedImageView.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                    saveImageToDevice(bitmap);

            }
        });

        dialog.show();
    }


    private void saveImageToDevice(Bitmap image) {
        OutputStream fos;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                values.put(MediaStore.Images.Media.IS_PENDING, true);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

                String imageName = "IMG_" + System.currentTimeMillis() + ".jpg";
                values.put(MediaStore.Images.Media.DISPLAY_NAME, imageName);

                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    fos = getContentResolver().openOutputStream(uri);
                    if (fos != null) {
                        image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.close();

                        values.put(MediaStore.Images.Media.IS_PENDING, false);
                        getContentResolver().update(uri, values, null, null);

                        Toast.makeText(this, "Изображение сохранено", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                File imageFile = new File(imagesDir, "IMG_" + System.currentTimeMillis() + ".jpg");

                fos = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();

                MediaScannerConnection.scanFile(this, new String[]{imageFile.toString()}, null, null);

                Toast.makeText(this, "Изображение сохранено", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка", Toast.LENGTH_SHORT).show();
        }
    }


}


