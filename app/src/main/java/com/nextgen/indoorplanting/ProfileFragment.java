package com.nextgen.indoorplanting;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private TextView userTypeTextView, foodProviderNameTextView, emailTextView;
    private LinearLayout changePasswordLayout, logoutLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initializeComponents(view);
        populateUserData();

        changePasswordLayout.setOnClickListener(v -> showChangePasswordDialog());
        logoutLayout.setOnClickListener(v -> showLogoutConfirmationDialog());

        return view;
    }

    private void initializeComponents(View view) {
        mAuth = FirebaseAuth.getInstance();

        userTypeTextView = view.findViewById(R.id.user_type_text_view);
        foodProviderNameTextView = view.findViewById(R.id.food_provider_name_text_view);
        emailTextView = view.findViewById(R.id.email_text_view);
        changePasswordLayout = view.findViewById(R.id.ChangePasswordLayout);
        logoutLayout = view.findViewById(R.id.LogoutLayout);
    }

    private void populateUserData() {
        UserSessionManager session = UserSessionManager.getInstance(getContext());

        String userType = "<b>User Type:</b> " + session.getUserType();
        String userName = "<b>Name:</b> " + session.getUserName();
        String email = "<b>Email:</b> " + session.getUserEmail();

        userTypeTextView.setText(Html.fromHtml(userType));
        foodProviderNameTextView.setText(Html.fromHtml(userName));
        emailTextView.setText(Html.fromHtml(email));
    }

    private void showChangePasswordDialog() {
       Intent intent = new Intent(getContext(),ChangePasswordActivity.class);
       startActivity(intent);
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> logout())
                .setNegativeButton("No", null)
                .show();
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }


}
