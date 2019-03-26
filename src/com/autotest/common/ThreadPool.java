package com.autotest.common;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//单例模式
public class ThreadPool implements Runnable{

    private static ThreadPool instance = null;
    static final long TICK_TIME = 10;
    private ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(100);
    private ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(2, 5,
            600, TimeUnit.SECONDS, workQueue);
    private boolean isEndAll = false;

    private ArrayList<TickFuncInfo> tickFuncList = new ArrayList<TickFuncInfo>();
    private ArrayList<TickFuncInfo> newTickFuncList = new ArrayList<TickFuncInfo>();

    static public ThreadPool GetInstance()
    {
        if(instance==null)
        {
            instance = new ThreadPool();
        }
        return instance;
    }

    private ThreadPool()
    {
        poolExecutor.execute(this);
    }

    /**
     * 返回tick函数的注册句柄，在反注册的时候使用
     * @param method
     * @param obj
     * @return
     */
    public Object registTick(Method method, Object obj)
    {
        TickFuncInfo hand = new TickFuncInfo(method, obj);
        newTickFuncList.add(hand);
        return hand;
    }

    public void unRegistTick(Object hand)
    {
        if (null==hand)
            return;

        TickFuncInfo temp = (TickFuncInfo)hand;
        temp.isEndTick = true;
    }

    public void registTask(Runnable runnable) throws InterruptedException {
        poolExecutor.execute(runnable);
    }

    public void EndPool()
    {
        isEndAll = true;
    }

    @Override
    public void run() {
        while (true)
        {
            long timeStart = System.currentTimeMillis();
            ArrayList<TickFuncInfo> needRemoveFuncs = new ArrayList<TickFuncInfo>();
            for (TickFuncInfo funcInfo : tickFuncList) {
                try {
                    funcInfo.method.invoke(funcInfo.obj, null);
                    if(funcInfo.isEndTick)
                    {
                        needRemoveFuncs.add(funcInfo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            for(TickFuncInfo funcInfo: needRemoveFuncs)
            {
                tickFuncList.remove(funcInfo);
            }

            for(TickFuncInfo funcInfo: newTickFuncList)
            {
                tickFuncList.add(funcInfo);
            }
            newTickFuncList.clear();

            long timeEnd = System.currentTimeMillis();
            long timeCost = timeEnd - timeStart;

            if (timeCost < TICK_TIME)
            {
                try {
                    Thread.sleep(TICK_TIME - timeCost);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(isEndAll)
            {
                break;
            }
        }
    }//end run

    public class TickFuncInfo
    {
        public Method method;
        public Object obj;
        public boolean isEndTick = false;
        public TickFuncInfo(Method method, Object obj)
        {
            this.method = method;
            this.obj = obj;
        }
    }
}

