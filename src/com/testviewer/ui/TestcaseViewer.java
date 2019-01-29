package com.testviewer.ui;

import com.testviewer.common.Common;
import com.testviewer.common.Msg;
import com.testviewer.common.MsgCom;
import com.testviewer.common.MsgQueue;
import com.testviewer.module.Testcase;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
                if (node.isLeaf()) {
                    Msg msg = new Msg("CmdSetStepViewer", null, GetSelectNodeXpath());
                    query.SendMessage(msg);
                }
                else
                {
                    Msg msgSend = new Msg("CmdSetItemSetting", null, "com.testviewer.ui.RunStepViewer");
                    msgSend.SetParam("testcase", null);
                    query.SendMessage(msgSend);
                }

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
                    Msg msg = new Msg("CmdSetTestcaseRun", null, GetSelectNodeXpath());
                    msg.SetParam("isTestcaseRun", false);
                    query.SendMessage(msg);
                }
                else
                {
                    node.SetSelect(true);
                    Msg msg = new Msg("CmdSetTestcaseRun", null, GetSelectNodeXpath());
                    msg.SetParam("isTestcaseRun", true);
                    query.SendMessage(msg);
                }
                repaint();
            }
        });

        addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                CheckBoxNode node = (CheckBoxNode) e.getPath().getLastPathComponent();
                if (node==null)
                {
                    return;
                }

                if(node.isLeaf())
                {
                    popMenu.SetMenuMod(TestcasePopMenu.MenuMode.LEAF_IDLE);
                }
                else
                {
                    popMenu.SetMenuMod(TestcasePopMenu.MenuMode.DIR_IDLE);
                }
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

    public void CmdSetNodeSelect(Msg msg)
    {
        String nodeXpath = (String)msg.GetParam("nodeXpath");
        Boolean isTestcaseRun = (Boolean)msg.GetParam("isTestcaseRun");

        CheckBoxNode node = GetNodeByXPath((CheckBoxNode) root, nodeXpath);
        if (node!=null)
        {
            node.SetSelect(isTestcaseRun);
        }
    }

    public void CmdReset(Msg msg)
    {
        root = new CheckBoxNode("测试用例");
        setModel(new DefaultTreeModel(root));
        repaint();
    }


    public void CmdModeToViewSys(Msg msg)
    {
        Testcase testcase = (Testcase) msg.GetParam("testcase");
        CheckBoxNode node = GetNodeByXPath((CheckBoxNode)root, testcase.GetTreeXpath());
        node.setTeststatus(testcase.getCurStatus());
        node.SetSelect(testcase.isTestcaseRun());
        repaint();
    }

    private void AddPopMenu()
    {
        TestcasePopMenu popupMenu = new TestcasePopMenu(this);
        setComponentPopupMenu(popupMenu);
        this.popMenu = popupMenu;
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
        return rspStrBuff.toString();
    }

    public String GetNodeXpath(CheckBoxNode node)
    {
        TreeNode[] path =  node.getPath();
        StringBuffer rspStrBuff = new StringBuffer();
        for (TreeNode subNode : path) {
            if(subNode==root)
            {
                continue;
            }
            if (rspStrBuff.length()==0) {
                rspStrBuff.append(subNode.toString());
            }
            else
            {
                rspStrBuff.append("/");
                rspStrBuff.append(subNode.toString());
            }
        }
        return rspStrBuff.toString();
    }

    public CheckBoxNode GetNodeByXPath(CheckBoxNode rootNode, String nodeXpath)
    {
        String[] nodeList = nodeXpath.split("/");
        String curNodeStr = nodeList[0];
        String nextPath = "";
        CheckBoxNode rspNode = null;
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

        if (rootNode==root)
        {
            for(int i=0; i<rootNode.getChildCount(); i++)
            {
                if(rootNode.getChildAt(i).toString().equals(curNodeStr))
                {
                    rspNode = GetNodeByXPath((CheckBoxNode) rootNode.getChildAt(i), nextPath);
                    break;
                }
            }
        }
        if (rspNode!=null)
        {
            return rspNode;
        }


        for(int i=0; i<rootNode.getChildCount(); i++)
        {
            if(rootNode.getChildAt(i).toString().equals(curNodeStr))
            {
                if (nextPath.length()==0)
                {
                    rspNode =(CheckBoxNode) rootNode.getChildAt(i);
                }
                else
                {
                    rspNode = GetNodeByXPath((CheckBoxNode) rootNode.getChildAt(i), nextPath);
                }
                if (rspNode!=null)
                {
                    break;
                }
            }
        }
        return rspNode;
    }

    public void ShowFileCode()
    {
        Msg msg = new Msg("CmdShowFileCode", null, GetSelectNodeXpath());
        query.SendMessage(msg);
    }

    public void RunTestcase()
    {
        CheckBoxNode node = (CheckBoxNode) getSelectionPath().getLastPathComponent();
        if(node.isLeaf())
        {
            Msg msg = new Msg("CmdRun", null, GetSelectNodeXpath());
            query.SendMessage(msg);
        }
        else
        {
            //需要发给testsuit,让他来组织执行CmdRunTestcases
            Msg msg = new Msg("CmdRunTestcases", null, "com.testviewer.module.Testsuit");
            msg.SetParam("xpath", GetSelectNodeXpath());
            query.SendMessage(msg);
        }
    }
}

class CheckBoxNode extends DefaultMutableTreeNode
{
    private boolean isSelect = false;
    private Testcase.TESTCASE_STATUS teststatus = Testcase.TESTCASE_STATUS.IDLE;

    public CheckBoxNode(String nodeStr)
    {
        super(nodeStr);
    }

    public void SignSetSelect(boolean isSelect)
    {
        this.isSelect = isSelect;
    }

    public void SetSelect(boolean isSelect)
    {

        this.isSelect = isSelect;

        int count = getChildCount();
        for (int i=0; i<count; i++) {
            CheckBoxNode subNode = (CheckBoxNode) getChildAt(i);
            subNode.SetSelect(isSelect);
        }

        // 子结点选择，父结点必须选择，反之不亦然
        CheckBoxNode parent = (CheckBoxNode)getParent();
        if (isSelect==true && parent!=null)
        {
            parent.SignSetSelect(isSelect);
        }

    }

    public boolean GetSelect()
    {
        return isSelect;
    }

    public Testcase.TESTCASE_STATUS getTeststatus() {
        return teststatus;
    }

    public void setTeststatus(Testcase.TESTCASE_STATUS teststatus) {
        this.teststatus = teststatus;
    }
}

/*
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
*/
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
        String curPath = Common.GetCurDir();
        if (leaf==false && expanded==false)
        {
            URL fileURL = this.getClass().getResource("/resource/dir_close.PNG");
            ImageIcon icon = new ImageIcon(fileURL.getPath());
            label.setIcon(icon);
        }
        else if(leaf==false && expanded==true)
        {
            URL fileURL = this.getClass().getResource("/resource/dir_open.PNG");
            ImageIcon icon = new ImageIcon(fileURL.getPath());
            label.setIcon(icon);
        }
        else
        {
            Testcase.TESTCASE_STATUS status = node.getTeststatus();
            String sourcePath = null;
            if (status==Testcase.TESTCASE_STATUS.FAILED)
            {
                sourcePath = "/resource/fail.PNG";
            }
            else if(status==Testcase.TESTCASE_STATUS.PASSED)
            {
                sourcePath = "/resource/pass.PNG";
            }
            else if(status==Testcase.TESTCASE_STATUS.ERROR)
            {
                sourcePath = "/resource/error.PNG";
            }
            else if(status==Testcase.TESTCASE_STATUS.RUNNING)
            {
                sourcePath = "/resource/run.PNG";
            }
            else
            {
                sourcePath = "/resource/idle.PNG";
            }
            URL fileURL = this.getClass().getResource(sourcePath);
            System.out.println(fileURL.getPath());
            label.setIcon(new ImageIcon(fileURL.getPath()));
        }
        return this;
    }
}

class TestcasePopMenu extends JPopupMenu
{
    private String[] menuList = {"执行", "停止", "查看源码"};
    public enum MenuMode
    {
        LEAF_IDLE,
        LEAF_RUNNING,
        DIR_IDLE,
        DIR_RUNNING
    }
    private HashMap<MenuMode, boolean[]> modeDefine = new HashMap<MenuMode, boolean[]>(){
        {put(MenuMode.LEAF_IDLE,    new boolean[]{true, false, true});
        put(MenuMode.LEAF_RUNNING, new boolean[]{false, true, false});
        put(MenuMode.DIR_IDLE,     new boolean[]{true, false, false});
        put(MenuMode.DIR_RUNNING,  new boolean[]{false, true, false});}
    };
    TestcaseViewer parentTree = null;

    public TestcasePopMenu(TestcaseViewer parentTree)
    {
        super();
        BuildMenuItems();
        this.parentTree = parentTree;
        SetCodeViewListener();
        SetTestcaseRunListener();
    }

    private void BuildMenuItems() {
        for (String menuStr : menuList)
        {
            JMenuItem item = new JMenuItem(menuStr);
            add(item);
        }
    }

    public void SetMenuMod(TestcasePopMenu.MenuMode mode)
    {
        boolean[] modeList = modeDefine.get(mode);
        int index = 0;
        for(boolean menuEnble : modeList)
        {
            JMenuItem item = (JMenuItem)getComponent(index);
            item.setEnabled(menuEnble);
            index += 1;
        }
    }

    private JMenuItem SearchItemByText(String str)
    {
        int index = 0;
        JMenuItem getItem = null;
        int count = getComponentCount();
        for (int i=0; i<count; i++)
        {
            JMenuItem item = (JMenuItem)getComponent(i);
            if (item.getText().equals(str))
            {
                getItem = item;
                break;
            }
        }
        return getItem;
    }

    public void SetCodeViewListener()
    {
        JMenuItem item = SearchItemByText("查看源码");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parentTree.ShowFileCode();
            }
        });
    }

    public void SetTestcaseRunListener()
    {
        JMenuItem item = SearchItemByText("执行");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parentTree.RunTestcase();
            }
        });
    }
}