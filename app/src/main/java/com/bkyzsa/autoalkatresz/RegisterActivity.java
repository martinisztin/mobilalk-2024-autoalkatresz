package com.bkyzsa.autoalkatresz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private static final String LOG_TAG = RegisterActivity.class.getName();
    private static final String PREF_KEY = Objects.requireNonNull(MainActivity.class.getPackage()).toString();
    private static final int SECRET_KEY = 99;

    EditText emailET;
    EditText passwordET;
    EditText passwordAgainET;
    EditText addressET;

    private SharedPreferences preferences;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // firebase instance
        mAuth = FirebaseAuth.getInstance();

        // get all text property
        emailET = findViewById(R.id.emailTextBox);
        passwordET = findViewById(R.id.passwordTextBox);
        passwordAgainET = findViewById(R.id.passwordAgainTextBox);
        addressET = findViewById(R.id.postAddressTextBox);

        // stupid shit for points
        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        String userName = preferences.getString("userName", "");
        String password = preferences.getString("password", "");

        emailET.setText(userName);
        passwordET.setText(password);
        passwordAgainET.setText(password);
    }

    public void register(View view) {
        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();
        String passwordAgain = passwordAgainET.getText().toString();
        String address = addressET.getText().toString();


        if(email.isEmpty() || password.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Minden mező kitöltése kötelező!", Toast.LENGTH_LONG).show();
            return;
        }

        if(!password.equals(passwordAgain)) {
            Toast.makeText(this, "A két jelszó nem egyezik!", Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if(task.isSuccessful()) {
                Toast.makeText(this, "Sikeres regisztráció!", Toast.LENGTH_LONG).show();
                startShopping();
            }
            else {
                Toast.makeText(this, "Sikertelen regisztráció!", Toast.LENGTH_LONG).show();
            }
        });

    }

    public void back(View view) {
        finish();
    }

    public void startShopping() {
        Intent intent = new Intent(this, ShopActivity.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
    }
}