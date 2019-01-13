package com.testviewer.module;

import com.testviewer.common.Msg;
import com.testviewer.common.MsgCom;
import com.testviewer.common.MsgQueue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.testviewer.common.ShellClient;
import com.testviewer.common.Common;

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

    private void LoadTestcase()
    {
        LinkedList<String> outRspList = new LinkedList<String>();
        Common.RouteDir(this.localPath, ".py", null, outRspList);
        for(String str : outRspList)
        {
            Testcase testcase = new Testcase(str, null, localPath);
            this.testcases.add(testcase);
        }
        SetTreeView();
    }

    private void SetTreeView()
    {
        for(Testcase testcase : this.testcases)
        {
            Msg msg = new Msg("CmdAddNode", GetComId(), "com.testviewer.ui.TestcaseViewer");
            msg.SetParam("nodeXpath", testcase.GetTreeXpath());
            query.SendMessage(msg);
        }
    }

    static public Testsuit LoadFromXml(String xmlPath) throws Exception {
        ObjectMapper mapper = new XmlMapper();
        Testsuit suit = mapper.readValue(new File(xmlPath),
                new TypeReference<Testsuit>(){});
        suit.SetTreeView();
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
}
