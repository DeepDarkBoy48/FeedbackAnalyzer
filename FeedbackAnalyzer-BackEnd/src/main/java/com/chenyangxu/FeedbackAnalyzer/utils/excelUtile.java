package com.chenyangxu.FeedbackAnalyzer.utils;


import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public class excelUtile {
    public static String excel(MultipartFile multipartFile){
        try {
            InputStream inputStream = multipartFile.getInputStream();
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = excel.getSheetAt(0);
            //获取最后一行的行号
            int lastRowNum = sheet.getLastRowNum();
            //获取最后一列的列号
            int lastCellNum = sheet.getRow(0).getLastCellNum();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
