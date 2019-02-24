package com.testviewer.module;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.testviewer.common.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Testsuit implements MsgCom{
    private static Testsuit instance = null;
    private String remotePath = null;
    private String localPath = null;
    private String xmlPath = null;
    private String logPaht = null;
    private List<Testcase> testcases = new ArrayList<Testcase>();

    MsgQueue query = MsgQueue.GetInstance();

    //任务执行相关
    private ThreadPool threadPool = ThreadPool.GetInstance();
    private List<Testcase> waitRuntestcases = new ArrayList<Testcase>();
    private Object tickHand = null;

    public void PrintInfo() {
        for(Testcase testcase : testcases)
        {
            System.out.println(testcase.getTestScriptPath());
        }
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getXmlPath() {
        return xmlPath;
    }

    public void setXmlPath(String xmlPath) {
        this.xmlPath = xmlPath;
    }

    public String getLogPaht() {
        return logPaht;
    }

    public void setLogPaht(String logPaht) {
        this.logPaht = logPaht;
    }

    public List<Testcase> getTestcases() {
        return testcases;
    }

    public void setTestcases(List<Testcase> testcases) {
        this.testcases = testcases;
    }

    //因为对象化IO需要保留此构造函数，一般不调用该函数
    public Testsuit(){
        query.RegistCom(this);
    }

    public Testsuit(String xmlPath)
    {
        this.xmlPath = xmlPath;
        try {
            LoadFromXml(xmlPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        query.RegistCom(this);
    }

    public Testsuit(String remotePath, String localPath)
    {
        this.localPath = localPath;
        this.remotePath = remotePath;
        LoadTestcase();
        query.RegistCom(this);
    }

    private String getFileNameByPath(String fullPath)
    {
        int pos = fullPath.lastIndexOf("\\");
        if (pos>0)
            return fullPath.substring(pos+1, fullPath.length());
        return fullPath;
    }

    private void LoadTestcase()
    {
        LinkedList<String> outRspList = new LinkedList<String>();
        Common.RouteDir(this.localPath, ".py", null, outRspList);
        for(String str : outRspList)
        {
            String fileName = getFileNameByPath(str);
            if (fileName.startsWith("test_") == false)
                continue;
            Testcase testcase = new Testcase(str, null, localPath);
            this.testcases.add(testcase);
        }
        SetTreeView();
    }

    private void SetTreeView()
    {
        int nodeNum = this.testcases.size();
        int hasAddNum = 0;
        for(Testcase testcase : this.testcases)
        {
            Msg msg = new Msg("CmdAddNode", GetComId(), "com.testviewer.ui.TestcaseViewer");
            msg.SetParam("nodeXpath", testcase.GetTreeXpath());
            if (hasAddNum >= nodeNum - 1)
                msg.SetParam("isLastNode", true);
            else
                msg.SetParam("isLastNode", false);
            hasAddNum = hasAddNum + 1;
            query.SendMessage(msg);
        }
    }

    static public Testsuit LoadFromXml(String xmlPath) throws Exception {
        ObjectMapper mapper = new XmlMapper();
        Testsuit suit = mapper.readValue(new File(xmlPath),
                new TypeReference<Testsuit>(){});
        suit.SetTreeView();
        suit.SystoTestcaseViewer();
        return suit;
    }

    static public void SaveToXml(Testsuit suit, String xmlPath) throws Exception {
        ObjectMapper mapper = new XmlMapper();
        ((XmlMapper) mapper).enable(SerializationFeature.INDENT_OUTPUT);
        String str = mapper.writeValueAsString(suit);
        mapper.writeValue(new File(xmlPath), suit);
    }

    public void CmdSaveToXml(Msg msg)
    {
        String xmlPath = (String)msg.GetParam("xmlPath");
        System.out.println("start save to xml file:" + xmlPath);
        try {
            SaveToXml(this, xmlPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void CmdRunTestcases(Msg msg)
    {
        String xpath = (String)msg.GetParam("xpath");
        for(Testcase testcase : testcases)
        {
            String testcaseXpath = testcase.GetTreeXpath();
            if (testcaseXpath.startsWith(xpath) && testcase.isTestcaseRun())
            {
                testcase.setCurStatus(Testcase.TESTCASE_STATUS.IDLE);
                waitRuntestcases.add(testcase);
            }
        }


        if (waitRuntestcases.size()>0)
        {
            Msg submsg = new Msg("CmdClear", null, "com.testviewer.ui.RunLogViewer");
            query.SendMessage(submsg);

            SystoTestcaseViewer();
            try {
                tickHand = threadPool.registTick(getClass().getMethod("TickRuncaseOneByOne"),this);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    public void TickRuncaseOneByOne()
    {
        if (waitRuntestcases.size() == 0)
        {
            threadPool.unRegistTick(tickHand);
        }
        //如果有用例处于执行状态，则返回
        for(Testcase testcase : waitRuntestcases)
        {
            if (testcase.getCurStatus()==Testcase.TESTCASE_STATUS.RUNNING)
            {
                return;
            }
        }

        List<Testcase> completeTestcase = new ArrayList<Testcase>();
        for(Testcase testcase : waitRuntestcases)
        {
            if (testcase.getCurStatus()==Testcase.TESTCASE_STATUS.IDLE)
            {
                testcase.CmdRun(null);
                return;
            }
            //说明用例已经执行完成
            if (testcase.getCurStatus()!=Testcase.TESTCASE_STATUS.RUNNING
                    && testcase.getCurStatus()!=Testcase.TESTCASE_STATUS.IDLE )
            {
                completeTestcase.add(testcase);
            }
        }

        for(Testcase testcase : completeTestcase)
        {
            waitRuntestcases.remove(testcase);
        }
    }

    public void CmdReset(Msg msg)
    {
        remotePath = null;
        localPath = null;
        xmlPath = null;
        logPaht = null;
        testcases = new ArrayList<Testcase>();
    }

    static public void main(String[] args)
    {
//        Testsuit suit = new Testsuit(null, "D:\\gzy");
//        try {
//            Testsuit.SaveToXml(suit, "d:\\gzy.xml");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        try {
            Testsuit suit = Testsuit.LoadFromXml("d:\\gzy.xml");
            suit.PrintInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String GetComId() {
        return getClass().getName();
    }

    /**
     * 用例选择数据同步到viewer
     */
    public void SystoTestcaseViewer()
    {
        for(Testcase testcase : testcases)
        {
            Boolean isTestcaseRun = testcase.isTestcaseRun();
            Msg msg = new Msg("CmdModeToViewSys", null, "com.testviewer.ui.TestcaseViewer");
            msg.SetParam("testcase", testcase);
            query.SendMessage(msg);
        }
        return;
    }
}
