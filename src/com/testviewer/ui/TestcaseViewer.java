package com.testviewer.ui;

import com.testviewer.common.Msg;
import com.testviewer.common.MsgCom;
import com.testviewer.common.MsgQueue;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TestcaseViewer extends JTree implements MsgCom
{
    private TestcasePopMenu popMenu = null;
    static private TreeNode root = new CheckBoxNode("测试用例");
    MsgQueue query = MsgQueue.GetInstance();
    public TestcaseViewer()
    {
        super(root);
        setCellRenderer(new CheckBoxCellRenderer());
        AddPopMenu();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                CheckBoxNode node = null;
                try {
                    node = (CheckBoxNode) getSelectionPath().getLastPathComponent();
                }
                catch (Exception es)
                {
                    return;
                }

                if(node.isLeaf())
                {
                    popMenu.SetLeafMod();
                }
                else
                {
                    popMenu.SetDirMod();
                }

                if (e.getX() > (30 + GetCheckBoxStartX()))
                {
                    return;
                }

                if (node.GetSelect())
                {
                    node.SetSelect(false);
                }
                else
                {
                    node.SetSelect(true);
                }
                repaint();
            }
        });

        query.RegistCom(this);
    }

    /**
     * 增加节点， 节点间与/为分界
     * @param nodeXpath 未包括根的xpath路径
     * @param curRoot   当前根结点
     */
    private void AddTreeNode(CheckBoxNode curRoot, String nodeXpath)
    {
        String[] nodeList = nodeXpath.split("/");
        String curNodeStr = nodeList[0];
        String nextPath = "";
        for (int i=0; i<nodeList.length; i++)
        {
            if (i==0)
            {
                curNodeStr = nodeList[i];
            }
            else
            {
                if (nextPath.length()==0)
                {
                    nextPath = nodeList[i];
                }
                else {
                    nextPath = nextPath + "/" + nodeList[i];
                }
            }
        }

        int childrenCount = curRoot.getChildCount();
        CheckBoxNode findNode = null;
        for (int i=0; i<childrenCount; i++)
        {
            CheckBoxNode node = (CheckBoxNode)curRoot.getChildAt(i);
            if(node.toString().equals(curNodeStr))
            {
                findNode = node;
                AddTreeNode(node, nextPath);
            }
        }

        // 说明没有找到对应的结点，需要增加
        if (findNode==null)
        {
            CheckBoxNode node = new CheckBoxNode(curNodeStr);
            curRoot.add(node);
            AddTreeNode(node, nodeXpath);
        }
    }

    public void CmdAddNode(String nodeXpath)
    {
        System.out.println("11111111111111111111");
        AddTreeNode((CheckBoxNode)root, nodeXpath);
    }

    private int GetCheckBoxStartX()
    {
        CheckBoxNode node = null;
        try {
            node = (CheckBoxNode) getSelectionPath().getLastPathComponent();
        }
        catch (Exception es)
        {
            return -1;
        }

        CheckBoxNode root = (CheckBoxNode)node.getRoot();
        return (root.getDepth() - node.getDepth()) * 20;
    }

    private void AddPopMenu()
    {
        TestcasePopMenu popupMenu = new TestcasePopMenu();
        setComponentPopupMenu(popupMenu);
        this.popMenu = popupMenu;
    }

    private static CheckBoxNode AddTestNode()
    {
        CheckBoxNode node = new CheckBoxNode("测试用例");
        return node;
    }

    @Override
    public String GetComId() {
        return this.getClass().getName();
    }
}

class CheckBoxNode extends DefaultMutableTreeNode
{
    private boolean isSelect = false;
    public CheckBoxNode(String NodeStr)
    {
        super(NodeStr);
    }

    public void SetSelect(boolean isSelect)
    {
        this.isSelect = isSelect;
        int count = getChildCount();
        for (int i=0; i<count; i++)
        {
            CheckBoxNode subNode = (CheckBoxNode)getChildAt(i);
            subNode.SetSelect(isSelect);
        }
    }

    public boolean GetSelect()
    {
        return isSelect;
    }
}

class CheckBoxCellRenderer  extends JPanel implements TreeCellRenderer
{
    private JCheckBox checkBox = null;
    private JLabel label = null;
    public CheckBoxCellRenderer()
    {
        super();
        label = new JLabel();
        checkBox = new JCheckBox();
        add(checkBox);
        add(label);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
    {
        CheckBoxNode node = (CheckBoxNode)value;
        checkBox.setSelected(node.GetSelect());

        if(selected)
        {
            setBackground(Color.LIGHT_GRAY);
        }
        else
        {
            setBackground(Color.WHITE);
        }

        label.setText(value.toString());
        if (leaf==false && expanded==false)
        {
            ImageIcon icon = new ImageIcon("C:\\Users\\timmy\\Desktop\\JDemo\\TestViewer\\resource\\dir_close.PNG");
            label.setIcon(icon);
        }
        else if(leaf==false && expanded==true)
        {
            ImageIcon icon = new ImageIcon("C:\\Users\\timmy\\Desktop\\JDemo\\TestViewer\\resource\\dir_open.PNG");
            label.setIcon(icon);
        }
        else
        {
            ImageIcon icon = new ImageIcon("C:\\Users\\timmy\\Desktop\\JDemo\\TestViewer\\resource\\fail.PNG");
            label.setIcon(icon);
        }
        return this;
    }
}

class TestcasePopMenu extends JPopupMenu
{
    private String[] menuList = {"执行", "停止", "设置", "查看源码"};
    public TestcasePopMenu()
    {
        super();
        for (String menuStr : menuList)
        {
            JMenuItem item = new JMenuItem(menuStr);

            add(item);
            System.out.println(menuStr);
        }
    }

    public void SetLeafMod()
    {
        getComponent(0).setEnabled(true);
        getComponent(1).setEnabled(true);
        getComponent(2).setEnabled(true);
        getComponent(3).setEnabled(true);
    }

    public void SetDirMod()
    {
        getComponent(0).setEnabled(true);
        getComponent(1).setEnabled(true);
        getComponent(2).setEnabled(false);
        getComponent(3).setEnabled(false);
    }
}