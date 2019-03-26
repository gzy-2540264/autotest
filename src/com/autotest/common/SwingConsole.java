//: net/mindview/util/SwingConsole.java
// Tool for running Swing demos from the
// console, both applets and JFrames.
package com.autotest.common;

import javax.swing.*;
import java.awt.*;

public class SwingConsole {
    public static void run(final JFrame f, final int width, final int height) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                f.setTitle(f.getClass().getSimpleName());
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                int setWidth = width;
                int setHeight = height;
                if(0==width || 0==height)
                {
                    Toolkit toolkit = f.getToolkit();
                    Dimension dimension = toolkit.getScreenSize();
                    if (0==width)
                    {
                        setWidth = dimension.width*8/10;
                    }
                    if(0==height)
                    {
                        setHeight = dimension.height*8/10;
                    }
                }
                f.setSize(setWidth, setHeight);
                f.setLocationByPlatform(true);
                f.setVisible(true);
            }
        });
    }
} ///:~