package com.neurobehavior.drift.automation.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelUtility {
    private static final String FILE_PATH = "reports/TestCases.xlsx";
    private static final String[] HEADERS = {
        "Test ID", "Module", "Feature", "Scenario", "Expected Result",
        "Actual Result", "Status", "Retry Count", "Execution Time",
        "Device", "Android Version", "Screenshot", "Log File",
        "Failure Reason", "Execution Date"
    };

    public static synchronized void logTestResult(
            String testId, String module, String feature, String scenario,
            String expected, String actual, String status, int retryCount,
            String execTime, String device, String osVersion,
            String screenshotPath, String logPath, String failureReason, String date) {

        File file = new File(FILE_PATH);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        Workbook workbook;
        Sheet sheet;

        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                workbook = new XSSFWorkbook(fis);
                sheet = workbook.getSheet("Test Execution");
                if (sheet == null) {
                    sheet = workbook.createSheet("Test Execution");
                    createHeaders(workbook, sheet);
                }
            } catch (IOException e) {
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("Test Execution");
                createHeaders(workbook, sheet);
            }
        } else {
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Test Execution");
            createHeaders(workbook, sheet);
        }

        // Find if test case already exists to update it, or find first empty row
        int rowCount = sheet.getLastRowNum();
        int targetRowIdx = -1;
        
        for (int i = 1; i <= rowCount; i++) {
            Row r = sheet.getRow(i);
            if (r != null) {
                Cell cell = r.getCell(0);
                if (cell != null && cell.getStringCellValue().equals(testId)) {
                    targetRowIdx = i;
                    break;
                }
            }
        }

        if (targetRowIdx == -1) {
            targetRowIdx = rowCount + 1;
        }

        Row row = sheet.getRow(targetRowIdx);
        if (row == null) {
            row = sheet.createRow(targetRowIdx);
        }

        row.createCell(0).setCellValue(testId);
        row.createCell(1).setCellValue(module);
        row.createCell(2).setCellValue(feature);
        row.createCell(3).setCellValue(scenario);
        row.createCell(4).setCellValue(expected);
        row.createCell(5).setCellValue(actual);
        
        // Format status cell
        Cell statusCell = row.createCell(6);
        statusCell.setCellValue(status);
        applyStatusStyle(workbook, statusCell, status);

        row.createCell(7).setCellValue(retryCount);
        row.createCell(8).setCellValue(execTime);
        row.createCell(9).setCellValue(device);
        row.createCell(10).setCellValue(osVersion);
        row.createCell(11).setCellValue(screenshotPath);
        row.createCell(12).setCellValue(logPath);
        row.createCell(13).setCellValue(failureReason);
        row.createCell(14).setCellValue(date);

        // Auto-fit column widths
        for (int col = 0; col < HEADERS.length; col++) {
            sheet.autoSizeColumn(col);
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void createHeaders(Workbook workbook, Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        headerStyle.setFont(font);
        
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private static void applyStatusStyle(Workbook workbook, Cell cell, String status) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        
        if ("PASS".equalsIgnoreCase(status)) {
            style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            font.setColor(IndexedColors.DARK_GREEN.getIndex());
        } else if ("FAIL".equalsIgnoreCase(status)) {
            style.setFillForegroundColor(IndexedColors.RED.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            font.setColor(IndexedColors.DARK_RED.getIndex());
        } else {
            style.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            font.setColor(IndexedColors.DARK_BLUE.getIndex());
        }
        
        style.setFont(font);
        cell.setCellStyle(style);
    }
}
