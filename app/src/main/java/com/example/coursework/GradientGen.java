package com.example.coursework;

import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

public class GradientGen extends AppCompatActivity {
    private Random random = new Random();
    private ImageView gradientImage;
    private int selectedColor1 = 0;
    private int selectedColor2 = 0;
    private int selectedColor3 = 0;
    private int selectedColor4 = 0;
    private ImageView horizontalGrad;
    private ImageView verticalGrad;
    private ImageView angularGrad;

    private ImageView radialGrad;
    private ImageView c1, c2, c3, c4;
    private ImageView randomBtn;

    private ImageView addToGal;

    final private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Images");
    final private StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    private Uri imageUri;
    boolean first_color = false;
    boolean second_color = false;
    boolean third_color = false;
    boolean forth_color = false;
    private int k = 0;
    private GradientOrientation gradientOrientation = GradientOrientation.HORIZONTAL;

    public enum GradientOrientation {
        HORIZONTAL,
        VERTICAL,
        ANGULAR,
        RADIAL
    }

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generate_actvity);

        gradientImage = findViewById(R.id.gradient_image);
        c1 = findViewById(R.id.color1);
        c2 = findViewById(R.id.color2);
        c3 = findViewById(R.id.color3);
        c4 = findViewById(R.id.color4);
        randomBtn = findViewById(R.id.randomBtn);
        addToGal = findViewById(R.id.addToGal);

        initializeOrientation();

        c1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setColor1(view);
            }
        });
        c2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setColor2(view);
            }
        });

        c3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setColor3(view);
            }
        });
        c4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setColor4(view);
            }
        });

        randomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = generateRandomGradientBitmap();
                gradientImage.setImageBitmap(bitmap);
                freeResources();
            }
        });

        addToGal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String userId = currentUser.getUid();
                    Bitmap bitmap = ((BitmapDrawable) gradientImage.getDrawable()).getBitmap();
                    if (saveBitmapToFile(bitmap, userId) != null) {
                        Uri fileUri = saveBitmapToFile(bitmap, userId);
                        uploadToFirebase(fileUri, userId);
                    } else {
                        Toast.makeText(GradientGen.this, "Ошибка сохранения изображения", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(GradientGen.this, "Пользователь не аутентифицирован", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


    private Uri saveBitmapToFile(Bitmap bitmap, String userId) {
        File file = new File(getFilesDir(), userId + System.currentTimeMillis() + "_image.jpg");

        try {
            OutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            outputStream.close();

            // Возвращаем Uri для сохраненного файла
            return Uri.fromFile(file);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void uploadToFirebase(Uri uri, String userId) {
        final StorageReference imageReference = storageReference.child(uri.toString()); // Используем уникальный путь для пользователя
        imageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        DataClass dataClass = new DataClass(uri.toString());
                        String key = databaseReference.child(userId).push().getKey(); // Используем путь с уникальным идентификатором пользователя
                        databaseReference.child(userId).child(key).setValue(dataClass);
                        Toast.makeText(GradientGen.this, "Сохранено!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GradientGen.this, "Ошибка!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Bitmap generateRandomGradientBitmap() {
        int width = gradientImage.getWidth();
        int height = gradientImage.getHeight();
        Bitmap gradientBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(gradientBitmap);

        int[] colors = getResources().getIntArray(R.array.color_circle);
        int numColors = 2 + random.nextInt(3);
        int[] gradientColors = new int[numColors];

        for (int i = 0; i < numColors; i++) {
            gradientColors[i] = colors[random.nextInt(colors.length)];
        }

        Paint paint = new Paint();
        Shader gradient = null;

        int gradientType = random.nextInt(3); // 0 for linear, 1 for radial, 2 for sweep

        switch (gradientType) {
            case 0: // Linear Gradient
                Point startPoint = new Point(random.nextInt(width), random.nextInt(height));
                Point endPoint = new Point(random.nextInt(width), random.nextInt(height));
                gradient = new LinearGradient(
                        startPoint.x, startPoint.y, endPoint.x, endPoint.y, gradientColors, null, Shader.TileMode.CLAMP);
                break;
            case 1: // Radial Gradient
                int radius = Math.min(width, height) / 2;
                Point center = new Point(random.nextInt(width), random.nextInt(height));
                gradient = new RadialGradient(
                        center.x, center.y, radius, gradientColors, null, Shader.TileMode.CLAMP);
                break;
            case 2: // Sweep Gradient
                Point sweepCenter = new Point(random.nextInt(width), random.nextInt(height));
                gradient = new SweepGradient(sweepCenter.x, sweepCenter.y, gradientColors, null);
                break;
        }

        paint.setShader(gradient);
        canvas.drawRect(0, 0, width, height, paint);

        return gradientBitmap;
    }

    public void create(View view) {
        int width = gradientImage.getWidth();
        int height = gradientImage.getHeight();
        Bitmap bitmap = null;
        if (first_color) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(selectedColor1);
        }
        if (second_color) {
            GradientBitmapGenerator generator = new GradientBitmapGenerator(selectedColor1, selectedColor2);
            if (gradientOrientation == GradientOrientation.RADIAL) {
                bitmap = generator.generateRadialGradientBitmap(width, height);
            } else if (gradientOrientation == GradientOrientation.ANGULAR) {
                bitmap = generator.generateAngularGradientBitmap(width, height);
            } else {
                bitmap = generator.generateGradientBitmap(width, height, gradientOrientation);

            }
        } else if (third_color) {
            GradientBitmapGenerator generator = new GradientBitmapGenerator(selectedColor1, selectedColor2, selectedColor3);
            if (gradientOrientation == GradientOrientation.RADIAL) {
                bitmap = generator.generateRadialGradientBitmap(width, height);
            } else if (gradientOrientation == GradientOrientation.ANGULAR) {
                bitmap = generator.generateAngularGradientBitmap(width, height);
            } else {
                bitmap = generator.generateGradientBitmap3(width, height, gradientOrientation);

            }
        }else if (forth_color) {
            GradientBitmapGenerator generator = new GradientBitmapGenerator(selectedColor1, selectedColor2, selectedColor3, selectedColor4);
            if (gradientOrientation == GradientOrientation.RADIAL) {
                bitmap = generator.generateRadialGradientBitmap(width, height);
            } else if (gradientOrientation == GradientOrientation.ANGULAR) {
                bitmap = generator.generateAngularGradientBitmap(width, height);
            } else {
                bitmap = generator.generateGradientBitmap3(width, height, gradientOrientation);

            }
        }

        gradientImage.setImageBitmap(bitmap);
        freeResources();

    }

    private void freeResources() {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setColor(Color.TRANSPARENT);
        c1.setBackground(gradientDrawable);
        c2.setBackground(gradientDrawable);
        c3.setBackground(gradientDrawable);
        c4.setBackground(gradientDrawable);

        c2.setVisibility(View.INVISIBLE);
        c3.setVisibility(View.INVISIBLE);
        c4.setVisibility(View.INVISIBLE);

        forth_color = false;
        selectedColor1 = 0;
        selectedColor2 = 0;
        selectedColor3 = 0;
        selectedColor4 = 0;


    }

    private void setSelectedColor1(ColorEnvelope envelope) {

        int color = envelope.getColor();
        selectedColor1 = color;

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setColor(selectedColor1);

        c1.setBackground(gradientDrawable);
        c2.setVisibility(View.VISIBLE);
    }

    private void setSelectedColor2(ColorEnvelope envelope) {

        int color = envelope.getColor();
        selectedColor2 = color;
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setColor(selectedColor2);

        c2.setBackground(gradientDrawable);
        c3.setVisibility(View.VISIBLE);
    }

    private void setSelectedColor3(ColorEnvelope envelope) {

        int color = envelope.getColor();
        selectedColor3 = color;
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setColor(selectedColor3);

        c3.setBackground(gradientDrawable);
        c4.setVisibility(View.VISIBLE);
    }

    private void setSelectedColor4(ColorEnvelope envelope) {

        int color = envelope.getColor();
        selectedColor4 = color;
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setColor(selectedColor4);

        c4.setBackground(gradientDrawable);
    }

    public void setColor1(View view) {
        new ColorPickerDialog.Builder(this)
                .setTitle("Выберите цвет")
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton(getString(R.string.confirm),
                        new ColorEnvelopeListener() {
                            @Override
                            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                                if (selectedColor1 == 0)
                                    first_color = true;
                                setSelectedColor1(envelope); // метод для сохранения выбранного цвета

                            }
                        })
                .attachAlphaSlideBar(true)
                .attachBrightnessSlideBar(true)
                .setBottomSpace(12)
                .show();
    }

    private void setColor2(View view) {
        // Второе диалоговое окно
        new ColorPickerDialog.Builder(this)
                .setTitle("Выберите цвет")
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton(getString(R.string.confirm),
                        new ColorEnvelopeListener() {
                            @Override
                            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                                if (selectedColor2 == 0) {
                                    second_color = true;
                                    first_color = false;
                                }
                                setSelectedColor2(envelope); // метод для сохранения выбранного цвета

                            }
                        })
                .attachAlphaSlideBar(true)
                .attachBrightnessSlideBar(true)
                .setBottomSpace(12)
                .show();
    }

    private void setColor3(View view) {
        // Второе диалоговое окно
        new ColorPickerDialog.Builder(this)
                .setTitle("Выберите цвет")
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton(getString(R.string.confirm),
                        new ColorEnvelopeListener() {
                            @Override
                            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                                if (selectedColor3 == 0) {
                                    third_color = true;
                                    second_color = false;
                                }
                                setSelectedColor3(envelope);

                            }
                        })
                .attachAlphaSlideBar(true)
                .attachBrightnessSlideBar(true)
                .setBottomSpace(12)
                .show();
    }

    private void setColor4(View view) {
        // Второе диалоговое окно
        new ColorPickerDialog.Builder(this)
                .setTitle("Выберите цвет")
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton(getString(R.string.confirm),
                        new ColorEnvelopeListener() {
                            @Override
                            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                                if (selectedColor4 == 0) {
                                    forth_color = true;
                                    third_color = false;
                                }
                                setSelectedColor4(envelope);

                            }
                        })
                .attachAlphaSlideBar(true)
                .attachBrightnessSlideBar(true)
                .setBottomSpace(12)
                .show();
    }


    private void initializeOrientation() {
        horizontalGrad = findViewById(R.id.imageView1);
        verticalGrad = findViewById(R.id.imageView2);
        angularGrad = findViewById(R.id.imageView3);
        radialGrad = findViewById(R.id.imageView4);

        horizontalGrad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gradientOrientation = GradientOrientation.HORIZONTAL;
                verticalGrad.setBackgroundColor(Color.TRANSPARENT);
                angularGrad.setBackgroundColor(Color.TRANSPARENT);
                radialGrad.setBackgroundColor(Color.TRANSPARENT);
                v.setBackgroundColor(getResources().getColor(R.color.gray));

            }
        });

        verticalGrad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gradientOrientation = GradientOrientation.VERTICAL;
                horizontalGrad.setBackgroundColor(Color.TRANSPARENT);
                angularGrad.setBackgroundColor(Color.TRANSPARENT);
                radialGrad.setBackgroundColor(Color.TRANSPARENT);
                v.setBackgroundColor(getResources().getColor(R.color.gray));

            }
        });

        angularGrad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gradientOrientation = GradientOrientation.ANGULAR;
                horizontalGrad.setBackgroundColor(Color.TRANSPARENT);
                verticalGrad.setBackgroundColor(Color.TRANSPARENT);
                radialGrad.setBackgroundColor(Color.TRANSPARENT);
                v.setBackgroundColor(getResources().getColor(R.color.gray));

            }
        });

        radialGrad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gradientOrientation = GradientOrientation.RADIAL;
                horizontalGrad.setBackgroundColor(Color.TRANSPARENT);
                verticalGrad.setBackgroundColor(Color.TRANSPARENT);
                angularGrad.setBackgroundColor(Color.TRANSPARENT);
                v.setBackgroundColor(getResources().getColor(R.color.gray));

            }
        });

    }

}
