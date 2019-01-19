package com.testviewer.ui;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.sun.scenario.effect.impl.sw.java.JSWBlend_COLOR_BURNPeer;
import com.testviewer.common.Msg;
import com.testviewer.common.MsgCom;
import com.testviewer.common.MsgQueue;
import com.testviewer.module.Testcase;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.Vector;

public class RunStepViewer extends JPanel implements MsgCom {
    JTable table = new JTable();
    MsgQueue queue = MsgQueue.GetInstance();

    public RunStepViewer()
    {
        setLayout(new BorderLayout());
        add(table);

        queue.RegistCom(this);
    }

    public void CmdSetItemSetting(Msg msg)
    {
        Testcase testcase = (Testcase) msg.GetParam("testcase");
        TableModel tableModel = new TestcaseTableMode(testcase);
        table.setModel(tableModel);
        repaint();
    }

    public void CmdReset(Msg msg)
    {
        TableModel tableModel = new TestcaseTableMode(null);
        table.setModel(tableModel);
        repaint();
    }

    @Override
    public String GetComId() {
        return getClass().getName();
    }
}

class TestcaseTableMode implements TableModel {
    private Testcase testcase = null;
    private String[] fieldNames = {"testScriptPath", "testRunLogPath", "runCmd", "runTimes", "isFailStop", "runTimeout",
    "passCheckPattern", "curStatus"};
    private String[] fieldDescs = {"脚本路径(只读)", "脚本执行日志存放本地目录", "脚本运行命令", "脚本运行次数",
            "失败后是否继续执行", "脚本执行超时时间", "脚本执行成功日志样式", "脚本当前状态(只读)"};

    private String[] columns = {"配置项名", "配置项值", "配置说明"};


    public TestcaseTableMode(Testcase testcase)
    {
        this.testcase = testcase;
    }

    @Override
    public int getRowCount() {
        if (testcase==null)
        {
            return 0;
        }
        return fieldNames.length;
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columns[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex==0 || columnIndex==2)
        {
            return false;
        }
        if (fieldDescs[rowIndex].indexOf("(只读)")>=0)
        {
            return false;
        }
        return true;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (testcase==null)
        {
            return "";
        }

        Object rspObj = null;
        if (columnIndex==0) {
            rspObj = fieldNames[rowIndex];
        }
        else if(columnIndex==2)
        {
            rspObj = fieldDescs[rowIndex];
        }
        else if(columnIndex==1)
        {
            String fieldName = fieldNames[rowIndex];
            if (fieldName.equals("testScriptPath"))
            {
                rspObj =  testcase.getTestScriptPath();
            }
            else if(fieldName.equals("testRunLogPath"))
            {
                rspObj =  testcase.getTestRunLogPath();
            }
            else if(fieldName.equals("runCmd"))
            {
                rspObj =  testcase.getRunCmd();
            }
            else if(fieldName.equals("runTimes"))
            {
                rspObj =  testcase.getRunTimes();
            }
            else if(fieldName.equals("isFailStop"))
            {
                rspObj =  testcase.isFailStop();
            }
            else if(fieldName.equals("runTimeout"))
            {
                rspObj =  testcase.getRunTimeout();
            }
            else if(fieldName.equals("passCheckPattern"))
            {
                rspObj =  testcase.getPassCheckPattern();
            }
            else if(fieldName.equals("curStatus"))
            {
                rspObj =  testcase.getCurStatus();
            }
            else
            {
                rspObj = null;
            }
        }
        return rspObj;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (testcase==null)
        {
            return;
        }
        if (columnIndex!=1)
        {
            return;
        }
        String fieldName = fieldNames[rowIndex];

        if(fieldName.equals("testRunLogPath"))
        {
            testcase.setTestRunLog((String)aValue);
        }
        else if(fieldName.equals("runCmd"))
        {
            testcase.setRunCmd((String)aValue);
        }
        else if(fieldName.equals("runTimes"))
        {
            testcase.setRunTimes(Integer.valueOf((String)aValue));
        }
        else if(fieldName.equals("isFailStop"))
        {
            String isFailStopStr = (String)aValue;
            if(isFailStopStr.toLowerCase().equals("true"))
            {
                testcase.setFailStop(true);
            }
            else
            {
                testcase.setFailStop(false);
            }
        }
        else if(fieldName.equals("runTimeout"))
        {
            testcase.setRunTimeout(Integer.valueOf((String)aValue));
        }
        else if(fieldName.equals("passCheckPattern"))
        {
            testcase.setPassCheckPattern((String)aValue);
        }
        else
        {
            System.out.println("error field name: " + fieldName );
        }
    }

    @Override
    public void addTableModelListener(TableModelListener l) {

    }

    @Override
    public void removeTableModelListener(TableModelListener l) {

    }
}
