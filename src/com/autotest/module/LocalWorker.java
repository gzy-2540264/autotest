package com.autotest.module;

import com.autotest.common.Common;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LocalWorker extends Worker implements Runnable {
    private String cmd = null;
    private InputStream in = null;
    private Method outCallbackFunc = null;
    private Method endCallbackFunc = null;
    private Object funcObj = null;

    Process process = null;
    BufferedReader outReader = null;
    Object tickHand = null;

    int TickEnd = 0;
    public LocalWorker(String cmd, InputStream in, Method outCallbackFunc, Method endCallbackFunc, Object funcObj)
    {
        this.cmd = cmd;
        this.in = in;
        this.outCallbackFunc = outCallbackFunc;
        this.endCallbackFunc = endCallbackFunc;
        this.funcObj = funcObj;

        try {
            tickHand = pool.registTick(this.getClass().getMethod("RunTick"), this);
            pool.registTask(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void RunTick(){
        if (process==null)
        {
            return;
        }

        try {
            StringBuffer buff = new StringBuffer();
            while(true) {
                long time_start = System.currentTimeMillis();
                String out = outReader.readLine();
                long time_end = System.currentTimeMillis();
                if (out!=null && out.length()>0) {
                    buff.append(out);
                    if (out.endsWith("\n")==false)
                    {
                        buff.append("\n");
                    }
                }

                // 说明当前没有读到东西
                if (time_end - time_start > 0 || out==null)
                {
                    break;
                }
            }

            if (buff.length()>0 && outCallbackFunc!=null) {
                // 说明缓冲中还有数据， tick任务不能结束
                TickEnd = 0;
                outCallbackFunc.invoke(funcObj, buff.toString());
            }
        } catch (Exception e) {}

        if (false == process.isAlive())
        {
            TickEnd ++;
        }
        if (TickEnd >= 30)
        {
            pool.unRegistTick(tickHand);
            if (endCallbackFunc !=null)
            {
                isWorkEnd = true;
                try {
                    endCallbackFunc.invoke(funcObj);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public void run() {
        try {
            process = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        BufferedInputStream bis = new BufferedInputStream(
                process.getInputStream());
        outReader = new BufferedReader(new InputStreamReader(bis));
    }
}
