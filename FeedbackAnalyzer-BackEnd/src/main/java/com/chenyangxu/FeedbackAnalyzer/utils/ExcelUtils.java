package com.chenyangxu.FeedbackAnalyzer.utils;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class ExcelUtils {

    public static XSSFSheet ExcelHandller(MultipartFile file){
        XSSFWorkbook excel = null;
        try {
            excel = new XSSFWorkbook(file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        XSSFSheet sheet = excel.getSheetAt(0);
        return sheet;
    }
}
