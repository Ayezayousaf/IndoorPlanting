package com.nextgen.indoorplanting;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText userIDTextView, passwordTextView;
    private MaterialButton loginBtn;
    private TextView signUpLink;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressDialogHelper progressDialogHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeComponents();
        setLoginButtonListener();
        setSignUpLinkListener();
    }

    private void initializeComponents() {
        userIDTextView = findViewById(R.id.userIDTextView);
        passwordTextView = findViewById(R.id.passwordTextView);
        loginBtn = findViewById(R.id.LoginBtn);
        signUpLink = findViewById(R.id.signUpLink);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        progressDialogHelper = new ProgressDialogHelper();
    }

    private void setLoginButtonListener() {
        loginBtn.setOnClickListener(v -> loginUser());
    }

    private void setSignUpLinkListener() {
        signUpLink.setOnClickListener(v -> openSignUpActivity());
    }

    private void loginUser() {
        String email = userIDTextView.getText().toString().trim();
        String password = passwordTextView.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            userIDTextView.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordTextView.setError("Password is required");
            return;
        }

        progressDialogHelper.showProgressDialog(this, "Logging In...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressDialogHelper.dismissProgressDialog();
                    if (task.isSuccessful()) {
                        fetchUserData(mAuth.getCurrentUser().getUid());
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void fetchUserData(String userId) {
        db.collection("Users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            processUserData(document);
                        } else {
                            handleUserNotFound();
                        }
                    } else {
                        handleUserFetchFailure();
                    }
                });
    }

    private void processUserData(DocumentSnapshot document) {
        String email = document.getString("UserEmail");
        String name = document.getString("UserFullName");
        String userType = document.getString("UserRole");

        if (userType == null) {
            showToast("User type not found.");
            navigateToLogin();
            return;
        }

        if (userType.equals("Administrator") || userType.equals("Public User")) {
            saveUserSessionDetails(email, name, userType);
            navigateToMain();
        } else {
            handleUnknownUserType();
        }
    }

    private void saveUserSessionDetails(String email, String name, String userType) {
        UserSessionManager session = UserSessionManager.getInstance(this);
        session.setUserEmail(email);
        session.setUserName(name);
        session.setUserType(userType);
    }

    private void handleUserNotFound() {
        ProgressDialogHelper.dismissProgressDialog();
        showToast("User data not found.");
        navigateToLogin();
    }

    private void handleUserFetchFailure() {
        ProgressDialogHelper.dismissProgressDialog();
        showToast("Failed to fetch user data.");
        navigateToLogin();
    }

    private void handleUnknownUserType() {
        ProgressDialogHelper.dismissProgressDialog();
        showToast("Unknown user type.");
        navigateToLogin();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void openSignUpActivity() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }
}
