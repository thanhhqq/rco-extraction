package com.saigonbpo.entity.ocr;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.saigonbpo.entity.config.Field;
import com.saigonbpo.entity.config.Position;
import com.saigonbpo.extractions.DataExtractionAbs;
import org.apache.commons.lang3.StringUtils;

public class EntLine extends DataExtractionAbs {

    private String textOriginal;
    private Rectangle box = new Rectangle(0, 0, 0, 0);
    private int pageNr;
    private Map<Integer, EntWord> wordMap = new HashMap<>();

    public String getTextOriginal() {
        return textOriginal;
    }

    public void setTextOriginal(String textOriginal) {
        this.textOriginal = textOriginal;
    }

    public Rectangle getBox() {
        return box;
    }

    public void setBox(Rectangle box) {
        this.box = box;
    }

    public int getPageNr() {
        return pageNr;
    }

    public void setPageNr(int pageNr) {
        this.pageNr = pageNr;
    }

    public Map<Integer, EntWord> getWordMap() {
        return wordMap;
    }

    public void setWordMap(Map<Integer, EntWord> wordMap) {
        this.wordMap = wordMap;
    }

    @Override
    public String toString() {
        return "EntLine [value=" + textOriginal + ", box=" + box + ", pageNr=" + pageNr + ", wordMap=" + wordMap + "]";
    }

//    @Override
//    public EntWord findbyKeyword(String keywordStr) throws Exception {
//        String newKeywordStr = "^" + keywordStr + "$";
//        //Find keyword object with 2 mode (Absolute & Relative)
//        boolean findRelativeMode = false;
//        do {
//            for (Map.Entry<Integer, EntWord> entry : wordMap.entrySet()) {
//                EntWord entWord = entry.getValue();
//                Matcher match = Pattern.compile(newKeywordStr).matcher(entWord.getText());
//                if (match.find()) {
//                    return entWord;
//                }
//            }
//            if (findRelativeMode) {
//                break;
//            }
//            findRelativeMode = true;
//            newKeywordStr = keywordStr;
//        } while (true);
//        return null;
//    }
    //New improve (keyword contain space)
    @Override
    public EntWord findbyKeyword(String keywordStr) throws Exception {
        return findbyKeyword(keywordStr, new Rectangle());
    }

    @Override
    public EntWord findbyKeyword(String keywordStr, Rectangle rectWithout) throws Exception {
        return findbyKeyword(keywordStr, new Rectangle(), false);
    }

    @Override
    public EntWord findbyKeyword(String keywordStr, Rectangle rectWithout, boolean isForAmount) throws Exception {
        List<EntWord> listWordObj = new ArrayList<>();
        String[] arrKeywordStr = keywordStr.split(StringUtils.SPACE);
        String beginChar = "^";
        String endChar = "$";
        //Find keyword object with 2 mode Absolute (default) & Relative
        boolean findRelativeMode = false;
        do {
            boolean flagFound = false;

            for (Map.Entry<Integer, EntWord> entry : wordMap.entrySet()) {
                //System.out.println("Word Key: " + entry.getKey());
                flagFound = true;
                listWordObj.clear();
                for (int kwIndex = 0; kwIndex < arrKeywordStr.length; kwIndex++) {
                    try {
                        EntWord entWord = wordMap.get(entry.getKey() + kwIndex);
                        String keyword = removeAccent(beginChar + arrKeywordStr[kwIndex] + endChar);
                        Matcher match = Pattern.compile(keyword, Pattern.CASE_INSENSITIVE).matcher(removeAccent(entWord.getText()));
                        if (!match.find()
                                || (entWord.getBox().x <= rectWithout.x && entWord.getBox().y <= rectWithout.y)
                                || (entWord.getBox().x >= rectWithout.x && entWord.getBox().y <= rectWithout.y)) {
                            throw new Exception();
                        }
                        listWordObj.add(entWord);
                    } catch (Exception e) {
                        flagFound = false;
                        break;
                    }
                }
                if (flagFound) {
                    break;
                }
            }
            if (findRelativeMode || flagFound) {
                break;
            }
            findRelativeMode = true;
            beginChar = endChar = StringUtils.EMPTY;
        } while (true);
        return mergeListToEntWord(listWordObj);
    }

    @Override
    public List<EntWord> find(Field fieldConfig, EntWord keywordObj) throws Exception {
        List<EntWord> result = new ArrayList<>();
        // Change range by position
        int rangeXBegin = keywordObj.getBox().x - fieldConfig.getWordSpace().getLeft();
        int rangeXEnd = keywordObj.getBox().x + keywordObj.getBox().width + fieldConfig.getWordSpace().getRight();

        if (fieldConfig.getPosition().equalsIgnoreCase(Position.LEFT.toString())) {
            rangeXEnd = keywordObj.getBox().x;
        }
        for (Map.Entry<Integer, EntWord> entry : wordMap.entrySet()) {
            EntWord wordChecking = entry.getValue();
            if (wordChecking.equals(keywordObj) || (keywordObj.getText().contains(wordChecking.getText()) && !trimSpecChar(wordChecking.getText()).isEmpty())) {
                if (keywordObj.getText().equals("Fax:")) {
                    System.out.println("com.saigonbpo.entity.ocr.EntLine.find()");
                }
                if (fieldConfig.getWordSpace().getLeft() <= 0 && fieldConfig.getWordSpace().getRight() <= 0) {
                    return returnDefautlSingleResult(fieldConfig, entry.getKey(), keywordObj);
                }
                continue;
            }
            // Detect match word
            int checkXBegin = wordChecking.getBox().x;
            int checkXEnd = checkXBegin + (wordChecking.getBox().width / 2);
            if ((checkXBegin >= rangeXBegin && checkXBegin <= rangeXEnd) || (checkXEnd >= rangeXBegin && checkXEnd <= rangeXEnd)) {
                result.add(wordChecking);
            }
            if (wordChecking.getBox().x > rangeXEnd) {
                break;
            }
        }
        return result;
    }

    //Return first word if <word_space> is empty or zero
    private List<EntWord> returnDefautlSingleResult(Field fieldConfig, Integer keywordIndex, EntWord keywordObj) {
        List<EntWord> result = new ArrayList<>();
        try {
            EntWord res = null;
            if (fieldConfig.getPosition().equalsIgnoreCase(Position.LEFT.toString())) {
                res = wordMap.get(keywordIndex - 1);
            } else if (fieldConfig.getPosition().equalsIgnoreCase(Position.RIGHT.toString())) {
                res = wordMap.get(keywordIndex + keywordObj.getCountWordMerged() + 1);
            }
            if (res != null) {
                result.add(res);
            }
        } catch (Exception e) {
        }
        return result;
    }

    @Override
    public List<List<EntWord>> finds(Field fieldInfo, EntWord entWord) throws Exception {
        return null;
    }

    @Override
    public List<List<EntWord>> find(Map<Integer, EntPage> pageMap, Field fieldConfig, EntWord keywordOjb)
            throws Exception {
        return null;
    }

    @Override
    public EntWord findByKeyword(Map<Integer, EntPage> pageMap, Field fieldConfig, String keyword) throws Exception {
        return null;
    }
}
