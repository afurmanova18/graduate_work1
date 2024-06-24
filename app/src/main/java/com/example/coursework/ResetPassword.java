package com.example.coursework;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


public class ResetPassword extends AppCompatActivity {

    Button resetButton;

    EditText user_email;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_pass_activity);

        resetButton = findViewById(R.id.resetButton);
        user_email = findViewById(R.id.user_email);

        mAuth = FirebaseAuth.getInstance();

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String val = user_email.getText().toString().trim();
                if(TextUtils.isEmpty(val)){
                    user_email.setError("Введите адрес электронной почты!");
                }
                else {
                    resetPassword(val);

                    }
                }
        });

    }

    private void resetPassword(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ResetPassword.this, "Ссылка для сброса пароля отправлена на ваш адрес электронной почты", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ResetPassword.this, LoginActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(ResetPassword.this, "Ошибка!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}