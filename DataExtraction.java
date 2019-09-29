package com.saigonbpo.extractions;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import org.apache.commons.lang3.StringUtils;
import com.saigonbpo.entity.config.AmountField;
import com.saigonbpo.entity.config.Config;
import com.saigonbpo.entity.config.Field;
import com.saigonbpo.entity.ocr.EntLine;
import com.saigonbpo.entity.ocr.EntPage;
import com.saigonbpo.entity.ocr.EntWord;
import com.saigonbpo.entity.result.Result;
import com.saigonbpo.entity.result.ResultField;
import com.saigonbpo.entity.result.Value;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class DataExtraction extends DataExtractionAbs {

    public Result extract(Config config, Map<Integer, EntPage> pageMap) throws Exception {
        Result resFinal = new Result();
        List<ResultField> listResultField = new ArrayList<>();
        // ---- Processing Amount ----
        //Clone Page Map
        try {
            String jsonPageMapOrg = (new Gson()).toJson(pageMap);
            List<ResultField> listResultAmount = extractAmount(config, pageMap);
            // ---- Processing Keyword ----
            // Parse Field Config To Object
            List<Field> listField = config.getListField();
            for (Field fieldConfig : listField) {
                //Using Clone Page Map
                Map<Integer, EntPage> mapCopy = (new Gson()).fromJson(jsonPageMapOrg, new TypeToken<Map<Integer, EntPage>>() {
                }.getType());
                Map<Integer, EntPage> pageMapOptimize = optimizePageMapByListBoundary(fieldConfig.getBoundaryVals(), mapCopy);
                // For Loop Keyword String
                List<String> listKeyword = fieldConfig.getKeywordVal();
                List<Value> listValue = new ArrayList<>();
                for (String keyword : listKeyword) {
                    // Find Word Entity By Keyword String
                    EntWord keywordObj = findByKeyword(pageMapOptimize, fieldConfig, keyword);
                    if (keywordObj != null) {
                        // Find out [List Word] In [List Line]
                        List<List<EntWord>> resList = find(pageMapOptimize, fieldConfig, keywordObj);
                        listValue.addAll(parseEntWord(resList, keywordObj, fieldConfig));
                    }
                }
                //Continue use matching in fiel property
                if (listValue.size() <= 0 && !fieldConfig.getMatching().trim().isEmpty()) {
                    listValue.addAll(findWithMatching(pageMapOptimize, fieldConfig));
                }

                ResultField f = new ResultField();
                f.setName(fieldConfig.getName());
                f.setListValue(listValue);
                listResultField.add(f);
            }
            listResultField.addAll(listResultAmount);
            resFinal.setListField(listResultField);

        } catch (Exception e) {
            throw e;
        }
        return resFinal;
    }

    private List<ResultField> extractAmount(Config config, Map<Integer, EntPage> pageMap) throws Exception {
        List<ResultField> listResField = new ArrayList<>();
        if (config.getListAmtField() == null) {
            return listResField;
        }
        if (config.getListAmtField().size() <= 0) {
            return listResField;
        }
        // Extract all number from OCR

        List<List<EntWord>> listFieldNetAmt = new ArrayList<>();
        List<List<EntWord>> listFieldVatRate = new ArrayList<>();
        List<List<EntWord>> listFieldShipRate = new ArrayList<>();
        List<List<EntWord>> listFieldVatAmt = new ArrayList<>();
        List<List<EntWord>> listFieldTotalAmt = new ArrayList<>();
        for (Map.Entry<Integer, EntPage> entry : pageMap.entrySet()) {
            Object[] resObjs = findAmountWithMatching(entry.getValue());
            //Create Temp Data
            Map<String, EntWord> mapCost = (Map<String, EntWord>) resObjs[0];
            List<Double> cost = (List<Double>) resObjs[1];
            //Order by
            Collections.sort(cost);
            //Compute for (Max Num * netEnt)/100
            AmountField amountField = config.getListAmtField().get(0);
            String vatRate = StringUtils.EMPTY;
            String shippingRate = StringUtils.EMPTY;
            EntWord netEnt = null;
            EntWord vatAmountEnt = null;
            EntWord totalEnt = null;
            boolean isFindOut = false;

            Double[] vatRates = Arrays.stream(amountField.getValue().split(SEP_VAT_RATE))
                    .map(Double::valueOf)
                    .toArray(Double[]::new);
            Arrays.sort(vatRates);

            Double[] shippingRates = Arrays.stream(amountField.getShippingRate().split(SEP_VAT_RATE))
                    .map(Double::valueOf)
                    .toArray(Double[]::new);
            Arrays.sort(shippingRates);

            //START FIND AND EXTRACT AMOUNT
            for (int v = vatRates.length - 1; v >= 0; v--) {
                vatRate = vatRates[v].toString();
                for (int i = cost.size() - 1; i >= 0; i--) {
                    double net = cost.get(i);
                    netEnt = mapCost.get(decFormat.format(net));
                    Object[] watAmountResults = findVatAmount(mapCost, net, vatRate);
                    double vatAmount = (double) watAmountResults[0];
                    vatAmountEnt = (EntWord) watAmountResults[1];
                    for (int sr = shippingRates.length - 1; sr >= 0; sr--) {
                        shippingRate = shippingRates[sr].toString();
                        totalEnt = mapCost.get(decFormat.format(vatAmount + net + shippingRates[sr]));
                        if (vatAmountEnt != null && totalEnt != null) {
                            isFindOut = true;
                            break;
                        }
                    }
                    if (isFindOut) {
                        break;
                    }
                }
                if (isFindOut) {
                    break;
                }
            }
            if (!isFindOut) {
                vatRate = StringUtils.EMPTY;
                shippingRate = StringUtils.EMPTY;
                netEnt = null;
                vatAmountEnt = null;
                totalEnt = null;
            }

            if (netEnt != null) {
                netEnt.setText(parseCurrencyCommon(netEnt.getText()));
            }
            if (vatAmountEnt != null) {
                vatAmountEnt.setText(parseCurrencyCommon(vatAmountEnt.getText()));
            }
            if (totalEnt != null) {
                totalEnt.setText(parseCurrencyCommon(totalEnt.getText()));
            }

            List<EntWord> listValueNetAmt = new ArrayList<>();
            listValueNetAmt.add(netEnt);
            listFieldNetAmt.add(listValueNetAmt);

            List<EntWord> listValueVatRate = new ArrayList<>();
            listValueVatRate.add(new EntWord(entry.getValue().getPageNr(), vatRate));
            listFieldVatRate.add(listValueVatRate);

            List<EntWord> listValueShipRate = new ArrayList<>();
            listValueShipRate.add(new EntWord(entry.getValue().getPageNr(), shippingRate));
            listFieldShipRate.add(listValueShipRate);

            List<EntWord> listValueVatAmt = new ArrayList<>();
            listValueVatAmt.add(vatAmountEnt);
            listFieldVatAmt.add(listValueVatAmt);

            List<EntWord> listValueTotalAmt = new ArrayList<>();
            listValueTotalAmt.add(totalEnt);
            listFieldTotalAmt.add(listValueTotalAmt);
        }

        // Generate Net Field
        ResultField resFieldNet = createBasicResultField(NetAmount, listFieldNetAmt);
        // Generate VAT Rate Field
        ResultField resFieldVatRate = createBasicResultField(VatRate, listFieldVatRate);
        // Generate Shipping Rate Field
        ResultField resFieldShippingRate = createBasicResultField(ShippingRate, listFieldShipRate);
        // Generate VAT Amount Field
        ResultField resFieldVatAmount = createBasicResultField(VatAmount, listFieldVatAmt);
        // Generate Total Field
        ResultField resFieldTotal = createBasicResultField(TotalAmount, listFieldTotalAmt);
        // Final result
        listResField.add(resFieldNet);
        listResField.add(resFieldShippingRate);
        listResField.add(resFieldVatAmount);
        listResField.add(resFieldVatRate);
        listResField.add(resFieldTotal);

        return listResField;
    }

    private Object[] findVatAmount(Map<String, EntWord> mapCost, double net, String vatRate) {
        EntWord vatAmountEnt = null;
        double vatAmount = 0;
        int step = 1;
        do {
            switch (step) {
                case 1:
                    vatAmount = Double.parseDouble(decFormat.format(net * Double.parseDouble(vatRate) / 100));
                    vatAmountEnt = mapCost.get(decFormat.format(vatAmount));
                    break;
                case 2:
                    vatAmount = Math.round(Double.parseDouble(decFormat.format(net * Double.parseDouble(vatRate) / 100)));
                    vatAmountEnt = mapCost.get(decFormat.format(vatAmount));
                    break;
                case 3:
                    vatAmount = Double.parseDouble(decFormat.format(net * Double.parseDouble(vatRate) / 100)) - ERROR_RATE;
                    vatAmountEnt = mapCost.get(decFormat.format(vatAmount));
                    break;
                default:
                    step = -1;
                    break;
            }
            if (vatAmountEnt != null || step == -1) {
                break;
            }
            step++;

        } while (true);
        Object[] res = new Object[]{vatAmount, vatAmountEnt};
        return res;
    }

    private ResultField createBasicResultField(String fieldName, List<List<EntWord>> listFieldTotal) {
        ResultField resFieldTotal = new ResultField();
        resFieldTotal.setName(fieldName);
        resFieldTotal.setListValue(parseEntWord(listFieldTotal, new EntWord()));
        return resFieldTotal;
    }

    private List<Value> parseEntWord(List<List<EntWord>> result, EntWord keywordOjb, Field fieldConfig) {
        List<Value> listValue = new ArrayList<>();
        if (result.size() <= 0 && keywordOjb != null) {
            Value value = new Value();
            value.setPageNr(keywordOjb.getPageNr());
            value.setKwVal(keywordOjb.getText());
            value.setKwBox(keywordOjb.getBox().x + StringUtils.SPACE + keywordOjb.getBox().y + StringUtils.SPACE
                    + keywordOjb.getBox().width + StringUtils.SPACE + keywordOjb.getBox().height);
            value.setValue(executeScript(fieldConfig, value.getValue()));
            listValue.add(value);
        } else {
            result.stream().forEach((List<EntWord> listWordObj) -> {
                Value value = new Value();
                if (!(listWordObj.get(0) == null)) {
                    Rectangle rect = listWordObj.size() > 0 ? listWordObj.get(0).getBox() : null;
                    if (!(rect == null)) {
                        String sep = StringUtils.EMPTY;
                        for (EntWord wordObj : listWordObj) {
                            String valWordOcr = wordObj.getText();
                            value.setPageNr(wordObj.getPageNr());
                            value.setValue(Objects.toString(value.getValue(), StringUtils.EMPTY).concat(sep).concat(valWordOcr));
                            value.setValBox(Math.min(wordObj.getBox().x, rect.x) + StringUtils.SPACE
                                    + Math.min(wordObj.getBox().y, rect.y) + StringUtils.SPACE
                                    + (wordObj.getBox().x + wordObj.getBox().width - rect.x) + StringUtils.SPACE
                                    + Math.max(wordObj.getBox().height, rect.height));
                            sep = StringUtils.SPACE;
                        }       //Apply <Script> in Config
                        value.setValue(trimSpecChar(value.getValue()));

                        if (fieldConfig != null) {
                            value.setValue(executeScript(fieldConfig, value.getValue()));
                        }       //Apply <Pattern> in Config
                        if (fieldConfig != null) {
                            value.setValue(compilePattern(fieldConfig, value.getValue()));
                        }       //Apply <trim>,... of method eliminates in Config
                        if (fieldConfig != null) {
                            value.setValue(executeMethod(fieldConfig, value.getValue()));
                        }
                        value.setKwVal(keywordOjb.getText());
                        value.setKwBox(keywordOjb.getBox().x + StringUtils.SPACE + keywordOjb.getBox().y + StringUtils.SPACE
                                + keywordOjb.getBox().width + StringUtils.SPACE + keywordOjb.getBox().height);
                        if (!value.getValue().isEmpty()) {
                            listValue.add(value);
                        }
                    }
                }
            });
        }
        return listValue;
    }

    public List<Value> parseEntWord(List<List<EntWord>> result, EntWord keywordOjb) {
        return parseEntWord(result, keywordOjb, null);
    }

    @Override
    public List<List<EntWord>> find(Map<Integer, EntPage> pageMap, Field fieldConfig, EntWord keywordOjb)
            throws Exception {
        EntPage entPage = pageMap.get(keywordOjb.getPageNr());
        return entPage.finds(fieldConfig, keywordOjb);
    }

    @Override
    public EntWord findByKeyword(Map<Integer, EntPage> pageMap, Field fieldConfig, String keyword) throws Exception {
        if (keyword.trim().isEmpty()) {
            return null;
        }
        for (Map.Entry<Integer, EntPage> entry : pageMap.entrySet()) {
            EntWord entWord = entry.getValue().findbyKeyword(keyword);
            if (entWord != null) {
                return entWord;
            }
        }
        return null;
    }

    @Override
    public EntWord findbyKeyword(String keywordStr, Rectangle rectWithout) throws Exception {
        return null;
    }

    public Object[] findAmountWithMatching(EntPage pageObj) throws Exception {
        //System.err.println("----------------" + pageObj.getPageNr() + "-----------------");
        Matcher m = Pattern.compile(regexNumberStr1, Pattern.MULTILINE).matcher(pageObj.getTextOriginal());
        Map<String, EntWord> mapCost = new HashMap<>();
        List<Double> cost = new ArrayList<>();
        while (m.find()) {
            //System.err.println("Original cost: "+m.group());
            EntWord wordObj = pageObj.findbyKeyword(m.group(), new Rectangle(), true);
            if (wordObj == null) {
                continue;
            }
            String costStr = parseCurrencyCommon(wordObj.getText());

            if (!costStr.matches(regexNumberStr2)) {
                continue;
            }
            double costVal = Double.parseDouble(costStr);
            if (cost.contains(costVal)) {
                continue;
            }
            //System.err.println(decFormat.format(costVal));
            cost.add(costVal);
            mapCost.put(decFormat.format(costVal), wordObj);
        }
        Object[] results = new Object[2];
        results[0] = mapCost;
        results[1] = cost;
        return results;
    }

    public List<Value> findWithMatching(Map<Integer, EntPage> pageMapOptimize, Field fieldConfig) throws Exception {
        List<List<EntWord>> resList = new ArrayList<>();
        List<Value> listValue = new ArrayList<>();
        for (Map.Entry<Integer, EntPage> entry : pageMapOptimize.entrySet()) {
            String content = entry.getValue().getTextOriginal();
            Matcher m = Pattern.compile(fieldConfig.getMatching(), Pattern.MULTILINE).matcher(content);
            Rectangle rectWithout = new Rectangle();
            while (m.find()) {
                EntWord wordObj = entry.getValue().findbyKeyword(m.group(), rectWithout);
                if (wordObj == null) {
                    continue;
                }
                rectWithout.setBounds(wordObj.getBox());
                List<EntWord> res = new ArrayList<>();
                res.add(wordObj);
                resList.add(res);
            }
        }
        listValue.addAll(parseEntWord(resList, new EntWord(), fieldConfig));
        return listValue;
    }

    @Override
    public List<EntWord> find(Field fieldConfig, EntWord keywordObj) throws Exception {
        return null;
    }

    @Override
    public EntWord findbyKeyword(String keywordStr) throws Exception {
        return null;
    }

    @Override
    public List<List<EntWord>> finds(Field fieldConfig, EntWord keywordObj) throws Exception {
        return null;
    }

    private Map<Integer, EntPage> optimizePageMapByListBoundary(List<String> listBoundary,
            Map<Integer, EntPage> pageMap) {
        Map<Integer, EntPage> pageMapCurrent = new HashMap<>();
        for (String boundary : listBoundary) {
            String[] boundaryArr = boundary.split(SEP_COMMA);
            int pageNr = Integer.parseInt(boundaryArr[0].trim());
            pageNr = pageNr <= 0 ? 1 : pageNr;
            Rectangle rectBoundary = new Rectangle(Integer.parseInt(boundaryArr[1].trim()),
                    Integer.parseInt(boundaryArr[2].trim()), Integer.parseInt(boundaryArr[3].trim()),
                    Integer.parseInt(boundaryArr[4].trim()));
            // Check condition boundary
            EntPage pageCurrent = pageMap.get(pageNr);
            if (pageCurrent == null) {
                continue;
            }
            if (rectBoundary.x < 0 | rectBoundary.y < 0 | rectBoundary.width <= 0
                    | rectBoundary.height <= 0) {
                pageMapCurrent.put(pageNr, pageCurrent);
                continue;
            }

            Map<Integer, EntLine> lineMapCurrent = pageCurrent.getLineMap();
            for (Iterator<Map.Entry<Integer, EntLine>> itLine = lineMapCurrent.entrySet().iterator(); itLine
                    .hasNext();) {
                Map.Entry<Integer, EntLine> entryLine = itLine.next();
                Map<Integer, EntWord> wordMapCurrent = entryLine.getValue().getWordMap();
                boolean isLineValid = false;
                for (Iterator<Map.Entry<Integer, EntWord>> it = wordMapCurrent.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<Integer, EntWord> entryWord = it.next();
                    if (rectBoundary.contains(entryWord.getValue().getBox())) {
                        isLineValid = true;
                    } else {
                        it.remove();
                    }
                }
                if (!isLineValid) {
                    itLine.remove();
                }
            }
            pageMapCurrent.put(pageNr, pageCurrent);
        }
        return pageMapCurrent;
    }

    @Override
    public EntWord findbyKeyword(String keywordStr, Rectangle rectWithout, boolean isForAmount) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
