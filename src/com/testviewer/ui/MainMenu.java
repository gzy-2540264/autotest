package com.testviewer.ui;

import javax.swing.*;

public class MainMenu extends JMenu {
    public MainMenu()
    {
        super("文件");
        JMenuItem open = new JMenuItem("打开");
        add(open);
        JMenuItem save = new JMenuItem("保存");
        add(save);
        JMenuItem setPath = new JMenuItem("设置用例路径");
        add(setPath);

        addSeparator();

        JMenuItem exit = new JMenuItem("退出");
        add(exit);

    }
}
