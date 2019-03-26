package com.autotest.ui;

import com.autotest.common.Msg;
import com.autotest.common.MsgCom;
import com.autotest.common.MsgQueue;
import com.autotest.ui.TestcaseNode.TESTCASE_STATUS;

import javax.swing.*;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TestcaseViewer extends JTree implements MsgCom {
    private MsgQueue queue = null;
    private TestcaseMenu popMenu = null;
    public TestcaseViewer()
    {
        super();
        CheckBoxCellRenderer renderer = new CheckBoxCellRenderer(this);
        setCellRenderer(renderer);
        setModel(new DirTreeMode());
        popMenu = new TestcaseMenu(this);
        setComponentPopupMenu(popMenu);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if ( getSelectionPath() ==null)
                {
                    return;
                }
                TestcaseNode node = (TestcaseNode) getSelectionPath().getLastPathComponent();
                if (node==null)
                {
                    return;
                }

                int hotspot = new JCheckBox().getPreferredSize().width;
                TreePath path = getPathForLocation(e.getX(), e.getY());
                if(path==null)
                    return;
                if(e.getX()>getPathBounds(path).x+hotspot)
                    return;
                if(e.getY()>getPathBounds(path).y+hotspot)
                    return;

                if(node.isSelect())
                {
                    node.setSelect(false);
                }
                else
                {
                    node.setSelect(true);
                }
                updateUI();
            }
        });

        addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TestcaseNode node = (TestcaseNode) getSelectionPath().getLastPathComponent();
                if(node.isLeaf())
                {
                    popMenu.setFileModel();

                    // 设置配置面板
                    Msg msg = new Msg("CmdUpdateShow", GetComId(), "StepViewer");
                    msg.SetParam("Testcase", node);
                    queue.SendMessage(msg);
                }
                else
                {
                    popMenu.setDirModel();
                }
            }
        });
        queue = MsgQueue.GetInstance();
        queue.RegistCom(this);
    }

    public void CmdUpdateModel(Msg msg)
    {
        setModel(new DirTreeMode());
        repaint();
    }

    public void CmdUpdateUI(Msg msg)
    {
        repaint();
    }

    @Override
    public String GetComId() {
        return "TestcaseViewer";
    }
}


class DirTreeMode implements TreeModel {
    private TestcaseNode root = null;
    public DirTreeMode()
    {
        if(null==TestcaseNode.allNodeList)
        {
            return;
        }
        this.root = TestcaseNode.searchTestcase(null);
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        TestcaseNode node = (TestcaseNode)parent;
        return node.getChildAt(index);
    }

    @Override
    public int getChildCount(Object parent) {
        TestcaseNode node = (TestcaseNode)parent;
        return node.getChildCount();
    }

    @Override
    public boolean isLeaf(Object node) {
        TestcaseNode tempNode = (TestcaseNode)node;
        return tempNode.isLeaf();

    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        TestcaseNode tempNode = (TestcaseNode)parent;
        return tempNode.getIndex((TreeNode)child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {

    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {

    }
}

class CheckBoxCellRenderer  extends TreeShowPane implements TreeCellRenderer
{
    public CheckBoxCellRenderer(JTree tree)
    {
        super(tree);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
    {
        if(selected)
        {
            setBackground(Color.LIGHT_GRAY);
        }
        else
        {
            setBackground(Color.WHITE);
        }

        if(value.getClass().getName().equals("com.autotest.ui.TestcaseNode"))
        {
            TestcaseNode node = (TestcaseNode)value;
            setSelect(node.isSelect());
        }

        setShowTitle(value.toString());
        if (leaf==false)
        {
            if (expanded==false)
            {
                setIcon(NODE_STATUS.NODE_DIR_CLOSE);
            }
            else
            {
                setIcon(NODE_STATUS.NODE_DIR_OPEN);
            }
        }
        else
        {
            TestcaseNode node = (TestcaseNode)value;

            if (node.getStatus()== TESTCASE_STATUS.TESTCASE_ERROR)
            {
                setIcon(NODE_STATUS.NODE_FILE_ERROR);
            }
            else if(node.getStatus()== TESTCASE_STATUS.TESTCASE_FAIL)
            {
                setIcon(NODE_STATUS.NODE_FILE_FAIL);
            }
            else if(node.getStatus()== TESTCASE_STATUS.TESTCASE_PASS)
            {
                setIcon(NODE_STATUS.NODE_FILE_PASS);
            }
            else if(node.getStatus()== TESTCASE_STATUS.TESTCASE_RUNNING)
            {
                setIcon(NODE_STATUS.NODE_FILE_RUNNING);
            }
            else
            {
                setIcon(NODE_STATUS.NODE_FILE_IDLE);
            }
        }

        return this;
    }
}

class TestcaseMenu extends JPopupMenu implements MsgCom
{
    JTree tree = null;
    JMenuItem run = new JMenuItem("执行");
    JMenuItem stop = new JMenuItem("停止");
    JMenuItem showSrc = new JMenuItem("显示源码");
    JMenuItem showLog = new JMenuItem("显示执行记录");
    MsgQueue queue = MsgQueue.GetInstance();
    public TestcaseMenu(JTree tree)
    {
        this.tree = tree;
        add(run);
        add(stop);
        add(showSrc);
        add(showLog);
        setIniModel();
        queue.RegistCom(this);

        showSrc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TestcaseNode node = (TestcaseNode) tree.getSelectionPath().getLastPathComponent();
                node.showSourceCode();
            }
        });

        run.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TestcaseNode node = (TestcaseNode) tree.getSelectionPath().getLastPathComponent();
                node.StartRun();
            }
        });
    }

    public void setFileModel()
    {
        run.setEnabled(true);
        stop.setEnabled(true);
        showSrc.setEnabled(true);
    }

    public void setDirModel()
    {
        run.setEnabled(true);
        stop.setEnabled(true);
        showSrc.setEnabled(false);
    }

    public void setIniModel()
    {
        run.setEnabled(false);
        stop.setEnabled(false);
        showSrc.setEnabled(false);
    }

    @Override
    public String GetComId() {
        return "TestcaseMenu";
    }
}