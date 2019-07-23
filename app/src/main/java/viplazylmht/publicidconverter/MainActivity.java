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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSrcPublic = findViewById(R.id.tvSrcPublic);
        tvPortPublic = findViewById(R.id.tvPortPublic);
        tvSmaliDir = findViewById(R.id.tvSmaliDir);
        tvOutput = findViewById(R.id.tv_output);

        btnSrcPublic = findViewById(R.id.btnSrcPublic);
        btnPortPublic = findViewById(R.id.btnPortPublic);
        btnSmaliDir = findViewById(R.id.btnSmaliDir);
        btnFind = findViewById(R.id.btnFind);
        btnStart = findViewById(R.id.btnStart);

        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.dialog_show);
        dialog.setTitle("PIDC: Message LOG");

        tvDialog = dialog.findViewById(R.id.tvDialog);
        btnSave = dialog.findViewById(R.id.btnSave);
        btnReturn = dialog.findViewById(R.id.btnReturn);

        progressDialog = new ProgressDialog(MainActivity.this);


        toolbox = new Converter(MainActivity.this);
        toolbox.setProgressDialog(progressDialog);
        btnSrcPublic.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("org.openintents.action.PICK_FILE");
                startActivityForResult(intent, PICK_SOURCE_PUBLIC);
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
                toolbox.msg("Don't save, user cancel");
                dialog.dismiss();
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
        btnFind.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {

                start_find();
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
                break;
            case PICK_PORT_PUBLIC:
                tvPortPublic.setText(dataPath);
                break;
            case PICK_SMALI_DIR:
                tvSmaliDir.setText(dataPath);
                break;

                default: break;


        }
    }
    protected boolean checkExist(String... args){
        for (String arg : args){
            File f = new File(arg);
            if (!f.exists()) return false;
        }
        return true;
    }


    protected void start_find(){
        String srcPublic = tvSrcPublic.getText().toString();
        String portPublic = tvPortPublic.getText().toString();
        String smaliDir = tvSmaliDir.getText().toString();

        if (!checkExist(srcPublic, smaliDir)) return;

        toolbox.setSrcPublic(srcPublic);
        toolbox.setPortPublic(portPublic);
        toolbox.setDIR_NAME(toolbox.generateDIR_NAME(smaliDir));

        toolbox.findIDs(smaliDir);
        tvDialog.setText(toolbox.getResult());
        dialog.show();
    }

}
