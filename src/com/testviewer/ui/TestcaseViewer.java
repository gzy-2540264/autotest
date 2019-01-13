package com.testviewer.ui;

import com.testviewer.common.Msg;
import com.testviewer.common.MsgCom;
import com.testviewer.common.MsgQueue;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TestcaseViewer extends JTree implements MsgCom
{
    private TestcasePopMenu popMenu = null;
    static private DefaultMutableTreeNode root = new CheckBoxNode("测试用例");

    MsgQueue query = MsgQueue.GetInstance();
    public TestcaseViewer()
    {
        super(root);
        CheckBoxCellRenderer renderer = new CheckBoxCellRenderer();
        setCellRenderer(renderer);

//        setCellEditor(new CheckBoxCellEditer(this));
//        setEditable(true);
        AddPopMenu();
        query.RegistCom(this);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if ( getSelectionPath() ==null)
                {
                    return;
                }
                CheckBoxNode node = (CheckBoxNode) getSelectionPath().getLastPathComponent();

                //设置配置面板
                Msg msg = new Msg("CmdSetStepViewer", null, GetSelectNodeXpath());
                query.SendMessage(msg);

                int hotspot = new JCheckBox().getPreferredSize().width;
                TreePath path = getPathForLocation(e.getX(), e.getY());
                if(path==null)
                    return;
                if(e.getX()>getPathBounds(path).x+hotspot)
                    return;
                if(e.getY()>getPathBounds(path).y+hotspot)
                    return;

                boolean isSelect = node.GetSelect();
                if(isSelect)
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
                if (nextPath.length()>0) {
                    AddTreeNode(node, nextPath);
                }
            }
        }

        // 说明没有找到对应的结点，需要增加
        if (findNode==null)
        {
            CheckBoxNode node = new CheckBoxNode(curNodeStr);
            curRoot.add(node);
            if (nextPath.length()>0) {
                AddTreeNode(node, nextPath);
            }
        }
    }

    public void CmdAddNode(Msg msg)
    {
        AddTreeNode((CheckBoxNode)root, (String)msg.GetParam("nodeXpath"));
        //不知道为什么，如果不展开的话，后面就无法展开了
        expandRow(0);
        repaint();

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

    public String GetSelectNodeXpath()
    {
        TreePath path =  getSelectionPath();
        Object[] list = path.getPath();
        StringBuffer rspStrBuff = new StringBuffer();
        boolean isRootNode = true;
        for (Object str : list) {
            if (isRootNode)
            {
                isRootNode = false;
                continue;
            }
            if (rspStrBuff.length()==0) {
                rspStrBuff.append(str);
            }
            else
            {
                rspStrBuff.append("/");
                rspStrBuff.append(str);
            }
        }
        System.out.println(rspStrBuff.toString());
        return rspStrBuff.toString();
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
        for (int i=0; i<count; i++) {
            CheckBoxNode subNode = (CheckBoxNode) getChildAt(i);
            subNode.SetSelect(isSelect);
        }
    }

    public boolean GetSelect()
    {
        return isSelect;
    }
}

class CheckBoxCellEditer extends AbstractCellEditor implements TreeCellEditor
{
    JTree tree = null;
    private JCheckBox checkBox = new JCheckBox();
    private JLabel label = new JLabel();
    private JPanel panel = new JPanel();
    private CheckBoxNode node = null;
    public CheckBoxCellEditer(JTree tree)
    {
        super();
        panel.add(checkBox);
        panel.add(label);
        this.tree = tree;

        addCellEditorListener(new CellEditorListener() {
            @Override
            public void editingStopped(ChangeEvent e) {

            }

            @Override
            public void editingCanceled(ChangeEvent e) {

            }
        });

        checkBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                node.SetSelect(checkBox.isSelected());
                tree.repaint();
            }
        });
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
        node = (CheckBoxNode)value;
        checkBox.setSelected(node.GetSelect());

        if(isSelected)
        {
            panel.setBackground(Color.LIGHT_GRAY);
        }
        else
        {
            panel.setBackground(Color.WHITE);
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
        return panel;

    }

    @Override
    public Object getCellEditorValue() {
        return checkBox.isSelected();
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
    private String[] menuList = {"执行", "停止", "设置", "查看源码", "同步"};
    public TestcasePopMenu()
    {
        super();
        for (String menuStr : menuList)
        {
            JMenuItem item = new JMenuItem(menuStr);

            add(item);
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