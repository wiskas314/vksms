package org.example;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;


import java.io.FileInputStream;
import java.util.*;

public class ExcelReader {

    public List<GroupEntry> readSchedule(String filePath) throws Exception{
        List<GroupEntry> groups=new ArrayList<>();
        Set<String> prcessed = new HashSet<>();

        try(Workbook wb= new HSSFWorkbook(new FileInputStream(filePath))){
            Sheet sheet =wb.getSheetAt(0);
            Row header = sheet.getRow(0);

            int groupCol = -1, timeCol=-1, audCol=-1;
            for(Cell cell : header){
                String h = cell.getStringCellValue().toLowerCase(Locale.ROOT);
                if (h.contains("группа")) groupCol= cell.getColumnIndex();
                else if (h.contains("время")) timeCol = cell.getColumnIndex();
                else if (h.contains("ауд")) audCol=cell.getColumnIndex();
            }
            if (groupCol == -1 || timeCol == -1 || audCol == -1) {
                throw new RuntimeException("Не найдены нужные колонки в Excel");
            }

            for (int i =1; i<=sheet.getLastRowNum(); i++){
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String group = getCellValue(row.getCell(groupCol)).trim();
                if(group.isEmpty()||prcessed.contains(group)) continue;

                String rawTime = getCellValue(row.getCell(timeCol)).trim();
                String time = rawTime.substring(0,2)+":"+rawTime.substring(2);
                String audience =getCellValue(row.getCell(audCol)).trim();

                groups.add(new GroupEntry(group,time,audience,0L));
                prcessed.add(group);
            }
        }
        return groups;
    }
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
}}
