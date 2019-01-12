package com.testviewer.common;

import java.lang.reflect.Method;
import java.util.*;

public class MsgQueue implements Runnable{
    static final int TICK_TIME = 100;
    private List<MsgCom> ComList = new ArrayList<MsgCom>();
    private Queue<Msg> msgQueue = new LinkedList<Msg>();
    private static MsgQueue instance = null;

    private MsgQueue()
    {
        Thread thead = new Thread(this);
        thead.start();
    }

    static public MsgQueue GetInstance()
    {
        if (instance==null)
            instance = new MsgQueue();
        return instance;
    }

    private boolean IsComRegist(MsgCom msgCom)
    {
        for (MsgCom msgCom1 : ComList)
        {
            if (msgCom == msgCom1)
                return true;
        }
        return false;
    }

    public void RegistCom(MsgCom msgCom)
    {
        if (IsComRegist(msgCom))
            return;

        ComList.add(msgCom);
    }

    public void UnRegistCom(MsgCom msgCom)
    {
        for(MsgCom msgCom1 : ComList)
        {
            if (msgCom1 == msgCom)
            {
                ComList.remove(msgCom);
            }
        }
    }

    public void SendMessage(Msg msg)
    {
        msgQueue.add(msg);
    }

    public int GetDeep()
    {
        return msgQueue.size();
    }

    public void run()
    {
        while(true)
        {
            //如果队列为空，只做延时操作
            if (msgQueue.isEmpty())
            {
                Common.SleepEx(TICK_TIME);
            }

            while(msgQueue.isEmpty()==false)
            {
                Msg msg = msgQueue.poll();

                //从已经注册的组件队列中查找合适的组件进行消息处理
                for (MsgCom com : ComList)
                {
                    //说明已经找到
                    if (com.GetComId().equals(msg.GetDestComId()))
                    {
                        String funcName = msg.GetMsgName();
                        Method[] methods = com.getClass().getDeclaredMethods();
                        for(int i=0; i<methods.length; i++)
                        {
                            if(methods[i].getName().equals(funcName))
                            {
                                try
                                {
                                    methods[i].invoke(com, msg);
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }// for(int i=0; i<methods.length; i++)
                    }// if (com.GetComId()==msg.GetDestComId())
                }// for (MsgCom com : ComList)
            }// while(msgQueue.isEmpty()==false)
        }
    }
}
