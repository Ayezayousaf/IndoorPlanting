package com.nextgen.indoorplanting;

import android.app.ProgressDialog;
import android.content.Context;

public class ProgressDialogHelper {

    private static ProgressDialog progressDialog;

    public static void showProgressDialog(Context context, String message) {
        dismissProgressDialog();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public static void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
