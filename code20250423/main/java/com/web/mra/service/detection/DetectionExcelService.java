package com.web.mra.service.detection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.mra.dto.DetectionExcelMetaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class DetectionExcelService {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final DetectionFileService detectionFileService;

    public Object detectExcel(DetectionExcelMetaDTO excelMeta) {
        try {
            validateExcelMeta(excelMeta);
            return processExcelFile(excelMeta);
        } catch (Exception e) {
            String errorMsg = String.format("Excel detection failed for ID: %d", excelMeta.getDetectionComponentId());
            log.error(errorMsg, e);
            throw new DetectionServiceException(errorMsg, e);
        }
    }

    private void validateExcelMeta(DetectionExcelMetaDTO excelMeta) {
        if (excelMeta.getExcelFileName() == null || excelMeta.getExcelFileName().isEmpty()) {
            throw new IllegalArgumentException("Excel file name cannot be null or empty");
        }
        if (excelMeta.getSheetName() == null || excelMeta.getSheetName().isEmpty()) {
            throw new IllegalArgumentException("Sheet name cannot be null or empty");
        }
    }

    private String processExcelFile(DetectionExcelMetaDTO excelMeta) throws Exception {
        String filePath = detectionFileService.processFile(excelMeta.getExcelFileName(),
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        if (filePath.isEmpty()) {
            return "";
        }
        return parseExcel(filePath, excelMeta.getSheetName(), excelMeta.getCellPositions(), excelMeta.getCellParams());
    }

    private String parseExcel(String filePath, String sheetNamePattern, String cellPositions, String cellParams)
            throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        
        File excelFile = new File(filePath);
        try (FileInputStream fis = new FileInputStream(excelFile); Workbook workbook = new XSSFWorkbook(fis)) {

            // Split sheet pattern to handle multiple sheets
            String[] sheetPatterns = sheetNamePattern.split("\\|");
            int sheetSequence = 1;

            // Process each matching sheet
            for (String pattern : sheetPatterns) {
                Sheet matchingSheet = null;
                log.info("Match file {} pattern: {}", filePath, pattern);
                // Find matching sheet for current pattern
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    String sheetName = workbook.getSheetName(i);
                    if (Pattern.matches(pattern.trim(), sheetName)) {
                        matchingSheet = workbook.getSheetAt(i);
                        log.info(" Match file {} sheet matched: {}", filePath, sheetName);
                        break;
                    }
                }

                if (matchingSheet == null) {
                    log.error("No sheet in Excel matched by name pattern {}", pattern);
                    return "";
                }
                List<Map<String, Object>> sheetResult = new ArrayList<>();
                Map<String, Object> aggregationResult = new HashMap<>();

                // Process cell positions for aggregations
                if (cellPositions != null && !cellPositions.isEmpty()) {
                    Map<String, Map<String, String>> positionsMap = objectMapper.readValue(cellPositions, Map.class);
                    if (positionsMap.containsKey(String.valueOf(sheetSequence))) {
                        processAggregations(matchingSheet, positionsMap.get(String.valueOf(sheetSequence)),
                                aggregationResult);
                        sheetResult.add(aggregationResult);
                    }
                }

                // Process cell parameters for key-value pairs
                if (cellParams != null && !cellParams.isEmpty()) {
                    Map<String, Map<String, String>> paramsMap = objectMapper.readValue(cellParams, Map.class);
                    if (paramsMap.containsKey(String.valueOf(sheetSequence))) {
                        processKeyValuePairs(matchingSheet, paramsMap.get(String.valueOf(sheetSequence)), sheetResult);
                    }
                }

                result.addAll(sheetResult);
                sheetSequence++;
            }

            if (result.isEmpty()) {
                return "[]";
            }
        } catch (Exception e) {
            return "";
        }

        String resultString = objectMapper.writeValueAsString(result);
        log.info(" parsel excel, result: " + resultString);
        return resultString;
    }

    private void processAggregations(Sheet sheet, Map<String, String> positionsMap, Map<String, Object> result) {
        positionsMap.forEach((key, range) -> {
            String[] cells = range.split(":");
            CellRangeAddress rangeCells = CellRangeAddress.valueOf(range);

            double aggregateValue = 0;
            int count = 0;

            for (int row = rangeCells.getFirstRow(); row <= rangeCells.getLastRow(); row++) {
                for (int col = rangeCells.getFirstColumn(); col <= rangeCells.getLastColumn(); col++) {
                    Cell cell = sheet.getRow(row).getCell(col);
                    if (cell != null) {
                        double value = getCellNumericValue(cell);
                        aggregateValue += value;
                        count++;
                    }
                }
            }

            if (key.startsWith("sum-")) {
                result.put(key, aggregateValue);
            } else if (key.startsWith("avg-")) {
                result.put(key, count > 0 ? aggregateValue / count : 0);
            } else if (key.startsWith("cnt-")) {
                result.put(key, Integer.valueOf(count));
            }
        });
    }

    private void processKeyValuePairs(Sheet sheet, Map<String, String> paramsMap, List<Map<String, Object>> result) {
        if (paramsMap.containsKey("table_range") ) {
            processTableRange(sheet, paramsMap, result);
        } else {
            processNormalRange(sheet, paramsMap, result);
        }
    }
    
    private void processTableRange(Sheet sheet, Map<String, String> paramsMap, List<Map<String, Object>> result) {
        String tableRange = paramsMap.get("table_range");
        String valueFilter = paramsMap.get("value_filter");
        
        log.info(" Enter table data parser, table range: {} valueFileter {}", tableRange, valueFilter);
        
        int filterColIndex = -1;
        if (valueFilter != null && !valueFilter.isEmpty()) {
            try {
                filterColIndex = Integer.valueOf(valueFilter);
            } catch (Exception e) {
                filterColIndex = -1;
            }
        }
        log.info(" filterColIndex {}  ", filterColIndex);
        
        CellRangeAddress rangeCells = CellRangeAddress.valueOf(tableRange);
        int firstRow = rangeCells.getFirstRow();
        int lastRow = rangeCells.getLastRow();
        int firstCol = rangeCells.getFirstColumn();
        int lastCol = rangeCells.getLastColumn();
    
        Row headerRow = sheet.getRow(firstRow);
        if (headerRow == null) {
            return;
        }
        
        List<String> headers = new ArrayList<>();
        for (int col = firstCol; col <= lastCol; col++) {
            Cell cell = headerRow.getCell(col);
            Object value = processCellValue(cell, null);
            headers.add(cell != null && !cell.toString().trim().isEmpty() ? value.toString().trim() : "Column_" + (col + 1));
        }
        
        
        
        int emptyRowCount = 0;
        for (int rowNum = firstRow + 1; rowNum <= lastRow; rowNum++) {
            if (emptyRowCount == 3) {
                break;
            }
            Row currentRow = sheet.getRow(rowNum);
            if (currentRow == null) {
                emptyRowCount++;
                continue;
            }
                    
            boolean isEmptyRow = true;
            for (int col = firstCol; col <= lastCol; col++) {
                Cell cell = currentRow.getCell(col);
                if (cell != null && !cell.toString().trim().isEmpty()) {
                    isEmptyRow = false;
                    break;
                }
            }
                
            if (isEmptyRow) {
                emptyRowCount++;
                continue;
            }
            
            emptyRowCount = 0;
            
            if (filterColIndex >= 0) {
                Cell filterCell = currentRow.getCell(firstCol + filterColIndex);
                if (filterCell == null || filterCell.toString().trim().isEmpty()) {
                    continue;
                }
            }
            
            Map<String, Object> rowData = new LinkedHashMap<>();
            for (int col = firstCol; col <= lastCol; col++) {
                Cell cell = currentRow.getCell(col);
                Object value = processCellValue(cell, null);
                rowData.put(headers.get(col - firstCol), value instanceof String ? ((String) value).trim() : value);
            }
            result.add(rowData);
        }
    }

    private void processNormalRange(Sheet sheet, Map<String, String> paramsMap, List<Map<String, Object>> result) {
        String keyRange = paramsMap.get("key_range");
        String valueRange = paramsMap.get("value_range");
        String keyFilter = paramsMap.get("key_filter");
        String valueFilter = paramsMap.get("value_filter");

        List<Object> keys = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        // Process key ranges
        if (keyRange != null && !keyRange.isEmpty()) {
            keys = processRanges(sheet, keyRange, keyFilter);
        }

        // Process value ranges
        if (valueRange != null && !valueRange.isEmpty()) {
            values = processRanges(sheet, valueRange, valueFilter);
        }

        // Create single map with key-value pairs
        Map<String, Object> resultMap = new LinkedHashMap<>();
        for (int i = 0; i < values.size(); i++) {
            String key = i < keys.size() ? keys.get(i).toString() : "key_" + (i + 1);
            resultMap.put(key, values.get(i));
        }
        result.add(resultMap);
    }

    private List<Object> processRanges(Sheet sheet, String rangeStr, String filter) {
        List<Object> results = new ArrayList<>();
        String[] ranges = rangeStr.split(",");

        for (String range : ranges) {
            if (range.contains(":")) {
                // Process range (e.g., A1:B2 or A1:B0 for dynamic range)
                CellRangeAddress rangeCells = CellRangeAddress.valueOf(range);
                int lastRow = rangeCells.getLastRow();

                // Check if this is a dynamic range (ends with 0)
                if (lastRow == 0) {
                    lastRow = sheet.getLastRowNum();
                }

                for (int row = rangeCells.getFirstRow(); row <= lastRow; row++) {
                    Row currentRow = sheet.getRow(row);
                    if (currentRow != null) {
                        for (int col = rangeCells.getFirstColumn(); col <= rangeCells.getLastColumn(); col++) {
                            Object value = processCellValue(currentRow.getCell(col), filter);
                            results.add(value);
                        }
                    }
                }
            } else {
                // Process single cell (e.g., A1)
                CellReference cellRef = new CellReference(range);
                Row row = sheet.getRow(cellRef.getRow());
                if (row != null) {
                    Cell cell = row.getCell(cellRef.getCol());
                    Object value = processCellValue(cell, filter);
                    results.add(value);
                }
            }
        }

        return results;
    }

    private Object getDateOrNumeric(Cell cell) {
        if (DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
            return sdf.format(date);
        }
        return cell.getNumericCellValue();
    }
    private Object processCellValue(Cell cell, String filter) {
        if (cell == null) {
            return "";
        }

        Object value;
        switch (cell.getCellType()) {
        case NUMERIC:
            value =getDateOrNumeric(cell);
            break;
        case STRING:
            value = cell.getStringCellValue().toString().trim();
            break;
        case BOOLEAN:
            value = cell.getBooleanCellValue();
            break;
        case FORMULA:
            try {
                // Try to get numeric value first
                value =getDateOrNumeric(cell);
            } catch (Exception e) {
                try {
                    // If not numeric, try parsing string value
                    value = Double.parseDouble(cell.getStringCellValue());
                } catch (Exception ex) {
                    value = 0;
                }
            }
            break;
        default:
            value = "";
        }

        if (filter != null && filter.equals("number only")) {
            return (value instanceof Number) ? value : null;
        }
        return value;
    }

    private double getCellNumericValue(Cell cell) {
        if (cell == null)
            return 0;

        switch (cell.getCellType()) {
        case NUMERIC:
            return cell.getNumericCellValue();
        case STRING:
            try {
                return Double.parseDouble(cell.getStringCellValue());
            } catch (NumberFormatException e) {
                return 0;
            }
        case BOOLEAN:
            return cell.getBooleanCellValue() ? 1 : 0;
        case FORMULA:
            try {
                // Try to get numeric value first
                return cell.getNumericCellValue();
            } catch (Exception e) {
                try {
                    // If not numeric, try parsing string value
                    return Double.parseDouble(cell.getStringCellValue());
                } catch (Exception ex) {
                    return 0;
                }
            }
        default:
            return 0;
        }
    }
}
