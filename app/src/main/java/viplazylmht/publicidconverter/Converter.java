package viplazylmht.publicidconverter;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Converter {
    private static final String DEFAULT_SEARCH_STRING = "0x7[0-9a-z]{7}";

    private ArrayList<String> originalIds = new ArrayList<String>();
    private ArrayList<String> nameType = new ArrayList<String>();
    private ArrayList<String> fd = new ArrayList<String>();
    private ArrayList<String> lineNumber = new ArrayList<String>();
    private ArrayList<String> taskList = new ArrayList<String>();

    private String srcPublic, portPublic, DIR_NAME;
    private ProgressDialog progressDialog;

    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }

    public void setProgressDialog(ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Public ID Converter");
    }

    private Context context;
    private String result;



    private AsyncTask mTask;

    public String getDIR_NAME() {
        return DIR_NAME;
    }

    public void setDIR_NAME(String DIR_NAME) {
        this.DIR_NAME = DIR_NAME;
    }

    public String generateDIR_NAME(String pathFile) {
        String dirName = null;
        for (int i =pathFile.length();i>=0;i--) {
            if (pathFile.substring(i - 1, i).equals("/")) {
                dirName = pathFile.substring(i);
                break;
            }
        }
        return dirName;
    }

    public Converter(Context context) {
        this.srcPublic = "";
        this.portPublic = "";
        this.DIR_NAME = "";
        this.context = context;
        this.result = "";

        fd.clear();
        taskList.clear();
        originalIds.clear();
        nameType.clear();
        lineNumber.clear();





    }

    public String getSrcPublic() {
        return srcPublic;
    }

    public void setSrcPublic(String srcPublic) {
        this.srcPublic = srcPublic;
    }

    public String getPortPublic() {
        return portPublic;
    }

    public void setPortPublic(String portPublic) {
        this.portPublic = portPublic;
    }

    private void updateStatusBar(String str) {
        //tv_output.append(str + "\n");
        fd.add(fd.size(),str);
    }
    public boolean convert(String smaliFile) {
        try {
            File f = new File(smaliFile);
            FileReader fileReader = new FileReader(f);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public String getResult() {
        return result;
    }

    public void findIDs(String smaliDir){
        File d = new File("/storage");

        File f = new File(smaliDir);
        StringBuffer listFileSmali = getAllSmaliFiles(f);



        String[] lines = listFileSmali.toString().split("\n");

        progressDialog.setMessage("Please wait, we are working in your files...");
        FindTask tool = new FindTask(smaliDir);
        tool.execute(lines);


    }
    public boolean findIdInFile(String smaliFile){
        originalIds.clear();
        nameType.clear();
        lineNumber.clear();

        //Read source smali

        try {
            File f = new File(smaliFile);
            FileReader fileReader = new FileReader(f);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            int count = 0;
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.contains("0x")) continue;
                int startPos = line.indexOf("0x");
                String id = line.substring(startPos, line.indexOf(" ", startPos));
                if (id.length() < 6) continue;
                lineNumber.add(String.valueOf(count));
                originalIds.add(id);
                count++;
            }
            bufferedReader.close();
            fileReader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (originalIds.isEmpty()) return false; // No ids found in this smali file

        //Search source public.xml
        Scanner scanner = null;
        try {
            //  updateStatusBar("Searching source public.xml for ids...");
            for (String id : originalIds) {
                boolean contains = false;
                DataInputStream dataFile = new DataInputStream(new FileInputStream(srcPublic));
                scanner = new Scanner(dataFile);
                while (scanner.hasNextLine()) {
                    final String lineFromFile = scanner.nextLine();
                    if (lineFromFile.contains(id)) {
                        String name = lineFromFile.substring(lineFromFile.indexOf("type="), lineFromFile.indexOf("id=") - 1);
                        nameType.add(name);
                        contains = true;
                    }
                }
                if (!contains) {
                    nameType.add("Not found in source public.xml");
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
        if (nameType.isEmpty()) return false;
        else {
            updateStatusBar("--------------");
            updateStatusBar("HOME: " + smaliFile.substring(DIR_NAME.length()));
            updateStatusBar("Found " + nameType.size() + " IDs");
        }

        return true;
    }
        private class FindTask extends AsyncTask<String, Integer, List<String>> {
        private String mess = "";
        private String smaliDir;


        public FindTask(String smaliDir) {
            this.smaliDir = smaliDir;
            DIR_NAME = generateDIR_NAME(smaliDir);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        // When all async task done
        @Override
        protected void onPostExecute(List<String> tasks) {
            progressDialog.dismiss();
            for (String line: fd){
                result += line + "\n";
            }
            fd.clear();

            msg("FIND DONE!");

            super.onPostExecute(tasks);
        }

        // After each task done
        @Override
        protected void onProgressUpdate(Integer... process) {

            progressDialog.setTitle("Please wait...");
            progressDialog.setMessage(DIR_NAME+mess.replaceAll(smaliDir,""));
            progressDialog.setProgress(process[0]);
            super.onProgressUpdate(process);
        }

        protected List<String> doInBackground(String... tasks) {
            // Get the number of task
            int count = tasks.length;
            // Initialize a new list
            List<String> taskList = new ArrayList<>(count);

            // Loop through the task
            for (int i = 0; i < tasks.length; i++) {
                String currentTask = tasks[i];
                // Do the current task here
                taskList.add(currentTask);

                File f = new File(currentTask);
                if (f.isFile()) {
                    findIdInFile(currentTask);
                    mess = currentTask;
                    show_found_ID();
                }
                // Sleep the thread
                try {
                    //mProgressDialog.setMessage(currentTask); This will show with leaked error
                    Thread.sleep(50);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // Publish the async task progress
                // Added 1, because index start from 0
                publishProgress((int) (((i + 1) / (float) count) * 100));
                // If the AsyncTask cancelled
                if (isCancelled()) {
                    break;
                }
            }

            return taskList;
        }

            private void show_found_ID() {
                if (originalIds.isEmpty()){ //updateStatusBar("Not have contain id");
                    return;}
                for (int i = 0;i<originalIds.size();i++){
                    updateStatusBar("Line "+lineNumber.get(i)+": "+ originalIds.get(i) + " # " + nameType.get(i));
                }
            }
        }

    private static StringBuffer getAllSmaliFiles(File curDir) {
        StringBuffer stringBuffer = new StringBuffer();
        File[] filesList = curDir.listFiles();

        for (File f : filesList) {
            if (f.isFile() && f.getName().contains(".smali")) {
                try {
                    stringBuffer.append(f.getCanonicalPath()).append("\n");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (f.isDirectory())
                stringBuffer.append(getAllSmaliFiles(f));
        }

        return stringBuffer;
    }
    public StringBuffer execCmd(String cmd) {
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();
            process.waitFor();
            return output;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void msg(String message) {
        Toast.makeText(this.context, message, Toast.LENGTH_LONG).show();
    }
    public void saveLog(String textLog) {
        try {
            OutputStream out = new FileOutputStream(new File(Environment.getExternalStorageDirectory(), "PIDC.log"));
            out.write(textLog.getBytes());
            out.close();
            this.msg("LOG SAVED!");
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("Test", "Setup::copyResources - "+e.getMessage());
        }


    }
}
