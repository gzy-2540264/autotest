package com.autotest.ui;

import com.autotest.common.Common;
import com.autotest.common.Msg;
import com.autotest.common.MsgCom;
import com.autotest.common.MsgQueue;
import com.autotest.module.LocalWorker;
import com.autotest.module.TestcaseResult;
import com.autotest.module.TestcaseRunGroup;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.swing.tree.TreeNode;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;


public class TestcaseNode implements TreeNode, MsgCom {
    @Override
    public String GetComId() {
        return mapPath;
    }

    public enum TESTCASE_STATUS{
        TESTCASE_IDLE,
        TESTCASE_PASS,
        TESTCASE_FAIL,
        TESTCASE_ERROR,
        TESTCASE_RUNNING
    };

    private String mapPath = null;
    private boolean isSelect = false;
    private boolean isLeaf = false;
    private TestcaseNode parent = null;
    private TESTCASE_STATUS status = TESTCASE_STATUS.TESTCASE_IDLE;
    private String runCmd = null;
    private String checkPartten = null;
    private int runTimeout = 600;
    private int failRetryTimes = 3;
    private int hasRunTime = 0;
    public static ArrayList<TestcaseNode> allNodeList = null;
    private Vector<TestcaseNode> subNodeList = new Vector<TestcaseNode>();
    private ArrayList<TestcaseResult> results = new ArrayList<TestcaseResult>();

    private StringBuffer runLogBuff = null;

    MsgQueue queue = MsgQueue.GetInstance();
    public TestcaseNode(String mapPath, TestcaseNode parent)
    {
        File f = new File(mapPath);
        this.mapPath = mapPath;
        this.parent = parent;
        if(f.isDirectory())
        {
            isLeaf = false;
            File[] files =  f.listFiles();
            for(File fp : files)
            {
                if (fp.isFile())
                {
                    if( fp.getName().startsWith("test_") ==false || fp.getName().endsWith(".py")==false)
                    {
                        continue;
                    }
                }
                TestcaseNode  subNode = new TestcaseNode(fp.getAbsolutePath(), this);
                subNodeList.add(subNode);
                addToLib(subNode);
            }
        }
        else
        {
            String fileName = mapPath.substring(mapPath.lastIndexOf("\\")+1);
            if (fileName.endsWith(".py") && fileName.startsWith("test_")) {
                this.isLeaf = true;
                runCmd = "python " + mapPath;
                checkPartten = "[SUCCESS]";
            }
        }
        if (null==parent)
        {
            addToLib(this);
        }

        queue.RegistCom(this);
    }

    //---------为支持序列化增加 start------------------------
    public int getRunTimeout() {
        return runTimeout;
    }

    public void setRunTimeout(int runTimeout) {
        this.runTimeout = runTimeout;
    }

    public String getRunCmd() {
        return runCmd;
    }

    public void setRunCmd(String runCmd) {
        this.runCmd = runCmd;
    }

    public String getCheckPartten() {
        return checkPartten;
    }

    public void setCheckPartten(String checkPartten) {
        this.checkPartten = checkPartten;
    }

    public TESTCASE_STATUS getStatus()
    {
        return status;
    }
    public void setStatus(TESTCASE_STATUS status)
    {
        this.status = status;
    }

    public String getMapPath() {
        return mapPath;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
        for(TestcaseNode node : subNodeList)
        {
            node.setSelect(isSelect);
        }
    }

    public void setSimpleSelect(boolean select) {
        isSelect = select;
    }

    public ArrayList<TestcaseResult> getResults() {
        return results;
    }

    public int getFailRetryTimes() {
        return failRetryTimes;
    }

    public void setFailRetryTimes(int failRetryTimes) {
        this.failRetryTimes = failRetryTimes;
    }

    public void setHasRunTime(int hasRunTime) {
        this.hasRunTime = hasRunTime;
    }
    //------------------------支持序列化  end--------------------------------------

    //------------------------适配tree增加函数 start--------------------------------
    @Override
    public TreeNode getChildAt(int childIndex) {
        if (childIndex>subNodeList.size())
            return null;
        return subNodeList.get(childIndex);
    }

    @Override
    public int getChildCount() {
        return subNodeList.size();
    }

    @Override
    public TreeNode getParent() {
        return parent;
    }

    @Override
    public int getIndex(TreeNode node) {
        return subNodeList.indexOf(node);
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    @Override
    public Enumeration<TestcaseNode> children() {
        return subNodeList.elements();
    }

    @Override
    public String toString()
    {
        String[] strList =  mapPath.split("\\\\");
        return strList[strList.length-1];
    }
    //------------------------适配tree增加函数  end--------------------------------
    public void addToLib(TestcaseNode node)
    {
        if(null==allNodeList)
        {
            allNodeList = new ArrayList<>();
        }
        allNodeList.add(node);
    }

    public void showSourceCode()
    {
        File file = new File(mapPath);
        if (file.isFile()==false)
        {
            return;
        }
        if (file.exists()==false)
        {
            Msg msg = new Msg("CmdClear", null, "LogViewer");
            queue.SendMessage(msg);

            msg = new Msg("CmdShowText", null, "LogViewer");
            msg.SetParam("showString", "文件不存在:" + mapPath);
            queue.SendMessage(msg);
            return;
        }
        boolean isClearBoard = false;
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
            while(true) {
                char[] tempchars = new char[1024];
                int readLen = reader.read(tempchars);
                if (readLen<0)
                {
                    break;
                }
                if (isClearBoard == false){
                    Msg msg = new Msg("CmdClear", null, "LogViewer");
                    queue.SendMessage(msg);
                    isClearBoard = true;
                }

                Msg msg = new Msg("CmdShowText", null, "LogViewer");
                msg.SetParam("showString", String.valueOf(tempchars));
                queue.SendMessage(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static TestcaseNode searchTestcase(String nodePath)
    {
        if (null==nodePath)
        {
            for(TestcaseNode node: allNodeList)
            {
                if(null==node.getParent())
                {
                    return node;
                }
            }
        }
        for(TestcaseNode node: allNodeList)
        {
            if(node.getMapPath().equals(nodePath))
            {
                return node;
            }
        }
        return null;
    }

    public void OutCallBack(String strOut)
    {
        if(runLogBuff==null)
        {
            runLogBuff = new StringBuffer();
        }
        runLogBuff.append(strOut);
        Msg msg = new Msg("CmdShowText", GetComId(), "LogViewer");
        msg.SetParam("showString", strOut);
        queue.SendMessage(msg);
    }

    public void EndCallBack()
    {
        hasRunTime++;
        String logStr = runLogBuff.toString();
        logStr = logStr.trim();
        if(logStr.endsWith(checkPartten))
        {
            status = TESTCASE_STATUS.TESTCASE_PASS;
        }
        else
        {

            status = TESTCASE_STATUS.TESTCASE_FAIL;
            if(hasRunTime<failRetryTimes)
            {
                TestcaseRunGroup group = TestcaseRunGroup.GetInstance();
                group.Add(this);
            }
        }
        UpdateUI();

        // 保存日志
        String logFile = getDefaultLogPath();
        try {
            File wf = new File(logFile);
            wf.createNewFile();
            FileWriter writer = new FileWriter(wf);
            writer.write(logStr);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void UpdateUI()
    {
        Msg msg = new Msg("CmdUpdateUI", GetComId(), "TestcaseViewer");
        queue.SendMessage(msg);
    }

    private String getDefaultLogPath()
    {
        int pos1 = mapPath.lastIndexOf(".");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(mapPath.substring(0, pos1));

        Date date=new Date();
        SimpleDateFormat df=new SimpleDateFormat("yyyyMMddhhmmss");
        String timeStr = df.format(date);

        stringBuffer.append("_");
        stringBuffer.append(timeStr);
        stringBuffer.append(".log");
        return stringBuffer.toString();
    }
    public void StartRun()
    {
        if (isLeaf && isSelect) {
            try {
                Method callback1 = this.getClass().getDeclaredMethod("OutCallBack", String.class);
                Method callback2 = this.getClass().getDeclaredMethod("EndCallBack");
                LocalWorker worker = new LocalWorker(runCmd, null, callback1, callback2, this);

                Msg msg2 = new Msg("CmdClear", GetComId(), "LogViewer");
                queue.SendMessage(msg2);
                status = TESTCASE_STATUS.TESTCASE_RUNNING;
                UpdateUI();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        else
        {
            TestcaseRunGroup group = TestcaseRunGroup.GetInstance();
            for(TestcaseNode node : subNodeList)
            {
                group.Add(node);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        TestcaseNode node = new TestcaseNode("d://gzy", null);
        System.out.println(TestcaseNode.allNodeList.size());
    }
}

