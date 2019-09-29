package com.saigonbpo.extractingbot_ocr;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import com.saigonbpo.entity.config.Config;
import com.saigonbpo.entity.ocr.EntPage;
import com.saigonbpo.entity.result.Result;
import com.saigonbpo.extractions.DataExtraction;
import com.saigonbpo.extractions.TesseractConversion;

public class AppMulti {

    public static void main(String[] args) throws Exception {
        try {
            ClassLoader cl = DataExtraction.class.getClassLoader();
            //Keyword
            String type = "doc";
            type = "inv";
            for (int i = 1; i <= 18; i++) {
                String HOCR_DOC = cl.getResource("hocr_" + type + "_" + i + ".txt").getFile();
                //HOCR_DOC = HOCR_DOC+","+HOCR_DOC;
                HOCR_DOC = HOCR_DOC;
                String RULE_CFG = cl.getResource("rule_cfg_" + type + "_" + i + ".xml").getFile();
                String RESULT = Paths.get((new File(RULE_CFG)).getParent(), "result_" + type + "_" + i + ".xml").toString();
                System.out.println("-------------- " + HOCR_DOC + " ------------");
                System.out.println("-------------- " + RULE_CFG + " ------------");
                System.out.println("-------------- " + RESULT + " ------------");
                // Load OCR Config (*.xml)
                JAXBContext jaxbContext = JAXBContext.newInstance(Config.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                Config config = (Config) jaxbUnmarshaller.unmarshal(new File(RULE_CFG));
                // Load Hocr (*.txt)
                TesseractConversion tex = new TesseractConversion();
                Map<Integer, EntPage> pageMap = tex.unmarshalHocrToEntity(HOCR_DOC.split(","));
                //System.out.println(pageMap.get(1).getTextOriginal());
                // Extract & Mapping data
                Result result = (new DataExtraction()).extract(config, pageMap);

                // TESTING (Parse result object to text file)
                JAXBContext context = JAXBContext.newInstance(Result.class);
                Marshaller m = context.createMarshaller();
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                // Write to System.out
                m.marshal(result, System.out);
                // Write to File
                m.marshal(result, new File(RESULT));
                System.out.println("-------------- Done ------------");
            }

            //Amount
//            for (int i = 17; i <= 17; i++) {
//                String RULE_CFG = cl.getResource("rule_cfg_inv_" + i + ".xml").getFile();
//                String HOCR_DOC = cl.getResource("hocr_inv_" + i + ".txt").getFile();
//                String RESULT = Paths.get((new File(RULE_CFG)).getParent(), "result_inv_" + i + ".xml").toString();
//                System.out.println("-------------- " + HOCR_DOC + " ------------");
//                System.out.println("-------------- " + RULE_CFG + " ------------");
//                System.out.println("-------------- " + RESULT + " ------------");
//                // Load OCR Config (*.xml)
//                JAXBContext jaxbContext = JAXBContext.newInstance(Config.class);
//                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//                Config config = (Config) jaxbUnmarshaller.unmarshal(new File(RULE_CFG));
//                // Load Hocr (*.txt)
//                TesseractConversion tex = new TesseractConversion();
//                Map<Integer, EntPage> pageMap = tex.unmarshalHocrToEntity(HOCR_DOC.split(","));
//
//                // Extract & Mapping data
//                DataExtraction data = new DataExtraction();
//                Result result = data.extract(config, pageMap);
//
//                // TESTING (Parse result object to text file)
//                JAXBContext context = JAXBContext.newInstance(Result.class);
//                Marshaller m = context.createMarshaller();
//                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//                // Write to System.out
//                m.marshal(result, System.out);
//                // Write to File
//                m.marshal(result, new File(RESULT));
//                System.out.println("-------------- Done ------------");
//                System.out.println("----------------------------------------------------------------");
//            }
        } catch (Exception e) {
            throw e;
        }
    }
}
