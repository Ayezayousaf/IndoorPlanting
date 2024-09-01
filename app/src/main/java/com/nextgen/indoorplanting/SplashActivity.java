package com.nextgen.indoorplanting;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private PermissionHelper permissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initializeComponents();

        if (permissionHelper.hasCameraPermission()) {
            checkUserAuthentication();
        } else {
            permissionHelper.requestCameraPermission();
        }
    }

    private void initializeComponents() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        permissionHelper = new PermissionHelper(this);
    }

    private void checkUserAuthentication() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            fetchUserData(currentUser.getUid());
        } else {
            navigateToLogin();
        }
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
        showToast("User data not found.");
        navigateToLogin();
    }

    private void handleUserFetchFailure() {
        showToast("Failed to fetch user data.");
        navigateToLogin();
    }

    private void handleUnknownUserType() {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionHelper.onRequestPermissionsResult(requestCode, grantResults)) {
            checkUserAuthentication();
        } else {
            if (permissionHelper.shouldShowRequestPermissionRationale()) {
                permissionHelper.showPermissionsRequiredToast();
                permissionHelper.requestCameraPermission();
            } else {
                showToast("Camera permission is required to run this app.");
                navigateToLogin();
            }
        }
    }
}
