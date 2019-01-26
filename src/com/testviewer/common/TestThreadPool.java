package com.testviewer.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class TestThreadPool implements Runnable{
    private long runTimes = 0;
    public void PrintTemp() throws IOException {

    }

    static public void main(String[] args)
    {
        TestThreadPool test = new TestThreadPool();
        ThreadPool pool = ThreadPool.GetInstance();
        try {
            pool.registTick(test.getClass().getMethod("PrintTemp"), test);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        try {
            pool.registTask(test);
            pool.registTask(test);
            pool.registTask(test);
            pool.registTask(test);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(1000*10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Runtime rt = Runtime.getRuntime();
        byte[] b = new byte[1024];
        try {
            Process p = rt.exec("python d:\\test.py");
            for(int i=0; i<100; i++) {
                Arrays.fill(b, (byte)0);
                InputStream in = p.getInputStream();
                in.read(b);
                String temp = new String(b, "GBK");
                temp = temp.trim();
                if (temp.length()>0) {
                    System.out.println(temp);
                }
                Common.SleepEx(100);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
