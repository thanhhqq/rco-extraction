package com.saigonbpo.extractions;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import com.saigonbpo.entity.config.Field;
import com.saigonbpo.entity.ocr.EntLine;
import com.saigonbpo.entity.ocr.EntPage;
import com.saigonbpo.entity.ocr.EntWord;
import java.awt.Rectangle;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Pattern;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.commons.lang3.StringUtils;

public abstract class DataExtractionAbs {

    public transient DecimalFormat decFormat = new DecimalFormat("#.00");

    public String NetAmount = "net_amount";
    public String VatRate = "vat_rate";
    public String ShippingRate = "shipping_rate";
    public String VatAmount = "vat_amount";
    public String TotalAmount = "total_amount";
    public final String SEP_AMOUNT = "[.,]";
    public String regexNumberStr1 = "([1-9]\\d{0,2})(" + SEP_AMOUNT + "\\d{3})*" + SEP_AMOUNT + "?\\d*";
    public String regexNumberStr2 = "\\d+(?:" + SEP_AMOUNT + "\\d+)?";
    public Pattern ptnNumber = Pattern.compile(regexNumberStr1);
    public String repAmountSpecial = "[^0-9.,]+";
    public final double ERROR_RATE = 0.01;
    public static final String SEP_COMMA = ",";
    public final String SEP_VAT_RATE = "\\|";

    public abstract List<EntWord> find(Field fieldConfig, EntWord keywordObj) throws Exception;

    public abstract EntWord findbyKeyword(String keywordStr, Rectangle rectWithout, boolean isForAmount) throws Exception;

    public abstract EntWord findbyKeyword(String keywordStr, Rectangle rectWithout) throws Exception;

    public abstract EntWord findbyKeyword(String keywordStr) throws Exception;

    public abstract EntWord findByKeyword(Map<Integer, EntPage> pageMap, Field fieldConfig, String keyword) throws Exception;

    public abstract List<List<EntWord>> finds(Field fieldConfig, EntWord keywordObj) throws Exception;

    public abstract List<List<EntWord>> find(Map<Integer, EntPage> pageMap, Field fieldConfig, EntWord keywordOjb) throws Exception;

    public String executeMethod(Field fieldConfig, String curValue) {
        try {
            if (fieldConfig.isLowercase()) {
                return curValue.toLowerCase();
            } else if (fieldConfig.isTrim()) {
                return curValue.trim();
            } else if (fieldConfig.isUppercase()) {
                return curValue.toUpperCase();
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return curValue;
    }

    public String compilePattern(Field fieldConfig, String curValue) {
        String newValue = StringUtils.EMPTY;
        try {
            if (fieldConfig.getPattern().trim().isEmpty() || curValue.matches(fieldConfig.getPattern())) {
                newValue = curValue;
            }
        } catch (Exception e) {
        }
        return newValue;
    }

    public String executeScript(Field fieldConfig, String curValue) {
        String newValue = curValue;
        //System.err.println(fieldConfig.getName()+": "+newValue);
        try {
            if (!fieldConfig.getScript().trim().isEmpty()) {
                ScriptEngineManager manager = new ScriptEngineManager();
                ScriptEngine engine = manager.getEngineByName("JavaScript");
                String script = "var func = new Object(); func.exec = " + fieldConfig.getScript();
                engine.eval(script);
                Invocable inv = (Invocable) engine;
                Object obj = engine.get("func");
                newValue = (String) inv.invokeMethod(obj, "exec", curValue);
            }
        } catch (NoSuchMethodException | ScriptException e) {
        }
        return newValue;
    }

    public EntWord mergeListToEntWord(List<EntWord> listWordObj) {
        if (listWordObj.size() <= 0) {
            return null;
        } else if (listWordObj.size() == 1) {
            return listWordObj.get(0);
        }

        EntWord wordObjTemp = listWordObj.get(0);
        String sep = StringUtils.SPACE;
        for (int i = 1; i < listWordObj.size(); i++) {
            wordObjTemp.setText(Objects.toString(wordObjTemp.getText(), StringUtils.EMPTY).concat(sep).concat(listWordObj.get(i).getText()));
            wordObjTemp.setBox(wordObjTemp.getBox().union(listWordObj.get(i).getBox()));
        }
        wordObjTemp.setCountWordMerged(listWordObj.size() - 1);
        return wordObjTemp;
    }

    public String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replaceAll("[đĐ]", "d");
    }

    public String trimSpecCharWithoutDigits(String text) {
        if (text.isEmpty()) {
            return text;
        }
        String optText = text;
        boolean retry = true;
        while (retry) {
            retry = false;
            char firstChar = optText.charAt(0);
            if (!Character.isDigit(firstChar)) {
                optText = optText.substring(1);
                if (optText.length() <= 1) {
                break;
            }
                retry = true;
            }
             char lastChar = optText.charAt(optText.length() - 1);
            if (!Character.isDigit(lastChar)) {
                optText = optText.substring(0, optText.length() - 1);
                if (optText.length() <= 1) {
                break;
            }
                retry = true;
            }
            
        }
        return optText;
    }

    public String trimSpecChar(String text) {
        if (text.isEmpty()) {
            return text;
        }
        String optText = text;
        boolean retry = true;
        while (retry) {
            retry = false;
            char firstChar = optText.charAt(0);
          
            if (!Character.isDigit(firstChar) && !Character.isLetter(firstChar)) {
                optText = optText.substring(1);
                 if (optText.length() <= 1) {
                break;
            }
                retry = true;
            }
              char lastChar = optText.charAt(optText.length() - 1);
            if (!Character.isDigit(lastChar) && !Character.isLetter(lastChar)) {
                optText = optText.substring(0, optText.length() - 1);
                 if (optText.length() <= 1) {
                break;
            }
                retry = true;
            }
           
        }
        return optText;
    }

    public HashMap<Integer, EntPage> copy(Map<Integer, EntPage> pageMapOrg) {
        HashMap<Integer, EntPage> copy = new HashMap<>();
        pageMapOrg.entrySet().stream().forEach((Map.Entry<Integer, EntPage> entryPage) -> {
            EntPage entPage = entryPage.getValue();
            NavigableMap<Integer, EntLine> lineMap = entPage.getLineMap();
            NavigableMap<Integer, EntLine> lineMapNew = new TreeMap<>();
            lineMap.entrySet().stream().forEach((Map.Entry<Integer, EntLine> entryLine) -> {
                EntLine entLine = entryLine.getValue();
                Map<Integer, EntWord> wordMap = entryLine.getValue().getWordMap();
                Map<Integer, EntWord> wordMapNew = new HashMap<>();
                wordMap.entrySet().stream().forEach((Map.Entry<Integer, EntWord> entryWord) -> {
                    wordMapNew.put(entryWord.getKey(), entryWord.getValue());
                });
                entLine.setWordMap(wordMapNew);
                lineMapNew.put(entryLine.getKey(), entLine);
            });
            entPage.setLineMap(lineMapNew);
            copy.put(entryPage.getKey(), entPage);
        });
        return copy;
    }

    public String parseCurrencyCommon(String value) {
        String numberStr = trimSpecCharWithoutDigits(value);
        try {
            if (numberStr.matches(".*\\d+" + SEP_AMOUNT + "\\d+.*")) {
                numberStr = numberStr.replaceAll(repAmountSpecial, StringUtils.EMPTY);
                String[] ops = numberStr.split(SEP_AMOUNT);
                numberStr = numberStr.replaceAll("[^0-9]+", StringUtils.EMPTY);
                if (ops[ops.length - 1].length() == 2) {
                    numberStr = new StringBuilder(numberStr).insert(numberStr.length() - ops[ops.length - 1].length(), ".").toString();
                }
            }
            numberStr = decFormat.format(Double.parseDouble(numberStr));
        } catch (NumberFormatException e) {
        }
        return numberStr;
    }

    public static void main(String[] args) {
        String str = "   `'\"có222...";
        char cs[] = str.toCharArray();
        for (char c : cs) {
            System.out.println(Character.isLetter(c) || Character.isDigit(c));

        }

    }
}
