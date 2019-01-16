package com.testviewer.ui;

import com.testviewer.common.Msg;
import com.testviewer.common.MsgCom;
import com.testviewer.common.MsgQueue;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;

public class RunLogViewer extends JTextPane implements MsgCom {
    MsgQueue queue = MsgQueue.GetInstance();
    public RunLogViewer()
    {
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
            document.insertString(document.getLength(), str, attrset);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}
