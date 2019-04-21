package com.autotest.module;

import com.autotest.ui.TestcaseNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 用作对象保存，读取用
 */
public class TestcaseSave {
    private ArrayList<TestcaseInfo> testcaseInfoList = new ArrayList<TestcaseInfo>();
    public TestcaseSave()
    {
        if (null==TestcaseNode.allNodeList)
            return;

        for(TestcaseNode node : TestcaseNode.allNodeList)
        {
            String testcasePath = node.getMapPath();
            boolean isSelect = node.isSelect();
            TestcaseNode.TESTCASE_STATUS status = node.getStatus();
            boolean isRoot = false;
            String runCmd = node.getRunCmd();
            String checkPattern = node.getCheckPartten();
            int runTimeout = node.getRunTimeout();
            if (null==node.getParent()) {
                isRoot = true;
            }
            TestcaseInfo info = new TestcaseInfo(testcasePath, isSelect, isRoot, status, runCmd, checkPattern, runTimeout);
            testcaseInfoList.add(info);
        }
    }

    public ArrayList<TestcaseInfo> getTestcaseInfoList() {
        return testcaseInfoList;
    }

    public void setTestcaseInfoList(ArrayList<TestcaseInfo> testcaseInfoList) {
        this.testcaseInfoList = testcaseInfoList;
    }

    public void saveToXml(String xmlPath) throws Exception {
        ObjectMapper mapper = new XmlMapper();
        ((XmlMapper) mapper).enable(SerializationFeature.INDENT_OUTPUT);
        String str = mapper.writeValueAsString(this);
        mapper.writeValue(new File(xmlPath), this);
    }

    public static TestcaseNode loadFromXml(String xmlPath) throws Exception {
        ObjectMapper mapper = new XmlMapper();
        TestcaseSave saveObj = mapper.readValue(new File(xmlPath),
                new TypeReference<TestcaseSave>(){});

        String rootPath = null;
        for(TestcaseInfo info : saveObj.getTestcaseInfoList())
        {
            if (info.isRoot())
                rootPath = info.getTestcasePath();
        }
        TestcaseNode node = new TestcaseNode(rootPath, null);

        for(TestcaseInfo info : saveObj.getTestcaseInfoList())
        {
            for (TestcaseNode subNode : TestcaseNode.allNodeList)
            {
                if (subNode.getMapPath().equals(info.getTestcasePath()))
                {
                    subNode.setSimpleSelect(info.isSelect());
                    subNode.setStatus(info.getStatus());
                    subNode.setRunCmd(info.getRunCmd());
                    subNode.setCheckPartten(info.getCheckPartten());
                    subNode.setRunTimeout(info.getRunTimeout());
                    subNode.setFailRetryTimes(info.getFailRunTime());
                    break;
                }
            }
        }
        return node;

    }

    public static void main(String[] args) throws Exception {
//        TestcaseNode node = new TestcaseNode("d:\\gzy", null);
//        TestcaseSave saveObj = new TestcaseSave();
//        saveObj.saveToXml("d:\\gzy.xml");
        TestcaseSave.loadFromXml("d:\\gzy.xml");
    }
}

class TestcaseInfo
{
    private String testcasePath = null;
    private boolean isSelect = false;
    private boolean isRoot = false;
    private String runCmd = null;
    private String checkPartten = null;
    private int runTimeout = 600;
    private TestcaseNode.TESTCASE_STATUS status = TestcaseNode.TESTCASE_STATUS.TESTCASE_IDLE;
    private int failRunTime = 3;  //在失败以后，重试的次数
    public TestcaseInfo(String testcasePath, boolean isSelect, boolean isRoot, TestcaseNode.TESTCASE_STATUS status,
                        String runCmd, String checkPartten, int runTimeout)
    {
        this.testcasePath = testcasePath;
        this.isSelect = isSelect;
        this.status = status;
        this.isRoot = isRoot;
        this.runCmd = runCmd;
        this.checkPartten = checkPartten;
        this.runTimeout = runTimeout;
    }

    public TestcaseInfo(String testcasePath, boolean isSelect, boolean isRoot, TestcaseNode.TESTCASE_STATUS status,
                        String runCmd, String checkPartten, int runTimeout, int failRunTime)
    {
        this.testcasePath = testcasePath;
        this.isSelect = isSelect;
        this.status = status;
        this.isRoot = isRoot;
        this.runCmd = runCmd;
        this.checkPartten = checkPartten;
        this.runTimeout = runTimeout;
        this.failRunTime = failRunTime;
    }

    public TestcaseInfo(){}

    public String getTestcasePath() {
        return testcasePath;
    }

    public void setTestcasePath(String testcasePath) {
        this.testcasePath = testcasePath;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public TestcaseNode.TESTCASE_STATUS getStatus() {
        return status;
    }

    public void setStatus(TestcaseNode.TESTCASE_STATUS status) {
        this.status = status;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public void setRoot(boolean root) {
        isRoot = root;
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

    public int getRunTimeout() {
        return runTimeout;
    }

    public void setRunTimeout(int runTimeout) {
        this.runTimeout = runTimeout;
    }

    public int getFailRunTime() {
        return failRunTime;
    }

    public void setFailRunTime(int failRunTime) {
        this.failRunTime = failRunTime;
    }
}