package viplazylmht.publicidconverter;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class ConvertTask extends AsyncTask<Void, Integer, Void> {
    private static final int SAVE_DATA_TO_DIR = -1;

    Activity context;
    private ArrayList<Work> listFilecontainsID;
    private String srcPublic, portPublic, DIR_NAME, smaliDir;
    private StringBuffer result;
    private ProgressDialog progressDialog;
    private Dialog dialog;
    private TextView tvDialog;

    public ConvertTask(Activity context, ProgressDialog progressDialog, String srcPublic, String portPublic) {
        this.context = context;
        this.portPublic = portPublic;
        this.srcPublic = srcPublic;
        listFilecontainsID = new ArrayList<>();
        listFilecontainsID.clear();
        this.progressDialog = progressDialog;
        result = new StringBuffer("");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle(context.getResources().getString(R.string.app_name));

    }

    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            is.close();
            os.close();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    private static File createDirectory(final String directoryPath) {
        File file = new File(directoryPath);
        if (!file.exists()) {
            file.setReadable(true, false);
            file.setWritable(true, true);
            if (file.mkdirs()) return file;
        }
        return null;
    }

    private static StringBuilder getAllFiles(File curDir) {
        StringBuilder stringBuilder = new StringBuilder();
        File[] filesList = curDir.listFiles();
        for (File f : filesList) {
            if (f.isFile()) {
                try {
                    stringBuilder.append(f.getCanonicalPath()).append("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (f.isDirectory())
                stringBuilder.append(getAllFiles(f));
        }
        return stringBuilder;
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
        tvDialog = dialog.findViewById(R.id.tvDialog);
    }

    public void setSmaliDir(String smaliDir) {
        this.smaliDir = smaliDir;
        DIR_NAME = new File(smaliDir).getName();
    }

    public void setListFilecontainsID(ArrayList<Work> listFilecontainsID) {
        this.listFilecontainsID = listFilecontainsID;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.setTitle(context.getResources().getString(R.string.converting));
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        progressDialog.dismiss();
        showSnackbar(context.getResources().getString(R.string.convert_done), 2000);
        showDialog();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        switch (values[0]) {
            case SAVE_DATA_TO_DIR:
                progressDialog.setMessage(context.getResources().getString(R.string.save_data_to_dir));
                break;
            default:
                progressDialog.setMessage(DIR_NAME + listFilecontainsID.get(values[0])
                        .getFilename().replaceAll(smaliDir, ""));
                progressDialog.setMax(listFilecontainsID.size());
                progressDialog.setProgress(values[0]);
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        int count = 0;
        result.append("\n[D]EBUG\t[W]ARNING\n");
        for (Work work : listFilecontainsID) {
            publishProgress(count);
            File f = new File(work.getFilename());
            if (!f.isFile()) continue;
            result.append(String.format("-------------\n%s (%s hits)\n\n", work.getFilename(), work.size()));
            for (int i = 0; i < work.size(); i++) {
                if (work.getIDname().get(i) == null) {
                    result.append(String.format("[W] Line %s: id %s was ignored because it was not found in source public.xml\n",
                            work.getIDinLine().get(i),
                            work.getIDlist().get(i)));
                    work.appendnewIDlist(null);
                    continue;
                }
                Scanner scanner = null;
                boolean contains = false;
                try {
                    DataInputStream dataFile = new DataInputStream(new FileInputStream(portPublic));
                    scanner = new Scanner(dataFile);
                    while (scanner.hasNextLine()) {
                        final String lineFromFile = scanner.nextLine();
                        if (lineFromFile.contains("name=\"" + work.getIDname().get(i) + "\"") && lineFromFile.contains("type=\"" + work.getIDtype().get(i) + "\"")) {
                            int startNewId = lineFromFile.indexOf("id=") + 4;
                            String newId = lineFromFile.substring(startNewId, lineFromFile.indexOf('"', startNewId + 1));
                            work.appendnewIDlist(newId);
                            contains = true;
                        }
                    }
                    if (contains) {
                        result.append(String.format("[D] Line %s: %s -> %s  # type=\"%s\" name=\"%s\"\n",
                                work.getIDinLine().get(i),
                                work.getIDlist().get(i),
                                work.getNewIDlist().get(i),
                                work.getIDtype().get(i),
                                work.getIDname().get(i)));
                    } else {
                        work.appendnewIDlist(null);
                        result.append(String.format("[W] Line %s: id %s with # type=\"%s\" name=\"%s\" was ignored because it was not found in port public.xml\n",
                                work.getIDinLine().get(i),
                                work.getIDlist().get(i),
                                work.getIDtype().get(i),
                                work.getIDname().get(i)));
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
            if (isCancelled()) break;
        }
        publishProgress(SAVE_DATA_TO_DIR);
        result.append(context.getResources().getString(R.string.save_new_data));
        saveNewData(smaliDir);
        return null;
    }

    private void showDialog() {
        tvDialog.setText(result.toString());
        result = new StringBuffer("");
        dialog.show();
    }

    private String getIdFromLine(String line) {
        int startPos = line.indexOf(Converter.DEFAULT_SEARCH_ID);
        if (startPos < 0) return null;
        int endPos;
        if ((endPos = line.indexOf(" ", startPos)) < 0) endPos = line.length();
        return line.substring(startPos, endPos);
    }

    private void saveNewData(String smaliDir) {
        StringBuilder result = new StringBuilder("");
        String saveDirPath = null;
        File saveDir = getSaveDataDir(smaliDir);
        if (saveDir != null) {
            try {
                saveDirPath = saveDir.getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (saveDirPath == null) {
            result.append("Canceled because this tool cant find a location to save new data.\n");
            saveBackupLog(Environment.getExternalStorageDirectory(), result.toString());
            return;
        }
        StringBuilder listFile = getAllFiles(new File(smaliDir));
        String[] lines = listFile.toString().split("\n");

        result.append(String.format("SAVE DIR: %s\nHAVE %s file to save\n", saveDirPath, String.valueOf(lines.length)));
        int count = 0;
        try {
            for (String path : lines) {
                count++;
                File newf = new File(path.replaceFirst(smaliDir, saveDirPath)).getParentFile();
                result.append(String.format("\n[%s] %s\n", String.valueOf(count), newf.getCanonicalPath() + "/" + new File(path).getName()));
                boolean isCopied = false;
                if (!newf.exists()) {
                    newf.setReadable(true, false);
                    newf.setWritable(true, true);
                }
                if (newf.exists() || newf.mkdirs()) {
                    File fi = new File(path);
                    File fo = new File(newf.getCanonicalPath(), fi.getName());
                    Work thisWork;
                    if ((thisWork = isModified(path)) != null) {
                        // copy and replace ID from path (FILE) to newf (FILE)
                        copyFileIsModified(fi, fo, thisWork, result);
                    } else {
                        // just copy path (FILE) to newf (FILE)
                        copyFileUsingStream(fi, fo);
                    }
                    isCopied = true;
                }
                if (isCopied) result.append(" Copied!\n");
                else result.append(" Canceled!\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        saveBackupLog(saveDir.getParentFile(), result.toString());
    }

    private void copyFileIsModified(File source, File dest, Work work, StringBuilder result) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(source));
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(dest));
            String line;
            int countID = 0;
            String oldId;
            while ((line = bufferedReader.readLine()) != null) {
                if (countID < work.size() && line.contains(Converter.DEFAULT_SEARCH_ID) && ((oldId = getIdFromLine(line)) != null) && oldId.length() > 6) {
                    String newId = work.getNewIDlist().get(countID);
                    if (newId == null) {
                        bufferedWriter.write(line + '\n');
                        result.append(String.format(" #%s Line %s: %s was skipped!\n",
                                String.valueOf(countID + 1),
                                work.getIDinLine().get(countID),
                                work.getIDlist().get(countID)));
                        countID++;
                        continue;
                    }
                    if (work.getIDlist().get(countID).equals(oldId)) {
                        bufferedWriter.write(line.replaceFirst(oldId, newId) + '\n');
                        result.append(String.format(" #%s Line %s: %s -> %s  # type=\"%s\" name=\"%s\"\n",
                                String.valueOf(countID + 1),
                                work.getIDinLine().get(countID),
                                work.getIDlist().get(countID),
                                work.getNewIDlist().get(countID),
                                work.getIDtype().get(countID),
                                work.getIDname().get(countID)));
                    } else bufferedWriter.write(line + '\n');
                    countID++;
                } else bufferedWriter.write(line + '\n');
            }
            if (countID == 0) {
                result.append(" No have ID to convert, see logs for details\n");
            }
            bufferedReader.close();
            bufferedWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveBackupLog(File savePath, String textLog) {
        if (!savePath.exists() && !savePath.mkdirs()) return;
        try {
            File f = new File(savePath.isDirectory() ? savePath : savePath.getParentFile(), "saveFile.log");
            OutputStream out = new FileOutputStream(f);
            DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
            Date date = new Date();
            out.write(("Public ID Converter\non [" + dateFormat.format(date) + "]\n").getBytes());
            out.write(String.format("Smali Directory: %s\n\n", smaliDir).getBytes());
            out.write(textLog.getBytes());
            out.close();
            showSnackbar(context.getResources().getString(R.string.toast_save_log, f.getCanonicalPath()), 2000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getBackupDir(String smaliDir) {
        File curDir = new File(smaliDir);
        if (!curDir.isDirectory()) return null;
        String DIR_NAME = curDir.getName();
        String projectDir = curDir.getParent();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date date = new Date();

        File temp;
        if ((temp = createDirectory(projectDir + File.separator + DIR_NAME + "-BACKUP")) != null) {
            return temp;
        }
        if ((temp = createDirectory(Environment.getExternalStorageDirectory() + File.separator + "PublicIDConverter" + File.separator + dateFormat.format(date) + File.separator + DIR_NAME + "-BACKUP")) != null) {
            return temp;
        }
        return null;
    }

    private File getSaveDataDir(String smaliDir) {
        File curDir = new File(smaliDir);
        if (!curDir.isDirectory()) return null;
        String DIR_NAME = curDir.getName();
        String projectDir = curDir.getParent();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date date = new Date();
        File temp;
        if ((temp = createDirectory(Environment.getExternalStorageDirectory() + File.separator + "PublicIDConverter" + File.separator + dateFormat.format(date) + File.separator + DIR_NAME + "-NEW")) != null) {
            return temp;
        }
        if ((temp = createDirectory(projectDir + File.separator + DIR_NAME + "-NEW")) != null) {
            return temp;
        }
        return null;
    }

    private Work isModified(String filename) {
        for (Work work : listFilecontainsID) {
            if (work.getFilename().equals(filename)) return work;
        }
        return null;
    }

    private void showSnackbar(String message, int duration) {
        ConstraintLayout container = context.findViewById(R.id.main_container);
        Snackbar.make(container, message, duration).show();
    }
}
