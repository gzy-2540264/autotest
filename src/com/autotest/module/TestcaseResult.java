package com.autotest.module;

import com.autotest.ui.TestcaseNode;

public class TestcaseResult {
    private String testcasePath = null;
    private String logPath = null;
    private TestcaseNode.TESTCASE_STATUS status = TestcaseNode.TESTCASE_STATUS.TESTCASE_IDLE;
    public TestcaseResult(String testcasePath, String logPath, TestcaseNode.TESTCASE_STATUS status)
    {
        this.testcasePath = testcasePath;
        this.logPath = logPath;
        this.status = status;
    }
    public TestcaseResult(){}

    public String getTestcasePath() {
        return testcasePath;
    }

    public void setTestcasePath(String testcasePath) {
        this.testcasePath = testcasePath;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public TestcaseNode.TESTCASE_STATUS getStatus() {
        return status;
    }

    public void setStatus(TestcaseNode.TESTCASE_STATUS status) {
        this.status = status;
    }
}
