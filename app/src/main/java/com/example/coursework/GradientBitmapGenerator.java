package com.example.coursework;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;

public class GradientBitmapGenerator {

    private int selectedColor1;
    private int selectedColor2;
    private int selectedColor3;
    private int selectedColor4;

    private int[] colors;

    private float[] positions;

    public GradientBitmapGenerator(int selectedColor1, int selectedColor2) {
        this.selectedColor1 = selectedColor1;
        this.selectedColor2 = selectedColor2;
        colors = new int[]{selectedColor1, selectedColor2};

    }
    public GradientBitmapGenerator(int selectedColor1, int selectedColor2, int selectedColor3) {
        this.selectedColor1 = selectedColor1;
        this.selectedColor2 = selectedColor2;
        this.selectedColor3 = selectedColor3;
        colors = new int[]{selectedColor1, selectedColor2, selectedColor3};
        positions = new float[]{0f, 0.5f, 1f};

    }
    public GradientBitmapGenerator(int selectedColor1, int selectedColor2, int selectedColor3, int selectedColor4) {
        this.selectedColor1 = selectedColor1;
        this.selectedColor2 = selectedColor2;
        this.selectedColor3 = selectedColor3;
        this.selectedColor4 = selectedColor4;
        colors = new int[]{selectedColor1, selectedColor2, selectedColor3,selectedColor4};
        positions = new float[]{0f, 0.33f, 0.66f, 1f};
    }

    public Bitmap generateAngularGradientBitmap(int width, int height) {

    Bitmap gradientBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    // Определение центра
    float centerX = width / 2.0f;
    float centerY = height / 2.0f;

    // Максимальное расстояние от центра до углов
    float maxRadius = (float) Math.hypot(width / 2.0f, height / 2.0f);

    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            // Вычисление угла в радианах от центра до текущей точки
            double angle = Math.atan2(y - centerY, x - centerX);
            if (angle < 0) {
                angle += 2 * Math.PI;
            }

            // Нормализация угла от 0 до 1
            float normalizedAngle = (float) (angle / (2 * Math.PI));

            // Определение соответствующего цвета на основе нормализованного угла
            int startColorIndex = (int) (normalizedAngle * (colors.length - 1));
            int endColorIndex = startColorIndex + 1;

            if (endColorIndex >= colors.length) {
                endColorIndex = 0;
            }

            float ratio = (normalizedAngle * (colors.length - 1)) - startColorIndex;

            int startColor = colors[startColorIndex];
            int endColor = colors[endColorIndex];

            // Интерполяция цветов
            int a = (int) ((1 - ratio) * ((startColor >> 24) & 0xff) + ratio * ((endColor >> 24) & 0xff));
            int r = (int) ((1 - ratio) * ((startColor >> 16) & 0xff) + ratio * ((endColor >> 16) & 0xff));
            int g = (int) ((1 - ratio) * ((startColor >> 8) & 0xff) + ratio * ((endColor >> 8) & 0xff));
            int b = (int) ((1 - ratio) * (startColor & 0xff) + ratio * (endColor & 0xff));

            int color = (a << 24) | (r << 16) | (g << 8) | b;
            gradientBitmap.setPixel(x, y, color);
        }
    }

    return gradientBitmap;
    }
    public Bitmap generateRadialGradientBitmap(int width, int height) {

        Bitmap gradientBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(gradientBitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Определение центра и радиуса градиента
        float centerX = width / 2.0f;
        float centerY = height / 2.0f;
        float radius = Math.max(width, height) / 2.0f;

        // Создание радиального градиента
        RadialGradient radialGradient = new RadialGradient(
                centerX,
                centerY,
                radius,
                colors,
                null,
                Shader.TileMode.CLAMP
        );

        paint.setShader(radialGradient);

        // Отрисовка градиента на холсте
        canvas.drawRect(0, 0, width, height, paint);

        return gradientBitmap;
    }

    public Bitmap generateGradientBitmap(int width, int height, GradientGen.GradientOrientation o) {
        Bitmap gradientBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Извлечение ARGB-компонентов из выбранных цветов
        int startA = (selectedColor1 >> 24) & 0xff;
        int startR = (selectedColor1 >> 16) & 0xff;
        int startG = (selectedColor1 >> 8) & 0xff;
        int startB = selectedColor1 & 0xff;

        int endA = (selectedColor2 >> 24) & 0xff;
        int endR = (selectedColor2 >> 16) & 0xff;
        int endG = (selectedColor2 >> 8) & 0xff;
        int endB = selectedColor2 & 0xff;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float ratio;
                if (o == GradientGen.GradientOrientation.HORIZONTAL) {
                    ratio = (float) x / (float) width;
                } else {
                    ratio = (float) y / (float) height;
                }

                int a = (int) (startA + ratio * (endA - startA));
                int r = (int) (startR + ratio * (endR - startR));
                int g = (int) (startG + ratio * (endG - startG));
                int b = (int) (startB + ratio * (endB - startB));

                int color = (a << 24) | (r << 16) | (g << 8) | b;
                gradientBitmap.setPixel(x, y, color);
            }
        }

        return gradientBitmap;
    }
    public Bitmap generateGradientBitmap3(int width, int height, GradientGen.GradientOrientation o) {
        Bitmap gradientBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(gradientBitmap);

        LinearGradient gradient;

        if (o == GradientGen.GradientOrientation.HORIZONTAL){

            gradient = new LinearGradient(
                0, 0, width, 0, colors, positions, Shader.TileMode.CLAMP);
        }
        else{
            gradient = new LinearGradient(
                    0, 0, 0, height, colors, positions, Shader.TileMode.CLAMP);
        }

        Paint paint = new Paint();
        paint.setShader(gradient);
        canvas.drawRect(0, 0, width, height, paint);

        return gradientBitmap;
    }

}
