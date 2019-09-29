package com.saigonbpo.entity.ocr;

import java.awt.Rectangle;
import org.apache.commons.lang3.StringUtils;

public class EntWord {
    private int countWordMerged = 0;
    private String text = StringUtils.EMPTY;
    private Rectangle box = new Rectangle(0, 0, 0, 0);
    private int pageNr;
    private int wordNr;
    private int lineNr;
    public EntWord(){
    }
    public EntWord(int pageNr, String text){
        this.text = text;
        this.pageNr = pageNr;
    }
    
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public int getWordNr() {
        return wordNr;
    }

    public void setWordNr(int wordNr) {
        this.wordNr = wordNr;
    }

    public int getLineNr() {
        return lineNr;
    }

    public void setLineNr(int lineNr) {
        this.lineNr = lineNr;
    }

    public int getCountWordMerged() {
        return countWordMerged;
    }

    public void setCountWordMerged(int countWordMerged) {
        this.countWordMerged = countWordMerged;
    }

    @Override
    public String toString() {
        return "EntWord [value=" + text + ", box=" + box + ", pageNr=" + pageNr + ", wordNr=" + wordNr + ", lineNr="
                + lineNr + "]";
    }
}
