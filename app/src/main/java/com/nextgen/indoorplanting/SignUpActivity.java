package com.nextgen.indoorplanting;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText userFullNameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Spinner signupTypeSpinner;
    private MaterialButton signUpButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initializeComponents();
        setSignUpButtonListener();
        setupToolbar();
    }

    private void initializeComponents() {
        userFullNameEditText = findViewById(R.id.UserFullNameEditText);
        emailEditText = findViewById(R.id.EmailFoodProviders);
        passwordEditText = findViewById(R.id.passwordInstitute);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordInstitute);
        signupTypeSpinner = findViewById(R.id.spinnerSignupTypes);
        signUpButton = findViewById(R.id.signUpButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void setSignUpButtonListener() {
        signUpButton.setOnClickListener(v -> showConfirmationDialog());
    }

    private void showConfirmationDialog() {
        String fullName = userFullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String userType = "Public User";

        //  String userType = signupTypeSpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(fullName)) {
            userFullNameEditText.setError("Full Name is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            return;
        }

        String message = "<b>Full Name:</b> " + fullName + "<br>"
                + "<b>Email:</b> " + email + "<br>";
               // + "<b>User Type:</b> " + userType;

        new AlertDialog.Builder(this)
                .setTitle("Confirm Sign Up")
                .setMessage(Html.fromHtml(message))
                .setPositiveButton("Confirm", (dialog, which) -> registerUser(fullName, email, password, userType))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void registerUser(String fullName, String email, String password, String userType) {
        ProgressDialogHelper.showProgressDialog(this,"Creating Account..");
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserData(user.getUid(), fullName, email, userType);
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Create Account");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle the back button action
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveUserData(String userId, String fullName, String email, String userType) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("UserFullName", fullName);
        userData.put("UserEmail", email);
        userData.put("UserRole", userType);

        db.collection("Users").document(userId)
                .set(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ProgressDialogHelper.dismissProgressDialog();
                        Toast.makeText(SignUpActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        Toast.makeText(SignUpActivity.this, "Failed to save user data: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToMain() {
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }
}
