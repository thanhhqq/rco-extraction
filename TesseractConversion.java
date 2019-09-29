package com.saigonbpo.extractions;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import com.saigonbpo.entity.ocr.EntLine;
import com.saigonbpo.entity.ocr.EntPage;
import com.saigonbpo.entity.ocr.EntWord;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.NavigableMap;
import java.util.TreeMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
public class TesseractConversion {
	private static final Pattern pRect = Pattern.compile("[0-9]+\\s[0-9]+\\s[0-9]+\\s[0-9]+");
	
        private Rectangle extractBox(String str) {
		Matcher m = pRect.matcher(str);
		m.find();
		String[] box = m.group(0).split(StringUtils.SPACE);
		return new Rectangle(Integer.parseInt(box[0]), Integer.parseInt(box[1]),
				Integer.parseInt(box[2]) - Integer.parseInt(box[0]),
				Integer.parseInt(box[3]) - Integer.parseInt(box[1]));
	}
	public Map<Integer, EntPage> unmarshalHocrToEntity(String[] hocrPathnames) throws IOException {
		Map<Integer, EntPage> pageMap = new HashMap<>();
                int count = 0;
		for (int pageNr = 0; pageNr < hocrPathnames.length; pageNr++) {
			Map<Integer, EntPage> firstMapPage = unmarshalHocrToEntity(hocrPathnames[pageNr].trim(),pageNr+count);
                        count = firstMapPage.size()-1;
			pageMap.putAll(firstMapPage);
		}
		return pageMap;
	}
	public Map<Integer, EntPage> unmarshalHocrToEntity(String hocrPathname) throws IOException {
		return unmarshalHocrToEntity(hocrPathname, 0);
	}
	private Map<Integer, EntPage> unmarshalHocrToEntity(String hocrPathname, int pageDynamic) throws IOException {
		File file = new File(hocrPathname);
		String htmlContent = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
		return unmarshalHocrContentToEntity(htmlContent, pageDynamic);
	}
	public Map<Integer, EntPage> unmarshalHocrContentToEntity(String htmlContent) throws IOException {
		return unmarshalHocrContentToEntity(htmlContent,0);
	}
	private Map<Integer, EntPage> unmarshalHocrContentToEntity(String htmlContent, int pageDynamic) throws IOException {
            Document doc = Jsoup.parse(htmlContent);
		Elements pageOCR = doc.getElementsByClass("ocr_page");
		StringBuilder pageValue = new StringBuilder();
		Map<Integer, EntPage> pageMap = new HashMap<>();
		for (int p = 0; p < pageOCR.size(); p++) {
			EntPage pageEnt = new EntPage();
                        int pageNr = p + 1 + pageDynamic;
			Elements lineOCR = pageOCR.get(p).getElementsByClass("ocr_line");
			NavigableMap<Integer,EntLine> lineMap = new TreeMap<>();
			for (int l = 0; l < lineOCR.size(); l++) {
				EntLine lineEnt = new EntLine();
				StringBuilder lineValue = new StringBuilder();
				Map<Integer, EntWord> wordMap = new HashMap<>();
				Elements wordOCR = lineOCR.get(l).getElementsByClass("ocrx_word");
				for (int w = 0; w < wordOCR.size(); w++) {
					EntWord wordEnt = new EntWord();
					String wordValue = wordOCR.get(w).text();
                                       
					wordEnt.setBox(extractBox(wordOCR.get(w).attr("title")));
					wordEnt.setLineNr(l + 1);
					wordEnt.setPageNr(pageNr);
					wordEnt.setText(wordValue);
					wordEnt.setWordNr(w + 1);
					wordMap.put(w + 1, wordEnt);
					lineValue.append((w>0?StringUtils.SPACE:StringUtils.EMPTY).concat(wordValue));
				}
				lineEnt.setBox(extractBox(lineOCR.get(l).attr("title")));
				lineEnt.setPageNr(pageNr);
				lineEnt.setWordMap(wordMap);
				lineEnt.setTextOriginal(lineValue.toString());
				lineMap.put(l + 1, lineEnt);
				pageValue.append((l>0?StringUtils.LF:StringUtils.EMPTY).concat(lineValue.toString()));
			}
			pageEnt.setBox(extractBox(pageOCR.get(p).attr("title")));
			pageEnt.setPageNr(pageNr);
			pageEnt.setLineMap(lineMap);
			pageEnt.setTextOriginal(pageValue.toString());
			pageMap.put(pageNr, pageEnt);
		}
		return pageMap;
	}

}
