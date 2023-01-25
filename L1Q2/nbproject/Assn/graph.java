package Assn;

import Swift.panelCreator;

import javax.swing.*;
import java.awt.*;
import java.util.Random;
import Assn.*;
class panelCreator2 {
    static int i=0; static int counter = 0; static int scale;
    static String[] clr = {"red ", "blue ","cyan ","green ",
            "pink ","YELLOW ","WHITE ","ORANGE ","magenta "};

    public void makePanel(int h, JFrame obr) {
        Color[] clr = {Color.red, Color.blue,Color.cyan,Color.green,
                Color.pink,Color.YELLOW,Color.WHITE,Color.ORANGE,Color.magenta};

        int w = 30;
        JPanel Panel = new JPanel();
        Panel.setBackground(clr[i]);
        int per = (h*400)/scale;
        if(per < 1) per = 2; //if decimal set to whole number | no decimal px height
        Panel.setBounds(counter*(w+10), 0, w, per);
        Panel.setOpaque(true);

        obr.add(Panel);
        counter++; i++;
        if(i == 8) i=0;
    }
}

public class graph {
    public void createPanel(int h, JFrame frame) {
        frame.setVisible(true);
        frame.setSize(800, 400);
        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
        frame.setLayout(null); //manual
        frame.setResizable(true);


//        int x=40;
//        frame.setSize(400+x, 400);

        panelCreator2 ob = new panelCreator2();
        ob.makePanel(h,frame);

        //sleep(500);
        //x+=40;
    }
}
