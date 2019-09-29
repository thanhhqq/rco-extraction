package com.saigonbpo.entity.ocr;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.saigonbpo.entity.config.Field;
import com.saigonbpo.entity.config.Position;
import com.saigonbpo.extractions.DataExtractionAbs;
import java.util.Comparator;
import java.util.HashMap;
import java.util.NavigableMap;
import java.util.TreeMap;

public class EntPage extends DataExtractionAbs {

    private String textOriginal;
    private Rectangle box = new Rectangle(0, 0, 0, 0);
    private int pageNr;
    //private Map<Integer, EntLine> lineMap = new HashMap<>();
    private NavigableMap<Integer, EntLine> lineMap = new TreeMap<>();

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

    public NavigableMap<Integer, EntLine> getLineMap() {
        return lineMap;
    }

    public void setLineMap(NavigableMap<Integer, EntLine> lineMap) {
        this.lineMap = lineMap;
    }

    @Override
    public EntWord findbyKeyword(String keywordStr) throws Exception {
        return findbyKeyword(keywordStr, new Rectangle());
    }

    @Override
    public EntWord findbyKeyword(String keywordStr, Rectangle rectWithout) throws Exception {
        return findbyKeyword(keywordStr, rectWithout, false);
    }

    @Override
    public EntWord findbyKeyword(String keywordStr, Rectangle rectWithout, boolean isForAmount) throws Exception {
        Map<Integer, EntLine> lineMapSorted = new HashMap<>(lineMap);
        if (isForAmount) {
            lineMapSorted = new TreeMap<>((Comparator<Integer>) (o1, o2) -> o2.compareTo(o1));
            lineMapSorted.putAll(lineMap);
        }
        for (Map.Entry<Integer, EntLine> entry : lineMapSorted.entrySet()) {
            EntWord entWord = entry.getValue().findbyKeyword(keywordStr, rectWithout);
            if (entWord != null) {
                return entWord;
            }
        }
        return null;
    }

    private boolean isMaxLineSpaceValid(Field fieldConfig, EntLine lineObjOld, EntLine lineObjCur) {
        if (lineObjOld == null || fieldConfig.getMaxLine().getMaxLineSpace() <= 0) {
            return true;
        }
        if (fieldConfig.getPosition().equalsIgnoreCase(Position.TOP.toString())) {
            return (lineObjOld.getBox().y + fieldConfig.getMaxLine().getMaxLineSpace())
                    <= (lineObjCur.getBox().y + lineObjCur.getBox().height);
        } else if (fieldConfig.getPosition().equalsIgnoreCase(Position.BOTTOM.toString())) {
            return (lineObjOld.getBox().y + lineObjOld.getBox().height + fieldConfig.getMaxLine().getMaxLineSpace())
                    >= (lineObjCur.getBox().y);
        }
        return true;
    }

    private int[] computeLineStartEnd(Field fieldConfig, EntWord keywordObj) {
        int[] results = new int[2];
        int startLine = keywordObj.getLineNr();
        int endLine = keywordObj.getLineNr();

        if (fieldConfig.getPosition().equalsIgnoreCase(Position.TOP.toString())) {
            startLine = keywordObj.getLineNr() - fieldConfig.getMaxLine().getValue() + Position.TOP.getValue();
            endLine = keywordObj.getLineNr() + Position.TOP.getValue();
        } else if (fieldConfig.getPosition().equalsIgnoreCase(Position.BOTTOM.toString())) {
            startLine = keywordObj.getLineNr() + Position.BOTTOM.getValue();
            endLine = keywordObj.getLineNr() + fieldConfig.getMaxLine().getValue();
        }
        results[0] = startLine;
        results[1] = endLine;
        return results;
    }

    @Override
    public List<List<EntWord>> finds(Field fieldConfig, EntWord keywordObj) throws Exception {
        List<List<EntWord>> result = new ArrayList<>();
        int[] resCompute = computeLineStartEnd(fieldConfig, keywordObj);
        int startLine = resCompute[0];
        int endLine = resCompute[1];

        EntLine lineObjOld = null;
        for (int lineKey = Math.max(startLine, 1); lineKey <= Math.min(endLine, lineMap.lastEntry().getKey()); lineKey++) {
            try {
                EntLine lineObjCur = lineMap.get(lineKey);
                if (!isMaxLineSpaceValid(fieldConfig, lineObjOld, lineObjCur)) {
                    break;
                }
                List<EntWord> listEntWord = lineObjCur.find(fieldConfig, keywordObj);
                if (listEntWord.size() <= 0) {
                    continue;
                }
                lineObjOld = lineObjCur;
                result.add(listEntWord);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public List<EntWord> find(Field fieldInfo, EntWord entWord) throws Exception {
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
