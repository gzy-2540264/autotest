package com.testviewer.ui;

import com.testviewer.common.Msg;
import com.testviewer.common.MsgCom;
import com.testviewer.common.MsgQueue;
import com.testviewer.module.Testsuit;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MainMenu extends JMenu implements MsgCom {
    MsgQueue query = MsgQueue.GetInstance();

    enum MENU_MODE
    {
        MENU_MODE_INIT,
        MENU_MODE_READ_PROJECT,
    }

    private JMenuItem importXml = null;
    private JMenuItem exportXml = null;
    private JMenuItem openDir = null;
    private JMenuItem closeDir = null;
    private JMenuItem exitSystem = null;

    public MainMenu()
    {
        super("文件");
        setOpenXmlItem();
        setSaveTestcasePathItem();
        addSeparator();

        setOpenTestcasePathItem();
        setCloseTestcasePathItem();

        addSeparator();

        exitSystem = new JMenuItem("退出");
        exitSystem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        add(exitSystem);


        SetMenuMode(MENU_MODE.MENU_MODE_INIT);
        query.RegistCom(this);
    }

    private void setOpenXmlItem()
    {
        importXml = new JMenuItem("导入工程(xml)");
        importXml.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 选择文件
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        if (f.isDirectory())
                        {
                            return false;
                        }
                        else
                        {
                            String name = f.getName();
                            if (name.endsWith(".xml"))
                            {
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public String getDescription() {
                        return "*.xml";
                    }
                });

                // 通过下面的代码获取桌面路径
                FileSystemView fsv = FileSystemView.getFileSystemView();
                File com=fsv.getHomeDirectory();
                fileChooser.setCurrentDirectory(com);
                fileChooser.showOpenDialog(null);

                File file = fileChooser.getSelectedFile();
                try {
                    Testsuit.LoadFromXml(file.getPath());
                    SetMenuMode(MENU_MODE.MENU_MODE_READ_PROJECT);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        add(importXml);
    }

    private void setSaveTestcasePathItem()
    {
        exportXml = new JMenuItem("保存工程(xml)");
        exportXml.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.SAVE_DIALOG |JFileChooser.DIRECTORIES_ONLY);
                fileChooser.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        if (f.isDirectory())
                        {
                            return false;
                        }
                        else
                        {
                            String name = f.getName();
                            if (name.endsWith(".xml"))
                            {
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public String getDescription() {
                        return "*.xml";
                    }
                });
                fileChooser.showOpenDialog(null);
                File xmlPath = fileChooser.getSelectedFile();
                String xmlAbsPath = xmlPath.getAbsolutePath();


                Msg msg = new Msg("CmdSaveToXml", null, "com.testviewer.module.Testsuit");
                msg.SetParam("xmlPath", xmlAbsPath);
                query.SendMessage(msg);
            }
        });
        add(exportXml);
    }

    private void setOpenTestcasePathItem()
    {
        openDir = new JMenuItem("打开文件");
        openDir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        if(f.isDirectory())
                        {
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public String getDescription() {
                        return null;
                    }
                });
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.showOpenDialog(null);

                File chooseFile = fileChooser.getSelectedFile();
                if (null==chooseFile)
                {
                    return;
                }

                String remotePath = JOptionPane.showInputDialog(null, "远程桌面路径");
                if (remotePath.length()<=0)
                {
                    return;
                }
                Testsuit  suit = new Testsuit(remotePath, chooseFile.getPath());
                SetMenuMode(MENU_MODE.MENU_MODE_READ_PROJECT);
            }//public void actionPerformed(ActionEvent e)
        });
        add(openDir);
    }

    private void setCloseTestcasePathItem()
    {
        closeDir = new JMenuItem("关闭文件");

        // 删除所有testcase
        add(closeDir);
    }

    private  void SetMenuMode(MENU_MODE mode)
    {
        switch (mode)
        {
            case MENU_MODE_INIT:
                importXml.setEnabled(true);
                exportXml.setEnabled(false);
                openDir.setEnabled(true);
                closeDir.setEnabled(false);
                exitSystem.setEnabled(true);
                break;
            case MENU_MODE_READ_PROJECT:
                importXml.setEnabled(false);
                exportXml.setEnabled(true);
                openDir.setEnabled(false);
                closeDir.setEnabled(true);
                exitSystem.setEnabled(true);
                break;
        }
    }

    @Override
    public String GetComId() {
        return this.getClass().getName();
    }

}
