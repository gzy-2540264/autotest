package com.testviewer.common;

import java.io.File;
import java.util.LinkedList;

public class Common {
    static public void Sleep(int seconds)
    {
        SleepEx(seconds*1000);
    }

    static public void SleepEx(int millis)
    {
        try {
            Thread.sleep(millis);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static public String GetCurDir()
    {
        File directory = new File("");//设定为当前文件夹
        try{
            return directory.getAbsolutePath();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    static public int RouteDir(String path, String expect, String unExpect, LinkedList<String> outRspList)
    {
        String[] expectList = null;
        String[] unExpectList = null;
        int hasFile = 0;

        if (expect!=null)
        {
            expectList = expect.split(",");
        }
        if (unExpect!=null)
        {
            unExpectList = unExpect.split(",");
        }

        File f = new File(path);
        if(f.isDirectory())
        {
            File[] files = f.listFiles();
            for(File file : files)
            {
                hasFile += RouteDir(file.getAbsolutePath(), expect, unExpect, outRspList);
            }
        }
        else
        {
            outRspList.add(f.getAbsolutePath());
            hasFile++;
        }
        return hasFile;
    }

    static public void main(String[] args)
    {
        LinkedList<String> outRspList = new LinkedList<String>();
        RouteDir("D:\\software", null, null, outRspList);
        for(String str : outRspList)
        {
            System.out.println(str);
        }
        System.out.println(outRspList.size());
    }
}

