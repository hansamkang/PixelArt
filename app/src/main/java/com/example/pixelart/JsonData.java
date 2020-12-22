package com.example.pixelart;

public class JsonData {
    String canvas[][];

    JsonData(String canvas[][]){
        this.canvas = canvas;
    }

    String [][] getCanvas(){ return canvas; }
}
