package com.autotest.ui;

import com.autotest.common.Msg;
import com.autotest.common.MsgCom;
import com.autotest.common.MsgQueue;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.*;

public class StepViewer extends JTable implements MsgCom {
    MsgQueue queue = null;
    public StepViewer()
    {
        setFont(new Font("宋体", Font.BOLD,18));
        queue = MsgQueue.GetInstance();
        queue.RegistCom(this);
        setModel(new TestcaseTableMode(null));
        setRowHeight(24);
    }

    public void CmdUpdateShow(Msg msg)
    {
        TestcaseNode node = (TestcaseNode)msg.GetParam("Testcase");
        if(node!=null && node.isLeaf())
            setModel(new TestcaseTableMode(node));
    }

    @Override
    public String GetComId() {
        return "StepViewer";
    }
}


class TestcaseTableMode implements TableModel {
    private TestcaseNode testcase = null;
    private String[] fieldNames = {"testScriptPath", "runCmd", "runTimeout",
            "passCheckPattern", "curStatus"};
    private String[] fieldDescs = {"脚本路径(只读)",  "脚本运行命令",  "脚本执行超时时间", "脚本执行成功日志样式", "脚本当前状态(只读)"};

    private String[] columns = {"配置项名", "配置项值", "配置说明"};

    public TestcaseTableMode(TestcaseNode testcase)
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
        if (testcase==null)
        {
            return 0;
        }
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
                rspObj =  testcase.getMapPath();
            }
            else if(fieldName.equals("runCmd"))
            {
                rspObj =  testcase.getRunCmd();
            }
            else if(fieldName.equals("runTimeout"))
            {
                rspObj =  testcase.getRunTimeout();
            }
            else if(fieldName.equals("passCheckPattern"))
            {
                rspObj =  testcase.getCheckPartten();
            }
            else if(fieldName.equals("curStatus"))
            {
                rspObj =  testcase.getStatus();
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

        if(fieldName.equals("runCmd"))
        {
            testcase.setRunCmd((String)aValue);
        }
        else if(fieldName.equals("runTimeout"))
        {
            testcase.setRunTimeout(Integer.valueOf((String)aValue));
        }
        else if(fieldName.equals("passCheckPattern"))
        {
            testcase.setCheckPartten((String)aValue);
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