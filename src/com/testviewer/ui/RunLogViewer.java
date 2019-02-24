package com.testviewer.ui;

import com.testviewer.common.Msg;
import com.testviewer.common.MsgCom;
import com.testviewer.common.MsgQueue;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import java.awt.*;

public class RunLogViewer extends JTextPane implements MsgCom {
    MsgQueue queue = MsgQueue.GetInstance();
    public RunLogViewer()
    {
        setFont(new Font("宋体",Font.BOLD,20));
        queue.RegistCom(this);
    }

    @Override
    public String GetComId() {
        return getClass().getName();
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

    public void CmdReset(Msg msg)
    {
        CmdClear(msg);
    }
}
