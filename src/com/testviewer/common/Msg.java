package com.testviewer.common;

import java.util.HashMap;
import java.util.Map;

public class Msg {
    private String msgName = "";
    private String sourceId = null;
    private String destId = null;
    private Map<String, Object> params = null;

    public Msg(String msgName, String sourceId, String destId)
    {
        this.msgName = msgName;
        this.sourceId = sourceId;
        this.destId = destId;
    }

    public String GetDestComId()
    {
        return destId;
    }

    public String GetMsgName()
    {
        return msgName;
    }

    public void SetParam(String key, Object value)
    {
        if (null==params)
        {
            params = new HashMap<String, Object>();
        }
        params.put(key, value);
    }

    public Object GetParam(String key)
    {
        return params.get(key);
    }
}
