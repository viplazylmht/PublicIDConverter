package viplazylmht.publicidconverter;


import java.util.ArrayList;

public class Work {

    private String filename;
    private ArrayList<String> IDlist = new ArrayList<String>();
    private ArrayList<String> IDinLine = new ArrayList<String>();
    private ArrayList<String> IDtype = new ArrayList<String>();
    private ArrayList<String> IDname = new ArrayList<String>();
    private ArrayList<String> newIDlist = new ArrayList<String>();


    public Work(String filename) {
        this.filename = filename;
        IDname.clear();
        IDlist.clear();
        IDtype.clear();
        IDinLine.clear();
        newIDlist.clear();
    }

    public String getFilename() {
        return filename;
    }

    public ArrayList<String> getIDlist() {
        return IDlist;
    }

    public ArrayList<String> getIDinLine() {
        return IDinLine;
    }

    public ArrayList<String> getIDtype() {
        return IDtype;
    }

    public ArrayList<String> getNewIDlist() {
        return newIDlist;
    }

    public ArrayList<String> getIDname() {
        return IDname;
    }

    public int size() {
        return IDlist.size();
    }

    public void appendIDlist(String id) {
        IDlist.add(id);
    }

    public void appendnewIDlist(String newId) {
        newIDlist.add(newId);
    }

    public void appendIDinLine(String lineNumber) {
        // IDinLine.add(lineNumber);
        IDinLine.add(IDinLine.size(), lineNumber);
    }

    public void appendIDtype(String type) {
        IDtype.add(type);
    }

    public void appendIDname(String name) {
        IDname.add(name);
    }

    public boolean isEmpty() {
        return (size() <= 0);
    }

    @Override
    public String toString() {
        return "Work{" +
                "filename='" + filename + '\'' +
                ", IDlist=" + IDlist +
                ", IDinLine=" + IDinLine +
                ", IDtype=" + IDtype +
                ", IDname=" + IDname +
                '}';
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
