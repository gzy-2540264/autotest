package com.testviewer.module;

import com.testviewer.common.*;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.*;
import java.util.Arrays;

public class Testcase implements MsgCom, Runnable {
    private String projectPath = null;         //工程所在路径
    private String testScriptPath = null;  //脚本路径
    private String testRunLogPath = null;      //脚本执行日志存放目录
    private String runCmd = null;          //脚本运行命令
    private int runTimes = 0;               //脚本运行次数
    private boolean isTestcaseRun = false;  //脚本是否执行
    private boolean isFailStop = false;     //失败后是否继续执行
    private int runTimeout = 60 * 10;       //脚本执行超时时间
    private String passCheckPattern = ""; //脚本执行成功日志样式
    private TESTCASE_STATUS curStatus = TESTCASE_STATUS.IDLE;  //脚本当前状态

    //以下成员变量只同任务执行相关，不是javabean成员数据
    ThreadPool threadPool = ThreadPool.GetInstance();
    InputStream in = null;
    Object tickHand = null;
    boolean isTaskRunning = false;
    StringBuffer logBuff = null;


    private MsgQueue queue = MsgQueue.GetInstance();

    @Override
    public String GetComId(){
        return GetTreeXpath();
    }

    @Override
    public void run() {
        Runtime rt = Runtime.getRuntime();
        try {
            logBuff = new StringBuffer();
            Process p = rt.exec(runCmd);
            in = p.getInputStream();
            isTaskRunning = true;
            p.waitFor();
            isTaskRunning = false;
        } catch (Exception e) {
            isTaskRunning = false;
            e.printStackTrace();
        }
    }

    public enum TESTCASE_STATUS{
        IDLE, PASSED, FAILED, ERROR, RUNNING, BREAK;
    }

    public Testcase()
    {
        queue.RegistCom(this);
    }

    public Testcase(String testScriptPath, String testRunLogPath, String projectPath)
    {
        this.projectPath = projectPath;
        this.testScriptPath = testScriptPath;
        if (null == testRunLogPath)
        {
            this.testRunLogPath = getDeaultLogPath();
        }
        else
        {
            this.testRunLogPath = testRunLogPath;
        }

        this.runCmd = "python " + testScriptPath;
        queue.RegistCom(this);
    }

    private String getDeaultLogPath()
    {
        File f = new File(testScriptPath);
        String path = f.getAbsolutePath();
        return path + "/log/";
    }


    public void CmdRun(Msg msg)
    {
        setCurStatus(TESTCASE_STATUS.RUNNING);

        //设置两个显示的状态
        Msg submsg = new Msg("CmdModeToViewSys", null, "com.testviewer.ui.TestcaseViewer");
        submsg.SetParam("testcase", this);
        queue.SendMessage(submsg);

        submsg = new Msg("CmdSetItemSetting", null, "com.testviewer.ui.RunStepViewer");
        submsg.SetParam("testcase", this);
        queue.SendMessage(submsg);


        try {
            threadPool.registTask(this);
            Thread.sleep(100);
            tickHand = threadPool.registTick(getClass().getMethod("MonitorTick"),this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 在用例执行完成后。通过日志分析用例成功还是失败
     */
    private void CheckTestcaseResult(String logStr)
    {
        boolean isResultPass =  false;
        if(passCheckPattern != null && passCheckPattern.length()>0)
        {
            logStr = logStr.trim();
            if(logStr.endsWith(passCheckPattern))
            {
                isResultPass = true;
            }
        }
        if (isResultPass==false)
        {
            setCurStatus(TESTCASE_STATUS.FAILED);
        }else
        {
            setCurStatus(TESTCASE_STATUS.PASSED);
        }

        Msg msg = new Msg("CmdModeToViewSys", null, "com.testviewer.ui.TestcaseViewer");
        msg.SetParam("testcase", this);
        queue.SendMessage(msg);

        msg = new Msg("CmdSetItemSetting", null, "com.testviewer.ui.RunStepViewer");
        msg.SetParam("testcase", this);
        queue.SendMessage(msg);

        threadPool.unRegistTick(tickHand);
        tickHand = null;
    }

    public void MonitorTick()
    {
        if (isTaskRunning==false && null != tickHand)
        {
            threadPool.unRegistTick(tickHand);
            tickHand = null;
            CheckTestcaseResult(logBuff.toString());
        }

        if (in==null)
        {
            return;
        }
        byte[] b = new byte[10240];
        try {
            Arrays.fill(b, (byte)0);
            in.read(b);
            String temp = new String(b, "GBK");
            temp = temp.trim();
            if (temp.length()>0) {
                String sendStr = String.valueOf(temp) + "\n";
                logBuff.append(sendStr);
                Msg msg = new Msg("CmdShowText", null, "com.testviewer.ui.RunLogViewer");
                msg.SetParam("showString", sendStr);
                queue.SendMessage(msg);
            }
        }catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }

    }

    public void CmdStop(Msg msg)
    {

    }

    public String getTestScriptPath() {
        return testScriptPath;
    }

    public void setTestScriptPath(String testScriptPath) {
        this.testScriptPath = testScriptPath;
    }

    public String getTestRunLogPath() {
        return testRunLogPath;
    }

    public void setTestRunLog(String testRunLog) {
        this.testRunLogPath = testRunLog;
    }

    public String getRunCmd() {
        return runCmd;
    }

    public void setRunCmd(String runCmd) {
        this.runCmd = runCmd;
    }

    public int getRunTimes() {
        return runTimes;
    }

    public void setRunTimes(int runTimes) {
        this.runTimes = runTimes;
    }

    public boolean isTestcaseRun() {
        return isTestcaseRun;
    }

    public void setTestcaseRun(boolean isTestcaseRun) {
        this.isTestcaseRun = isTestcaseRun;
    }

    public boolean isFailStop() {
        return isFailStop;
    }

    public void setFailStop(boolean failStop) {
        isFailStop = failStop;
    }

    public int getRunTimeout() {
        return runTimeout;
    }

    public void setRunTimeout(int runTimeout) {
        this.runTimeout = runTimeout;
    }

    public String getPassCheckPattern() {
        return passCheckPattern;
    }

    public void setPassCheckPattern(String passCheckPattern) {
        this.passCheckPattern = passCheckPattern;
    }

    public TESTCASE_STATUS getCurStatus() {
        return curStatus;
    }

    public void setCurStatus(TESTCASE_STATUS curStatus) {
        this.curStatus = curStatus;
    }

    public String GetTreeXpath()
    {
        String xpath = testScriptPath.substring(projectPath.length() + 1, testScriptPath.length() - 3);
        String rspString =  xpath.replace("\\", "/");
        return rspString;

    }

    public String GetTreeNodeName()
    {
        String nodeXpath = GetTreeXpath();
        String[] nodeList = nodeXpath.split("/");
        String curNodeStr = nodeList[nodeList.length - 1];
        return curNodeStr;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public void CmdShowFileCode(Msg xxx)
    {
        File file = new File(testScriptPath);
        if (file.exists()==false)
        {
            System.out.println("文件不存在:" + testScriptPath);
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
                    Msg msg = new Msg("CmdClear", null, "com.testviewer.ui.RunLogViewer");
                    queue.SendMessage(msg);
                    isClearBoard = true;
                }

                Msg msg = new Msg("CmdShowText", null, "com.testviewer.ui.RunLogViewer");
                msg.SetParam("showString", String.valueOf(tempchars));
                queue.SendMessage(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void CmdSetTestcaseRun(Msg msg)
    {
        Boolean getflag = (Boolean)msg.GetParam("isTestcaseRun");
        setTestcaseRun(getflag);
    }

    public void CmdSetStepViewer(Msg msg)
    {
        Msg msgSend = new Msg("CmdSetItemSetting", null, "com.testviewer.ui.RunStepViewer");
        msgSend.SetParam("testcase", this);
        queue.SendMessage(msgSend);
    }
}
