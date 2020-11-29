package com.lgh.tool.excel.imports;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * @ClassName ImportExcel
 * @Description:自定义表格模板导入
 * @Author lgh
 * @Date 2020/11/3 14:53
 **/
public class ImportExcel {
    //根据03还是07版本的Excel创建不同wb对象
    public static Workbook getWorkbook(InputStream inStr, String filePath) throws Exception {
        Workbook wb = null;
        String fileType = filePath.substring(filePath.lastIndexOf("."));
        System.out.println(fileType);
        if (".xls".equals(fileType)) {
            wb = new HSSFWorkbook(inStr); // 2003-
        } else if (".xlsx".equals(fileType)) {
            wb = new XSSFWorkbook(inStr); // 2007+
        } else {
            throw new Exception("解析的文件格式有误！");
        }
        return wb;

    }
    // 读取多个sheet方法
    public static Map<String, List<List<String>>> moreSheetImport(InputStream is,String filePath) throws Exception {
        Map<String, List<List<String>>> tempMap = new HashMap<>();
        Workbook wb = getWorkbook(is, filePath);
        int sheetNum = wb.getNumberOfSheets();
        for (int j = 0; j < sheetNum; j++) {
            List<List<String>> lists = new ArrayList<List<String>>();
            Sheet sheet = wb.getSheetAt(j);
            int rowstart = sheet.getFirstRowNum();
            int rowEnd = sheet.getLastRowNum();
            String sheetName=sheet.getSheetName();
            for (int i = rowstart; i <= rowEnd; i++) {// 从第一行表头开始读
                Row xssRow = sheet.getRow(i);
                if (null == xssRow)
                    continue;
                int cellStart = xssRow.getFirstCellNum();
                if (cellStart > -1) {
                    int cellEnd = xssRow.getLastCellNum() - 1;//因为总大1,减去
                    List<String> listmini = new ArrayList<String>();
                    for (int k = cellStart; k <= cellEnd; k++) {
                        Cell cell = xssRow.getCell(k);
                        String value = getCellStringValue(cell);
                        listmini.add(value);
                    }
                    if (listmini.size() != 0) {
                        lists.add(listmini);
                    }
                }
            }
            tempMap.put(String.valueOf(j), lists);
        }
        System.out.println(tempMap);
        return tempMap;

    }
    public static String getCellStringValue(Cell cell) {
        String cellValue = "";
        // 以下是判断数据的类型
        switch (cell.getCellTypeEnum()) {
            case NUMERIC: // 数字
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    cellValue = sdf.format(DateUtil.getJavaDate(cell.getNumericCellValue())).toString();
                } else {
                    DataFormatter dataFormatter = new DataFormatter();
                    cellValue = dataFormatter.formatCellValue(cell);
                }
                break;
            case STRING: // 字符串
                cellValue = cell.getStringCellValue();
                break;
            case BOOLEAN: // Boolean
                cellValue = cell.getBooleanCellValue() + "";
                break;
            case FORMULA: // 公式
                cellValue = cell.getCellFormula() + "";
                break;
            case BLANK: // 空值
                cellValue = "";
                break;
            case ERROR: // 故障
                cellValue = "非法字符";
                break;
            default:
                cellValue = "未知类型";
                break;
        }
        return cellValue;
    }
}
