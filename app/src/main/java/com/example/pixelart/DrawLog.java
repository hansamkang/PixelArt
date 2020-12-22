package com.example.pixelart;

import android.util.Log;

public class DrawLog {
    int x;
    int y;
    String color;
    boolean flag[][];

    DrawLog(int x, int y, String color, boolean flag[][]){
        this.x = x; this.y = y; this.color = color; this.flag = flag;
    }

    int getX() { return x; }
    int getY() { return y; }
    String getColor() { return color; }
    boolean[][] getFlag(){ return flag; }
    boolean isEmty() {

        if(flag == null)
            return true;
        else
            return false;
    }
}
