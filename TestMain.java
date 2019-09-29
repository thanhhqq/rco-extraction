/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.saigonbpo.extractingbot_ocr;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.TreeSet;

/**
 *
 * @author Quoc Thanh
 */
public class TestMain {
       public static void main(String[] arg) throws ParseException{
          DecimalFormat dfEvent = new DecimalFormat("###");
          DecimalFormat df = new DecimalFormat("#.##");      
          System.out.println(df.format(Double.parseDouble("1.5067")));
           String value = "123.";
           
           TreeSet<Double> dd = new TreeSet<Double>();
           dd.add(Double.parseDouble("1.5067"));
           dd.add(Double.parseDouble("3.5067"));
           dd.add(Double.parseDouble("2.5067"));
           dd.add(Double.parseDouble("7.5067"));
           dd.add(Double.parseDouble("5.5067"));
           for (Double dd1 : dd) {
               System.out.println(dd1);
           }
           String pattern = "([1-9]\\\\d{0,2}(,\\\\d{3})*[.]?\\\\d*)";
           System.err.println(value.matches(pattern));
//           NumberFormat format = NumberFormat.getCurrencyInstance();
//           Number number = format.parse("£28,510.00");
//           System.out.println(number.toString());
           System.out.println("£28,510.00".replaceAll("\\p{IsL}", ""));
       }
}
