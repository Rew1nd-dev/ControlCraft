package com.verr1.controlcraft.utils;

public class ParseUtils {
    public static boolean tryParseLongFilter(String s) {
        if(s.isEmpty() || s.equals("-"))return true;
        try{
            Long.parseLong(s);
            return true;
        }catch (NumberFormatException e){
            return false;
        }
    }

    public static long tryParseLong(String s) {
        try{
            return Long.parseLong(s);
        }catch (NumberFormatException e){
            return 0;
        }
    }

    public static boolean tryParseDoubleFilter(String s) {
        if(s.isEmpty() || s.equals("-"))return true;
        try{
            Double.parseDouble(s);
            return true;
        }catch (NumberFormatException e){
            return false;
        }
    }

    public static boolean tryParseClampedDoubleFilter(String s, double threshold){
        if(s.isEmpty() || s.equals("-"))return true;
        try{
            double d = Double.parseDouble(s);
            if(Math.abs(d) < threshold)return true;
        }catch (NumberFormatException e){
            return false;
        }
        return false;
    }

    public static double tryParseDouble(String s) {
        try{
            return Double.parseDouble(s);
        }catch (NumberFormatException e){
            return 0;
        }
    }
}
