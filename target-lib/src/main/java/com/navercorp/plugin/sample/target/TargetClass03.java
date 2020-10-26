package com.navercorp.plugin.sample.target;

public class TargetClass03 {

    public void invoke() {
        invoke(10);
    }

    public void invoke(int times) {
        System.out.println("invoke");
        if (times <= 1) {
            return;
        }

        invoke(times - 1);
    }

    public static void main(String[] args){
        new TargetClass03().invoke();
    }
}