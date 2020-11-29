package com.lgh.tool.excel.export;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName ExportExcel
 * @Description:导出
 * @Author lgh
 * @Date 2020/11/3 9:46
 **/
public class ExportExcel {

        /**
         * 工作薄
         */
        private static HSSFWorkbook workbook;

        //sheet
        private static HSSFSheet sheet;

        //标题行开始位置
        private static final int TITLE_START_POSITION = 0;

        //时间行开始位置
//    private static final int DATEHEAD_START_POSITION = 1;

        //表头行开始位置
        private static final int HEAD_START_POSITION = 1;

        //文本行开始位置
        private static final int CONTENT_START_POSITION = 2;


        /**
         * 数据导出（多sheet）
         *
         * @param dataMap   对象集合   key为主表bean   value为从表List集合
         * @param titleMap  表头信息(对象属性名称--要显示的标题值)
         * @param sheetName sheet名称和表头值（静态部分）
         * @param sheetList sheet名称和表头值 （动态部分）（传入要获取的key对象的字段）
         */
        public static void excelExportBacth(Map<?, ?> dataMap, Map<String, String> titleMap, List<String> sheetList, String sheetName,
                                            String suffix, HttpServletResponse response, String fileName) {
            try (OutputStream output = response.getOutputStream()) {
                // 初始化workbook
                initHSSFWorkbook(sheetName);
                Set<?> dateSet = dataMap.keySet();
                for (Object obj : dateSet
                ) {
                    //将sheetName赋给temp
                    String temp = sheetName;
                    //拼接sheetName
                    sheetName = getSheetName(obj, sheetList, sheetName);
                    //创建sheet；
                    sheet = workbook.createSheet(sheetName);
                    //标题行
                    createTitleRow(titleMap, sheetName);
                    //清空sheetName
                    sheetName = temp;
                    //时间行
//        createDateHeadRow(titleMap);
                    //表头行
                    createHeadRow(titleMap);
                    // 文本行
                    createContentRow((List<?>) dataMap.get(obj), titleMap);
                    // 写入处理结果
                }
                response.reset();
                response.setHeader("Content-disposition", "attachment; filename=" + URLEncoder.encode(fileName, "utf-8") + suffix);
                response.setContentType("application/vnd.ms-excel;charset=utf-8");
                response.setCharacterEncoding("utf-8");
                workbook.write(output);
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * @param obj       Map的key对象
         * @param sheetList sheet名称和表头值 （动态部分）（传入要获取的key对象的字段）
         * @param sheetName sheet名称和表头值（静态部分）
         * @return String 拼接好的sheetName
         */
        private static String getSheetName(Object obj, List<String> sheetList, String sheetName) {
            try {
                for (String str : sheetList) {
                    String method = "get" + str.substring(0, 1).toUpperCase() + str.substring(1);
                    Method m = obj.getClass().getMethod(method, null);
                    Object objectValue = m.invoke(obj, null);
                    if (objectValue != null) {
                        sheetName = objectValue.toString() + "--" + sheetName;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return sheetName;
        }


        /**
         * 数据导出(单sheet)
         *
         * @param dataList  对象集合
         * @param titleMap  表头信息(对象属性名称--要显示的标题值)
         * @param sheetName sheet名称和表头值
         */
        public static void excelExport(List<?> dataList, Map<String, String> titleMap, String sheetName,
                                       String suffix, HttpServletResponse response, String fileName) {
            // 初始化workbook
            initHSSFWorkbook(sheetName);
            sheet = workbook.createSheet();
            //标题行
            createTitleRow(titleMap, sheetName);
            //时间行
//        createDateHeadRow(titleMap);
            //表头行
            createHeadRow(titleMap);
            // 文本行
            createContentRow(dataList, titleMap);
            // 写入处理结果

            try (OutputStream output = response.getOutputStream()) {
                response.reset();
                response.setHeader("Content-disposition", "attachment; filename=" + URLEncoder.encode(fileName, "utf-8") + suffix);
                response.setContentType("application/vnd.ms-excel;charset=utf-8");
                response.setCharacterEncoding("utf-8");
                workbook.write(output);
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        /**
         * 初始化
         *
         * @param sheetName
         */
        private static void initHSSFWorkbook(String sheetName) {
            workbook = new HSSFWorkbook();
        }

        /**
         * 生成标题(第0行创建)
         *
         * @param titleMap  对象属性名称--表头显示名称
         * @param sheetName sheet名称
         */
        private static void createTitleRow(Map<String, String> titleMap, String sheetName) {
            CellRangeAddress titleRange = new CellRangeAddress(0, 0, 0, titleMap.size() - 1);
            sheet.addMergedRegion(titleRange);
            HSSFRow titleRow = sheet.createRow(TITLE_START_POSITION);
            HSSFCell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(sheetName);

            //创建单元格，并设置值表头 设置表头居中
            HSSFCellStyle styleTitle = titleCell.getCellStyle();
            //水平居中
            styleTitle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            //垂直居中
            styleTitle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        }

        /**
         * 创建表头行(第二行创建)
         *
         * @param titleMap 对象属性名称--表头显示名称
         */
        public static void createHeadRow(Map<String, String> titleMap) {
            HSSFRow headRow = sheet.createRow(HEAD_START_POSITION);
            int i = 0;
            for (String entry : titleMap.keySet()) {
                HSSFCell headCell = headRow.createCell(i);
                headCell.setCellValue(titleMap.get(entry));
                i++;
            }
        }

        /**
         * 创建文本行
         *
         * @param dataList 对象数据集合
         * @param titleMap 对象属性名称--表头显示名称
         */
        public static void createContentRow(List<?> dataList, Map<String, String> titleMap) {
            try {
                int i = 0;
                for (Object obj : dataList) {
                    HSSFRow textRow = sheet.createRow(CONTENT_START_POSITION + i);
                    int j = 0;
                    int cellLength = 0;
                    Object objectValue;
                    for (String entry : titleMap.keySet()) {
                        if (obj instanceof Map) {
                            objectValue = ((Map) obj).get(entry);

                        } else {
                            String method = "get" + entry.substring(0, 1).toUpperCase() + entry.substring(1);
                            Method m = obj.getClass().getMethod(method, null);
                            objectValue = m.invoke(obj, null);

                        }

                        String value = "";
                        if (objectValue != null) {
                            value = objectValue.toString();
                            if (cellLength < value.getBytes().length) {
                                cellLength = value.getBytes().length;
                            }
                        }
                        HSSFCell textcell = textRow.createCell(j);
                        textcell.setCellValue(value);
                        j++;

                    }
                    if (cellLength != 0) {
                        sheet.setColumnWidth(i, cellLength * 2 * 256);
                    }
                    i++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

