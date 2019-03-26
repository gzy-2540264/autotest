package com.autotest.ui;

import com.autotest.common.MsgCom;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

public class TreeShowPane  extends JPanel{
    enum NODE_STATUS{
        NODE_DIR_OPEN,
        NODE_DIR_CLOSE,
        NODE_FILE_IDLE,
        NODE_FILE_PASS,
        NODE_FILE_FAIL,
        NODE_FILE_ERROR,
        NODE_FILE_RUNNING
    }
    private JTree tree = null;
    private JCheckBox checkBox = null;
    private JLabel label = null;
    public TreeShowPane(JTree tree)
    {
        this.tree = tree;
        checkBox = new JCheckBox();
        label = new JLabel();
        add(checkBox);
        add(label);
    }

    public void setShowTitle(String value)
    {
        label.setText(value);
    }

    public void setSelect(boolean isSelect)
    {
        checkBox.setSelected(isSelect);
    }

    public boolean getSelect()
    {
        return checkBox.isSelected();
    }

    public void setIcon(NODE_STATUS status)
    {
        String iconPath = null;
        if (status == NODE_STATUS.NODE_DIR_CLOSE) {
            iconPath = "/resource/dir_close.PNG";
        }
        else if (status == NODE_STATUS.NODE_DIR_OPEN)
        {
            iconPath = "/resource/dir_open.PNG";
        }
        else if(status == NODE_STATUS.NODE_FILE_ERROR)
        {
            iconPath = "/resource/error.PNG";
        }
        else if(status == NODE_STATUS.NODE_FILE_FAIL)
        {
            iconPath = "/resource/fail.PNG";
        }
        else if(status == NODE_STATUS.NODE_FILE_IDLE)
        {
            iconPath = "/resource/idle.PNG";
        }
        else if(status == NODE_STATUS.NODE_FILE_PASS)
        {
            iconPath = "/resource/pass.PNG";
        }
        else if(status == NODE_STATUS.NODE_FILE_RUNNING)
        {
            iconPath = "/resource/run.PNG";
        }
        else
        {
            iconPath = "/resource/idle.PNG";
        }
        URL fileURL = this.getClass().getResource(iconPath);
        label.setIcon(new ImageIcon(fileURL));
    }
}