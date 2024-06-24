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
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    EditText current_password, new_password;
    Button changeBtn;

    private FirebaseAuth mAuth;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password);


        current_password = findViewById(R.id.current_password);
        new_password = findViewById(R.id.new_password);
        changeBtn = findViewById(R.id.changeBtn);


        mAuth = FirebaseAuth.getInstance();

        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

   }

    private void changePassword() {
        String currentPassword = current_password.getText().toString().trim();
        String newPassword = new_password.getText().toString().trim();

        if (TextUtils.isEmpty(currentPassword)) {
            current_password.setError("Введите текущий пароль");
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            new_password.setError("Введите новый пароль");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ChangePasswordActivity.this, "Пароль успешно изменен", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(ChangePasswordActivity.this, "Не удалось изменить пароль. Пожалуйста, попробуйте еще раз", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

}
