package com.autotest.module;

import com.autotest.common.ThreadPool;
import com.autotest.ui.TestcaseNode;
import sun.security.jca.GetInstance;

import java.util.ArrayList;

public class TestcaseRunGroup {
    private ArrayList<TestcaseNode> needRunList = new ArrayList<>();
    private TestcaseNode runningNode = null;
    private ThreadPool pool = ThreadPool.GetInstance();
    private Object hand = null;
    private static TestcaseRunGroup groupInstance = null;
    private TestcaseRunGroup(){}

    public static TestcaseRunGroup GetInstance()
    {
        if (groupInstance==null)
        {
            groupInstance = new TestcaseRunGroup();
        }
        return groupInstance;
    }

    public void StartRun()
    {
        try {
            hand = pool.registTick(getClass().getDeclaredMethod("TickChooseTestNode"), this);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void Add(TestcaseNode node)
    {
        needRunList.add(node);
    }

    public void TickChooseTestNode()
    {
        if (needRunList.size()<=0)
        {
            return;
        }
        if (runningNode==null && needRunList.size()>0)
        {
            runningNode = needRunList.get(0);
            needRunList.remove(0);
            runningNode.StartRun();
        }

        if (runningNode.getStatus()!=TestcaseNode.TESTCASE_STATUS.TESTCASE_RUNNING)
        {
            runningNode = null;
        }
    }
}
