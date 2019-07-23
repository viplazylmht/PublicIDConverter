package viplazylmht.publicidconverter;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class RequirePermissionActivity extends AppCompatActivity {
    ConstraintLayout container, requirePermissionLayout, requireFileManagerLayout;
    Button btnRequirePerm, btnDownloadFileManager;

    private static final int STORAGE_PERMISSION_CODE = 17;
    private static final int GET_FILE_MANAGER_CODE = 17;
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_require_permission);
        btnRequirePerm = findViewById(R.id.btn_grantPermission);
        btnDownloadFileManager = findViewById(R.id.btn_getFileManager);

        container = findViewById(R.id.containerData);
        requirePermissionLayout = (ConstraintLayout) findViewById(R.id.requirePermissionLayout);
        requireFileManagerLayout = (ConstraintLayout) findViewById(R.id.requireFileManagerLayout);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // Toast.makeText(this, "Permission already GRNTED", Toast.LENGTH_SHORT).show();
            if (checkFileManagerTool()) comeToApp();
        }
        else {
            requirePermissionLayout.setVisibility(View.VISIBLE);
            requireFileManagerLayout.setVisibility(View.INVISIBLE);
        }

        // if app not have permission - open layout to get it



        btnRequirePerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requirePermission();

            }
        });

        btnDownloadFileManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=org.openintents.filemanager"));
                startActivityForResult(i, GET_FILE_MANAGER_CODE);
            }
        });


    }
    private boolean checkFileManagerTool(){
        boolean isInstalled;
        PackageManager pm = getPackageManager();
        int flags = 0;

        try {
            pm.getPackageInfo("org.openintents.filemanager", flags);
            isInstalled = true;
        } catch (PackageManager.NameNotFoundException e) {
            isInstalled = false;
        }

        return isInstalled;
    }
    private void comeToApp(){
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
        finish();
    }
    private void requirePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.require_perm_title))
                    .setMessage(getResources().getString(R.string.require_perm_info))
                    .setPositiveButton(getResources().getString(R.string.ok_btn), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(RequirePermissionActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.cancel_btn), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();

        }
        else {
            ActivityCompat.requestPermissions(RequirePermissionActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                showSnackbar(getResources().getString(R.string.permission_granted), 1000);
                Toast.makeText(this, getResources().getString(R.string.permission_granted), Toast.LENGTH_SHORT).show();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (checkFileManagerTool()){
                    comeToApp();
                }
                else {
                    requireFileManagerLayout.setVisibility(View.VISIBLE);
                    requirePermissionLayout.setVisibility(View.INVISIBLE);
                }

            }
            else {
                Toast.makeText(this, getResources().getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                showSnackbarOpenAppSetting(getResources().getString(R.string.open_in_setting_ntf), 1100);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_FILE_MANAGER_CODE) {
            if (checkFileManagerTool()) {
                Toast.makeText(this, getResources().getString(R.string.app_installed_ntf), Toast.LENGTH_SHORT).show();
                comeToApp();
            }
            else {
                showSnackbar(getResources().getString(R.string.downloading_ntf), 1000);
            }
        }

    }

    private void showSnackbar(String message, int duration)
    {
        // Create snackbar
        final Snackbar snackbar = Snackbar.make(container , message, duration);

        // Set an action on it, and a handler

        /*
        snackbar.setAction("DISMISS", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        */
        snackbar.show();
    }
    private void showSnackbarOpenAppSetting(String message, int duration)
    {
        // Create snackbar
        final Snackbar snackbar = Snackbar.make(container , message, duration);

        // Set an action on it, and a handler
        snackbar.setAction(getResources().getString(R.string.open_setting), new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent openSetting = new Intent();
                openSetting.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                openSetting.setData(uri);
                startActivity(openSetting);
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }
}