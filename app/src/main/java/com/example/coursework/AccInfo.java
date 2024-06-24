package com.example.coursework;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;


public class AccInfo extends AppCompatActivity {

    TextView emailTextView, userName, backTV, emailInfo, leaveAcc, changePass;
    ImageView nameImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acc_info);

        emailTextView = findViewById(R.id.emailTextView);
        emailInfo = findViewById(R.id.emailInfo);
        userName = findViewById(R.id.userName);
        backTV = findViewById(R.id.backText);
        leaveAcc = findViewById(R.id.leaveAcc);
        changePass = findViewById(R.id.changePass);
        nameImageView = findViewById(R.id.nameImageView);

        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AccInfo.this, ChangePasswordActivity.class);
                startActivity(intent);
            }
        });

        backTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AccInfo.this, MainPage.class);
                startActivity(intent);
            }
        });

        leaveAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isLoggedIn", false); // Устанавливаем false, так как пользователь вышел из профиля
                editor.apply();
                Intent intent = new Intent(AccInfo.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });



        nameImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangeDisplayNameDialog();

            }
        });

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String email = currentUser.getEmail();
            String un = currentUser.getDisplayName();
            emailTextView.setText(email + "!");
            emailInfo.setText(email);
            userName.setText(un);
        } else {

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAuthenticationMethod();
    }
    private void checkAuthenticationMethod() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && user.getProviderData() != null) {
            for (UserInfo profile : user.getProviderData()) {
                if (GoogleAuthProvider.PROVIDER_ID.equals(profile.getProviderId())) {
                    findViewById(R.id.changePass).setVisibility(View.GONE);
                    break;
                }
            }
        }
        else {

        }
    }

    private void showChangeDisplayNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_display_name, null);
        builder.setView(dialogView);


        final EditText inputName = dialogView.findViewById(R.id.inputDisplayName);


        builder.setPositiveButton("Изменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newDisplayName = inputName.getText().toString().trim();
                if (!newDisplayName.isEmpty()) {

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(newDisplayName)
                            .build();


                    user.updateProfile(profileUpdates);
                    updateDisplayNameOnScreen(newDisplayName);

                }
            }
        });

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        AlertDialog dialog = builder.create();
        dialog.show();

        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);


        if (positiveButton != null) {
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.pink));
        }

        if (negativeButton != null) {
            negativeButton.setTextColor(ContextCompat.getColor(this, R.color.pink));
        }

    }

    private void updateDisplayNameOnScreen(String newDisplayName) {
        userName.setText(newDisplayName);
    }
}
