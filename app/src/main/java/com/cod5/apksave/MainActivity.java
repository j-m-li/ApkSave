/*
 * 25 June MMXXIV PUBLIC DOMAIN by JML
 *
 * The authors disclaim copyright to this source code
 *
 */
package com.cod5.apksave;

import static java.util.concurrent.TimeUnit.*;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.system.Os;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.cod5.apksave.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 189;

    static {
        System.loadLibrary("natlib");
    }
    private String txt;
    private Handler handler = null;
    private boolean running = false;
    private ScheduledExecutorService scheduler;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scheduler = Executors.newScheduledThreadPool(1);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.sampleText.setText(R.string.sign_you_apk);
        binding.button.setOnClickListener(this::onClickMe);
        if (hasWriteStoragePermission()) {
            binding.sampleText.setText(R.string.sign_you_apk);
        }
        askAllFilesPermission();
        listDownloadsFiles();
    }

    private void listDownloadsFiles() {
        File d = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        final PackageManager pm = getPackageManager();

        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        int i = 0;
        for (ApplicationInfo pi : packages) {
            if ((pi.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                RadioButton r;
                r = new RadioButton(this);
                r.setText(pi.sourceDir);
                r.setId(i);
                binding.radio.addView(r, i);
                i++;
            }
        }

        binding.passwd.setText(d.getAbsolutePath() + "/out/");

    }

    /* print result of permission request */
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void askAllFilesPermission() {
        if (Build.VERSION.SDK_INT >= 30) {
            if (hasAllFilesPermission()) {
                return;
            }
            startActivity(
                    new Intent(
                            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                            Uri.parse("package:" + getPackageName())
                    )
            );
        }
    }


    @RequiresApi(Build.VERSION_CODES.R)
    private boolean hasAllFilesPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return Environment.isExternalStorageManager();
        }
        return true;
    }

    private boolean hasWriteStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE
                );
                return false;
            }
        }
        return true;

    }

    public void onClickMe(View v) {

        if (binding.radio.getChildCount() < 1) {
            listDownloadsFiles();
            Toast.makeText(MainActivity.this, "cannot find any .zip file.", Toast.LENGTH_LONG).show();
            return;
        } else {
            //binding.sampleText.setText(R.string.start);
            //Toast.makeText(MainActivity.this, "Start signing", Toast.LENGTH_LONG).show();
        }
        try {
            InputStream in;
            OutputStream out;
            AssetManager assetManager = getAssets();
            {
                int id = binding.radio.getCheckedRadioButtonId();
                if (id >= 0) {
                    binding.sampleText.setText(R.string.start);
                    RadioButton rdb = binding.radio.findViewById(id);
                    binding.sampleText.setText(runCommand(binding.passwd.getText().toString(), "hwzip", rdb.getText().toString()));
                    //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
          /*              File cert = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + rdb.getText().toString().substring(rdb.getText().toString().lastIndexOf("/")));
                        Log.d("SignV2:", cert.getPath() + " " + binding.passwd.getText().toString() + rdb.getText().toString());
                        if (true || !cert.exists()) {
                            in = new FileInputStream(rdb.getText().toString());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                out = Files.newOutputStream(cert.toPath());
                            } else {
                                out = new FileOutputStream(cert);
                            }
                            copyFile(in, out);
                            out.close();
                        }

           */
                    //} else {
                      //  File cert = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/certificate.jks");
                       // Log.d("Sign(v1):", cert.getPath() + " " + binding.passwd.getText().toString() + " " + rdb.getText().toString());
                    //}
                    binding.sampleText.setText(R.string.apk_signed);

                } else {
                    //binding.sampleText.setText(R.string.please_select_an_apk);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getCause() != null) {
                //binding.sampleText.setText(e.getCause().toString());
            } else {
                //binding.sampleText.setText(R.string.failed);
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == RESULT_OK) {
            binding.sampleText.setText(String.format("%s/app.apk", this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)));
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
    protected String runCommand(String workingDir, String cmd, String src) {
        if (running) {
            return "Already running... please wait.";
        }

        handler = new Handler(Looper.getMainLooper());

        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                String expath = getApplicationContext().getApplicationInfo().nativeLibraryDir;
                running = true;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (running) {
                                binding.sampleText.setText(binding.sampleText.getText() + ".");
                                handler.postDelayed(this, 500);
                            }
                        } catch (Exception e) {

                        }
                    }
                });

                try {
                    //binding.passwd.setText(expath + "/lib" + "hwzip" + ".so");
                    txt = exec(expath + "/lib" + "hwzip" + ".so", workingDir, "extract", src, "", "", "");
                    txt = exec(expath + "/lib" + "hwzip" + ".so", workingDir, "create", "../extracted.apk", "-m", "store", ".") + txt;

                } catch (Exception e) {
                    txt = "Error 5674";
                }
                running = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.sampleText.setText(txt);
                        //Toast.makeText(MainActivity.this, "Success!", Toast.LENGTH_LONG).show();
                    }
                });
             }
        }, 1, MILLISECONDS);

        return "Running unzip...";
    }

    public native String exec(String cmd,
                              String pwd,
                              String arg1,
                              String arg2,
                              String arg3,
                              String arg4,
                              String arg5);
}