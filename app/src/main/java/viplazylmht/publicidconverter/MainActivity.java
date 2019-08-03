package viplazylmht.publicidconverter;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    TextView tvSrcPublic, tvPortPublic, tvSmaliDir, tvOutput, tvDialog;
    Button btnSrcPublic, btnPortPublic, btnSmaliDir, btnStart, btnFind, btnSave, btnReturn;

    private Dialog dialog;
    public ProgressDialog progressDialog;

    Converter toolbox;
    private static final int PICK_SOURCE_PUBLIC = 1;
    private static final int PICK_PORT_PUBLIC = 2;
    private static final int PICK_SMALI_DIR = 3;
    public static final int FIND_ID_ONLY = 10;
    public static final int FIND_ID_AND_CONVERT = 11;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSrcPublic = findViewById(R.id.tvSrcPublic);
        tvPortPublic = findViewById(R.id.tvPortPublic);
        tvSmaliDir = findViewById(R.id.tvSmaliDir);

        btnSrcPublic = findViewById(R.id.btnSrcPublic);
        btnPortPublic = findViewById(R.id.btnPortPublic);
        btnSmaliDir = findViewById(R.id.btnSmaliDir);
        btnFind = findViewById(R.id.btnFind);
        btnStart = findViewById(R.id.btnStart);

        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.dialog_show);
        dialog.setTitle(getResources().getString(R.string.log_title));

        tvDialog = dialog.findViewById(R.id.tvDialog);
        btnSave = dialog.findViewById(R.id.btnSave);
        btnReturn = dialog.findViewById(R.id.btnReturn);

        progressDialog = new ProgressDialog(MainActivity.this);


        toolbox = new Converter(MainActivity.this);
        toolbox.setProgressDialog(progressDialog);
        toolbox.setDialog(dialog);

        btnSrcPublic.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("org.openintents.action.PICK_FILE");
                startActivityForResult(intent, PICK_SOURCE_PUBLIC);
            }
        });
        btnPortPublic.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("org.openintents.action.PICK_FILE");
                startActivityForResult(intent, PICK_PORT_PUBLIC);
            }
        });
        btnSmaliDir.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("org.openintents.action.PICK_DIRECTORY");
                startActivityForResult(intent, PICK_SMALI_DIR);
            }
        });
        btnSave.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbox.saveLog(tvDialog.getText().toString());
                dialog.dismiss();
            }
        });
        btnReturn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //toolbox.msg(getResources().getString(R.string.log_canceled));
                dialog.dismiss();
                toolbox.showSnackbar(getResources().getString(R.string.log_canceled), 2000);
            }
        });
        btnFind.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                start_find();
            }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_convert();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        String dataPath = Objects.requireNonNull(data.getData()).getPath();
        switch (requestCode) {
            case PICK_SOURCE_PUBLIC:
                tvSrcPublic.setText(dataPath);
                toolbox.showSnackbar(getResources().getString(R.string.path_filled), 1500);
                break;
            case PICK_PORT_PUBLIC:
                tvPortPublic.setText(dataPath);
                toolbox.showSnackbar(getResources().getString(R.string.path_filled), 1500);
                break;
            case PICK_SMALI_DIR:
                tvSmaliDir.setText(dataPath);
                toolbox.showSnackbar(getResources().getString(R.string.path_filled), 1500);
                break;
                default: break;
        }
    }

    protected boolean checkVailPath(String srcPublic, String smaliDir) {
        if (srcPublic == null || smaliDir == null) return false;
        boolean done;
        done = srcPublic.endsWith(".xml") && new File(srcPublic).exists();
        done &= new File(smaliDir).isDirectory();
        return done;
    }

    protected boolean checkVailPath(String srcPublic, String portPublic, String smaliDir) {
        if (portPublic == null) return false;
        boolean done = checkVailPath(srcPublic, smaliDir);
        done &= portPublic.endsWith(".xml") && new File(portPublic).exists();
        return done;
    }
    protected void start_find(){
        String srcPublic = tvSrcPublic.getText().toString();
        String portPublic = tvPortPublic.getText().toString();
        String smaliDir = tvSmaliDir.getText().toString();

        if (!checkVailPath(srcPublic, smaliDir)) {
            toolbox.showSnackbar(getResources().getString(R.string.invaild_path), 2500);
            return;
        }
        toolbox.setData(srcPublic, portPublic, smaliDir);
        toolbox.showSnackbar(getResources().getString(R.string.start_find_ntf), 2000);
        toolbox.findIDs(FIND_ID_ONLY);
    }

    protected void start_convert() {
        String srcPublic = tvSrcPublic.getText().toString();
        String portPublic = tvPortPublic.getText().toString();
        String smaliDir = tvSmaliDir.getText().toString();

        if (!checkVailPath(srcPublic, portPublic, smaliDir)) {
            toolbox.showSnackbar(getResources().getString(R.string.invaild_path), 2500);
            return;
        }
        toolbox.setData(srcPublic, portPublic, smaliDir);
        toolbox.showSnackbar(getResources().getString(R.string.start_convert_ntf), 2000);
        toolbox.findIDs(FIND_ID_AND_CONVERT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.dismiss();
        toolbox.ProgressDismiss();
    }
}
