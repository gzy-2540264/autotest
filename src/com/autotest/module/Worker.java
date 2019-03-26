package com.autotest.module;

import com.autotest.common.ThreadPool;

public class Worker{
    protected ThreadPool pool = null;
    protected boolean isWorkEnd = false;
    public Worker()
    {
        pool = ThreadPool.GetInstance();
    }

    public boolean isWorkEnd() {
        return isWorkEnd;
    }
}

