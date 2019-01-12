package com.testviewer.ui;

import com.testviewer.common.Msg;
import com.testviewer.common.MsgCom;
import com.testviewer.common.MsgQueue;
import com.testviewer.module.Testsuit;
import javafx.stage.FileChooser;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MainMenu extends JMenu implements MsgCom {
    MsgQueue query = MsgQueue.GetInstance();
    public MainMenu()
    {
        super("文件");
        setOpenXmlItem();
        setOpenTestcasePathItem();

        JMenuItem save = new JMenuItem("保存");
        add(save);
        JMenuItem setPath = new JMenuItem("设置用例路径");
        add(setPath);

        addSeparator();

        JMenuItem exit = new JMenuItem("退出");
        add(exit);

        query.RegistCom(this);
    }

    private void setOpenXmlItem()
    {
        JMenuItem open = new JMenuItem("打开xml");
        open.addActionListener(new ActionListener() {
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
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        add(open);
    }

    private void setOpenTestcasePathItem()
    {
        JMenuItem item = new JMenuItem("打开工程路径");
        item.addActionListener(new ActionListener() {
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

                Testsuit  suit = new Testsuit(chooseFile.getPath(), remotePath);

            }//public void actionPerformed(ActionEvent e)
        });
        add(item);
    }


    @Override
    public String GetComId() {
        return this.getClass().getName();
    }
}
