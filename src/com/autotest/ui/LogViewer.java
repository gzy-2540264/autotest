package com.autotest.ui;

import com.autotest.common.Msg;
import com.autotest.common.MsgCom;
import com.autotest.common.MsgQueue;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import java.awt.*;

public class LogViewer extends JTextPane implements MsgCom {
    private MsgQueue queue = MsgQueue.GetInstance();
    public LogViewer()
    {
        setFont(new Font("宋体", Font.BOLD,20));
        queue.RegistCom(this);
    }

    @Override
    public String GetComId() {
        return "LogViewer";
    }

    public void CmdClear(Msg msg)
    {
        setText("");
    }

    public void CmdShowText(Msg msg)
    {
        Document document = getDocument();
        SimpleAttributeSet attrset = new SimpleAttributeSet();
        String str = (String)msg.GetParam("showString");

        try {
            String decodeStr = new String(str.getBytes("gbk"));
            document.insertString(document.getLength(), decodeStr, attrset);
            setCaretPosition(document.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
