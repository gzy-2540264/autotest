package com.testviewer.module;

import com.testviewer.common.Common;
import com.testviewer.common.Msg;
import com.testviewer.common.MsgCom;
import com.testviewer.common.MsgQueue;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.*;

public class Testcase implements MsgCom {
    private String projectPath = null;         //工程所在路径
    private String testScriptPath = null;  //脚本路径
    private String testRunLogPath = null;      //脚本执行日志存放目录
    private String runCmd = null;          //脚本运行命令
    private int runTimes = 0;               //脚本运行次数
    private boolean isFailStop = false;     //失败后是否继续执行
    private int runTimeout = 60 * 10;       //脚本执行超时时间
    private String passCheckPattern = null; //脚本执行成功日志样式
    private TESTCASE_STATUS curStatus = TESTCASE_STATUS.IDLE;  //脚本当前状态

    private MsgQueue queue = MsgQueue.GetInstance();

    @Override
    public String GetComId(){
        return GetTreeXpath();
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

    public void run()
    {

    }

    public void stop()
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

    public void CmdSetStepViewer(Msg msg)
    {
        Msg msgSend = new Msg("CmdSetItemSetting", null, "com.testviewer.ui.RunStepViewer");
        msgSend.SetParam("testcase", this);
        queue.SendMessage(msgSend);
    }

    public String GetTreeXpath()
    {
        String xpath = testScriptPath.substring(projectPath.length() + 1, testScriptPath.length() - 3);
        String rspString =  xpath.replace("\\", "/");
        return rspString;

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
                System.out.println(String.valueOf(tempchars).length());
                msg.SetParam("showString", String.valueOf(tempchars));
                queue.SendMessage(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
