package com.autotest.ui;
import com.autotest.common.Msg;
import com.autotest.common.MsgCom;
import com.autotest.common.MsgQueue;
import com.autotest.common.SwingConsole;
import com.autotest.module.TestcaseSave;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import static java.lang.Thread.sleep;

public class MainFrame extends JFrame{
    private MainMenu mainMenu = null;
    private TestcaseViewer testcaseViewer = null;
    private LogViewer logViewer = null;
    private StepViewer stepViewer = null;
    private JProgressBar progressBar = null;

    private JSplitPane splitPane1 = null;
    private JSplitPane splitPane2 = null;

    public MainFrame()
    {
        super();
        mainMenu = new MainMenu();
        JMenuBar bar = new JMenuBar();
        bar.add(mainMenu);
        setJMenuBar(bar);

        testcaseViewer = new TestcaseViewer();
        logViewer = new LogViewer();
        stepViewer = new StepViewer();
        progressBar = new JProgressBar();
        progressBar.setMaximum(100);
        progressBar.setMinimum(0);
        progressBar.setValue(0);


        splitPane1 = new JSplitPane();
        splitPane1.setLeftComponent(new JScrollPane(testcaseViewer));

        splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane2.setTopComponent(new JScrollPane(logViewer));
        splitPane2.setBottomComponent(new JScrollPane(stepViewer));

        splitPane1.setRightComponent(splitPane2);

        setLayout(new BorderLayout());
        add(splitPane1, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);
    }

    public void SetSplit()
    {
        splitPane1.setDividerLocation(0.2);
        splitPane2.setDividerLocation(0.8);
    }

    static public void main(String []args)
    {
//        TestcaseNode testcaseNode = new TestcaseNode("d://gzy", null);

        MainFrame frame = new MainFrame();
        SwingConsole.run(frame, 0, 0);

        try {
            sleep(1000);
            frame.SetSplit();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class MainMenu extends JMenu implements MsgCom
{
    private JMenuItem itemOpenDir = new JMenuItem("打开文件夹");
    private JMenuItem itemOpenFile = new JMenuItem("打开文件");
    private JMenuItem itemSave = new JMenuItem("保存");
    private JMenuItem itemSaveAs = new JMenuItem("另存为");
    private JMenuItem itemClose = new JMenuItem("退出");

    private MsgQueue queue = MsgQueue.GetInstance();
    private String xmlFilePath = null;
    public MainMenu()
    {
        super("文件");
        add(itemOpenDir);
        add(itemOpenFile);
        addSeparator();
        add(itemSaveAs);
        add(itemSave);
        addSeparator();
        add(itemClose);

        SetClose();
        SetOpenFile();
        setOpenDir();
        setSave();
        setSaveAs();

        setMenuUnOpenModel();
    }

    private void SetClose()
    {
        itemClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    private void SetOpenFile()
    {
        itemOpenFile.addActionListener(new ActionListener() {
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

                try {
                    File file = fileChooser.getSelectedFile();
                    TestcaseSave.loadFromXml(file.getAbsolutePath());
                    xmlFilePath = file.getAbsolutePath();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    return;
                }

                Msg msg = new Msg("CmdUpdateModel", GetComId(), "TestcaseViewer");
                queue.SendMessage(msg);

                setMenuHasOpenModel();
            }
        });
    }

    public void setOpenDir()
    {
        itemOpenDir.addActionListener(new ActionListener() {
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
                TestcaseNode node = new TestcaseNode(chooseFile.getAbsolutePath(), null);

                Msg msg = new Msg("CmdUpdateModel", GetComId(), "TestcaseViewer");
                queue.SendMessage(msg);
                setMenuHasOpenModel();
            }//public void actionPerformed(ActionEvent e)
        });
    }

    private void setSave()
    {
        itemSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (xmlFilePath==null)
                {
                    return;
                }
                TestcaseSave save = new TestcaseSave();
                try {
                    save.saveToXml(xmlFilePath);
                    JOptionPane.showMessageDialog(null,"保存成功");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private void setSaveAs()
    {
        itemSaveAs.addActionListener(new ActionListener() {
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
                xmlFilePath = xmlPath.getAbsolutePath();
                TestcaseSave save = new TestcaseSave();
                try {
                    save.saveToXml(xmlFilePath);
                    JOptionPane.showMessageDialog(null,"成功保存到："+xmlFilePath);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private void setMenuUnOpenModel()
    {
        itemOpenDir.setEnabled(true);
        itemOpenFile.setEnabled(true);
        itemSave.setEnabled(false);
        itemSaveAs.setEnabled(false);
    }

    private void setMenuHasOpenModel()
    {
        itemOpenDir.setEnabled(false);
        itemOpenFile.setEnabled(false);
        if (xmlFilePath==null)
            itemSave.setEnabled(false);
        else
            itemSave.setEnabled(true);
        itemSaveAs.setEnabled(true);
    }

    @Override
    public String GetComId() {
        return "MainMenu";
    }
}
