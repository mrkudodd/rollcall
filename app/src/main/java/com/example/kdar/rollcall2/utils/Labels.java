package com.example.kdar.rollcall2.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

public class Labels {
    String mPath;

    class label{
        int num;
        String thelabel;
        public label(String label, int n){
            thelabel = label;
            num = n;
        }
    }

    ArrayList<label> labels = new ArrayList<>();
    public Labels(String path){
        mPath = path;
    }

    public boolean isEmpty()
    {
        return !(labels.size()>0);
    }

    public void add(String s,int n)
    {
        labels.add( new label(s,n));
    }

    public String get(int i) {
        Iterator<label> Ilabel = labels.iterator();
        while (Ilabel.hasNext()) {
            label l = Ilabel.next();
            if (l.num==i)
                return l.thelabel;
        }
        return "";
    }

    public int get(String s) {
        Iterator<label> Ilabel = labels.iterator();
        while (Ilabel.hasNext()) {
            label l = Ilabel.next();
            if (l.thelabel.equalsIgnoreCase(s))
                return l.num;
        }
        return -1;
    }

    public void Save() {
        try {
            File f=new File (mPath+"faces.txt");
            f.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            Iterator<label> Ilabel = labels.iterator();
            while (Ilabel.hasNext()) {
                label l = Ilabel.next();
                bw.write(l.thelabel+","+l.num);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e("error",e.getMessage()+" "+e.getCause());
            e.printStackTrace();
        }
    }

    public void Read() {
        try {

            FileInputStream fstream = new FileInputStream(
                    mPath+"faces.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    fstream));

            String strLine;
            labels = new ArrayList<>();
            // Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                StringTokenizer tokens=new StringTokenizer(strLine,",");
                String s=tokens.nextToken();
                String sn=tokens.nextToken();

                labels.add(new label(s,Integer.parseInt(sn)));
            }
            br.close();
            fstream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public int max() {
        int m=0;
        Iterator<label> Ilabel = labels.iterator();
        while (Ilabel.hasNext()) {
            label l = Ilabel.next();
            if (l.num>m) m=l.num;
        }
        return m;
    }
}
