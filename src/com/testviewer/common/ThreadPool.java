package com.testviewer.common;

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

    private ArrayList<TickFuncInfo> tickFuncList = new ArrayList<TickFuncInfo>();

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

    public TickFuncInfo registTick(Method method, Object obj)
    {
        TickFuncInfo hand = new TickFuncInfo(method, obj);
        tickFuncList.add(hand);
        return hand;
    }

    public void unRegistTick(TickFuncInfo hand)
    {
        hand.isEndTick = true;
    }

    public void registTask(Runnable runnable) throws InterruptedException {
        poolExecutor.execute(runnable);
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

