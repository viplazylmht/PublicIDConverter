package viplazylmht.publicidconverter;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

class Converter {
    public static final String DEFAULT_SEARCH_ID = "0x7";
    private ArrayList<Work> listFileContainID;
    private ProgressDialog progressDialog;
    private String srcPublic, portPublic, smaliDir, DIR_NAME;
    private Activity context;
    private StringBuilder result;
    private Dialog dialog;
    private TextView tvDialog;

    public Converter(Activity context) {
        this.srcPublic = "";
        this.portPublic = "";
        this.DIR_NAME = "";
        this.context = context;
        this.result = new StringBuilder("");
        listFileContainID = new ArrayList<>();
        listFileContainID.clear();
    }

    private static StringBuilder getAllSmaliFiles(File curDir) {
        StringBuilder stringBuilder = new StringBuilder();
        File[] filesList = curDir.listFiles();
        for (File f : filesList) {
            if (f.isFile() && f.getName().endsWith(".smali")) {
                try {
                    stringBuilder.append(f.getCanonicalPath()).append("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (f.isDirectory())
                stringBuilder.append(getAllSmaliFiles(f));
        }
        return stringBuilder;
    }

    public void setSmaliDir(String smaliDir) {
        this.smaliDir = smaliDir;
        this.DIR_NAME = new File(smaliDir).getName();
    }

    public void setData(String srcPublic, String portPublic, String smaliDir) {
        this.srcPublic = srcPublic;
        this.portPublic = portPublic;
        this.smaliDir = smaliDir;
    }

    public void setProgressDialog(ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle(context.getResources().getString(R.string.app_name));
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
        tvDialog = dialog.findViewById(R.id.tvDialog);
    }

    private void updateStatusBar(String str) {
        result.append(str).append("\n");
    }

    private void convertAll() {
        ConvertTask convertTask = new ConvertTask(context, progressDialog, srcPublic, portPublic);
        convertTask.setListFilecontainsID(listFileContainID);
        convertTask.setSmaliDir(smaliDir);
        convertTask.setDialog(dialog);
        convertTask.execute();
    }

    public String getResult() {
        return this.result.toString();
    }

    public void showDialog() {
        tvDialog.setText(getResult());
        this.result = new StringBuilder("");
        dialog.show();
    }

    public void findIDs(int mode) {
        listFileContainID.clear();
        StringBuilder listFileSmali = getAllSmaliFiles(new File(smaliDir));
        String[] lines = listFileSmali.toString().split("\n");

        FindTask tool = new FindTask(smaliDir, lines.length, mode);
        tool.execute(lines);
    }

    private boolean findIdInFile(String smaliFile) {
        Work smaliWorkFile = new Work(smaliFile);
        //Read source smali
        try {
            FileReader fileReader = new FileReader(new File(smaliFile));
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            int count = 0;
            while ((line = bufferedReader.readLine()) != null) {
                count++;
                if (!line.contains(DEFAULT_SEARCH_ID)) continue;
                String id = getIdFromLine(line);
                if (id.length() < 6) continue;
                smaliWorkFile.appendIDlist(id);
                smaliWorkFile.appendIDinLine(String.valueOf(count));
            }
            bufferedReader.close();
            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        if (smaliWorkFile.isEmpty()) return false; // No ids found in this smali file

        //Search source public.xml
        Scanner scanner = null;
        try {
            for (String id : smaliWorkFile.getIDlist()) {
                boolean contains = false;
                DataInputStream dataFile = new DataInputStream(new FileInputStream(srcPublic));
                scanner = new Scanner(dataFile);
                while (scanner.hasNextLine()) {
                    final String lineFromFile = scanner.nextLine();
                    if (lineFromFile.contains(id)) {
                        int startType = lineFromFile.indexOf("type=") + 6;
                        String type = lineFromFile.substring(startType, lineFromFile.indexOf('"', startType + 1));
                        int startName = lineFromFile.indexOf("name=") + 6;
                        String name = lineFromFile.substring(startName, lineFromFile.indexOf('"', startName + 1));
                        smaliWorkFile.appendIDtype(type);
                        smaliWorkFile.appendIDname(name);
                        contains = true;
                    }
                }
                if (!contains) {
                    smaliWorkFile.appendIDtype(null);
                    smaliWorkFile.appendIDname(null);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                try {
                    scanner.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        updateStatusBar("--------------");
        updateStatusBar("FILE: " + smaliFile);
        updateStatusBar("Found " + smaliWorkFile.size() + " IDs");

        listFileContainID.add(smaliWorkFile);
        return true;
    }

    private String getIdFromLine(String line) {
        int startPos = line.indexOf(Converter.DEFAULT_SEARCH_ID);
        if (startPos < 0) return null;
        int endPos;
        if ((endPos = line.indexOf(" ", startPos)) < 0) endPos = line.length();
        return line.substring(startPos, endPos);
    }

    private void show_found_ID() {
        Work currentFile = listFileContainID.get(listFileContainID.size() - 1);
        if (currentFile.size() <= 0) return;
        for (int i = 0; i < currentFile.size(); i++) {
            if (currentFile.getIDname().get(i) != null)
                updateStatusBar(String.format("Line %s: %s # type=\"%s\" name=\"%s\"",
                        currentFile.getIDinLine().get(i),
                        currentFile.getIDlist().get(i),
                        currentFile.getIDtype().get(i),
                        currentFile.getIDname().get(i)));
            else
                updateStatusBar(String.format("WARNING in line %s: id \"%s\" was not found in source public",
                        currentFile.getIDinLine().get(i),
                        currentFile.getIDlist().get(i)));
        }
    }

    private void msg(String message) {
        Toast.makeText(this.context, message, Toast.LENGTH_LONG).show();
    }

    public void saveLog(String textLog) {
        try {
            File f = new File(Environment.getExternalStorageDirectory(), "PublicIDConverter/lastlog.txt");
            OutputStream out = new FileOutputStream(f);
            DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
            Date date = new Date();
            out.write(("Public ID Converter\non [" + dateFormat.format(date) + "]\n").getBytes());
            out.write(String.format(" SRC Public Path: %s\nPort Public Path: %s\n Smali Directory: %s\n\n",
                    srcPublic, portPublic, smaliDir).getBytes());
            out.write(textLog.getBytes());
            out.close();
            showSnackbar(context.getResources().getString(R.string.toast_save_log, f.getCanonicalPath()), 2000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void ProgressDismiss() {
        progressDialog.dismiss();
    }

    public void showSnackbar(String message, int duration) {
        ConstraintLayout container = context.findViewById(R.id.main_container);
        Snackbar.make(container, message, duration).show();
    }

    private class FindTask extends AsyncTask<String, Integer, List<String>> {
        private String mess = "";
        private String smaliDir;
        private int mode, maxFile;

        public FindTask(String smaliDir, int maxFile, int mode) {
            this.smaliDir = smaliDir;
            DIR_NAME = new File(smaliDir).getName();
            this.mode = mode;
            this.maxFile = maxFile;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setTitle(context.getResources().getString(R.string.finding));
            progressDialog.setMessage("Please wait, we are working in your files...");
            progressDialog.setMax(maxFile);
            progressDialog.show();
        }

        // When all async task done
        @Override
        protected void onPostExecute(List<String> tasks) {
            super.onPostExecute(tasks);
            progressDialog.dismiss();
            if (mode == MainActivity.FIND_ID_ONLY) {
                msg(context.getResources().getString(R.string.find_done));
                showDialog();
            } else if (mode == MainActivity.FIND_ID_AND_CONVERT) {
                convertAll();
            }
        }

        // After each task done
        @Override
        protected void onProgressUpdate(Integer... process) {
            progressDialog.setMessage(DIR_NAME+mess.replaceAll(smaliDir,""));
            progressDialog.setProgress(process[0]);
            super.onProgressUpdate(process);
        }

        @Override
        protected List<String> doInBackground(String... tasks) {
            for (int i = 0; i < tasks.length; i++) {
                publishProgress(i);
                String currentTask = tasks[i];
                File f = new File(currentTask);
                if (f.isFile()) {
                    mess = currentTask;
                    if (findIdInFile(currentTask)) show_found_ID();
                }
                try {
                    Thread.sleep(50);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isCancelled()) break;
            }
            return null;
        }
    }
}
