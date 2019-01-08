package com.testviewer.ui;
import com.testviewer.common.Common;
import com.testviewer.common.MsgCom;
import com.testviewer.common.Msg;
import com.testviewer.common.MsgQueue;
import com.testviewer.common.SwingConsole;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class MainFrame extends JFrame implements MsgCom {
    private JSplitPane splitPane1 = null;
    private JSplitPane splitPane2 = null;
    private MsgQueue queue = MsgQueue.GetInstance();

    public MainFrame()
    {
        super("TestViewer");
        AddMainMenu();
        AddViewer();
        queue.RegistCom(this);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                SetSplite();
            }
        });
    }

    private void AddMainMenu()
    {
        MainMenu menu = new MainMenu();
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);
        setJMenuBar(menuBar);
    }

    private void AddViewer()
    {
        splitPane1 = new JSplitPane();

        JScrollPane scrollPane = new JScrollPane(new TestcaseViewer());

        splitPane1.setLeftComponent(scrollPane);
        splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane2.setTopComponent(new RunLogViewer());
        splitPane2.setBottomComponent(new RunStepViewer());
        splitPane1.setRightComponent(splitPane2);
        add(splitPane1);
    }

    public void CmdSetSplite(Msg msg)
    {
        splitPane1.setDividerLocation(0.2);
        splitPane2.setDividerLocation(0.8);
    }

    public void SetSplite()
    {
        Msg msg = new Msg("CmdSetSplite", this.GetComId(), this.GetComId());
        queue.SendMessage(msg);
    }

    @Override
    public String GetComId()
    {
        return this.getClass().getName();
    }


    public static void main(String[] args) {
        MainFrame frame = new MainFrame();
        SwingConsole.run(frame, 0, 0);
        Common.SleepEx(300);
        frame.SetSplite();

        MsgQueue query = MsgQueue.GetInstance();
        Msg msg = new Msg("CmdAddNode", null, "TestcaseViewer");
        msg.SetParam("nodeXpath", "document/ediary/gzy");
        query.SendMessage(msg);
    }
}
