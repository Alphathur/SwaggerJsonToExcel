package com.alphathur.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class ExportExcelUtil {

  private ExportExcelUtil() {
  }

  public static <E> String[] buildTitleArr(Class<E> clazz) {
    Field[] fields = ExportExcelUtil.getAllFields(clazz);
    Map<String, Method> fieldMethodMap = ExportExcelUtil.buildFieldMethodMap(clazz);
    Map<String, String> fieldTitleMap = ExportExcelUtil.buildFieldTitleMap(fields, fieldMethodMap);
    ExportExcelUtil.sortMethodMap(fields, fieldMethodMap);

    //创建表头数组
    return ExportExcelUtil.buildTitleArr(fieldMethodMap, fieldTitleMap);
  }

  private static Map<String, String> buildFieldTitleMap(Field[] fields,
      Map<String, Method> fieldMethodMap) {
    Map<String, String> fieldTitleMap = new LinkedHashMap<>();
    List<String> removeKeys = new ArrayList<>();
    Arrays.stream(fields).forEach(field -> {
      if (fieldMethodMap.containsKey(field.getName())) {
        ExcelIgnore excelIgnore = field.getAnnotation(ExcelIgnore.class);
        boolean ignore = excelIgnore != null && excelIgnore.value();
        if (ignore) {
          removeKeys.add(field.getName());
          return;
        }
        ExcelTitle excelTitle = field.getAnnotation(ExcelTitle.class);
        String title = excelTitle == null ? field.getName() : excelTitle.value();
        fieldTitleMap.put(field.getName(), title);
      }
    });
    removeKeys.forEach(key -> fieldMethodMap.remove(key));
    return fieldTitleMap;
  }


  private static String[] buildTitleArr(Map<String, Method> fieldMethodMap,
      Map<String, String> fieldTitleMap) {
    List<String> fieldlist = new ArrayList<>(fieldMethodMap.keySet());
    int itemSize = fieldTitleMap.size();
    String[] titleArr = new String[itemSize];
    for (int i = 0; i < fieldlist.size(); i++) {
      String field = fieldlist.get(i);
      titleArr[i] = fieldTitleMap.get(field);
    }
    return titleArr;
  }

  private static <E> Map<String, Method> buildFieldMethodMap(Class<E> clazz) {
    List<Method> getMethods = Arrays.stream(clazz.getMethods())
        .filter(
            method -> method.getName().startsWith("get") && !method.getName().equals("getClass"))
        .collect(
            Collectors.toList());
    Map<String, Method> fieldMethodMap = new LinkedHashMap<>();
    for (Method getMethod : getMethods) {
      String m = getMethod.getName().replace("get", "");
      String field = m.substring(0, 1).toLowerCase() + m.substring(1);
      fieldMethodMap.put(field, getMethod);
    }
    return fieldMethodMap;
  }

  private static <E> Field[] getAllFields(Class<E> clazz) {
    List<Field> fieldList = new ArrayList<>();
    while (clazz != null) {
      fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
      clazz = (Class<E>) clazz.getSuperclass();
    }
    Field[] fields = new Field[fieldList.size()];
    fieldList.toArray(fields);
    return fields;
  }


  private static void sortMethodMap(Field[] fields, Map<String, Method> fieldMethodMap) {
    Set<String> fieldSet = fieldMethodMap.keySet();
    List<Field> fieldList = Arrays.stream(fields).filter(e -> fieldSet.contains(e.getName()))
        .collect(Collectors.toList());
    fields = fieldList.toArray(new Field[]{});
    Arrays.sort(fields, (o1, o2) -> {
      Order order1 = o1.getAnnotation(Order.class);
      Order order2 = o2.getAnnotation(Order.class);
      if (order1 == null && order2 == null) { //均不含注解时不排序
        return 0;
      }
      if (order1 == null) { //order1 == null && order2 != null 仅有一个含有注解时，默认排到不含注解的后面
        return -1;
      }
      if (order2 == null) { //order1 != null && order2 == null 仅有一个含有注解时，默认排到不含注解的后面
        return 1;
      }
      return order1.value() - order2.value();//均含有注解时，按照注解值从小到大排序
    });
    Map<String, Method> sortedMethodMap = new LinkedHashMap<>();
    Arrays.stream(fields).forEach(e -> {
      String key = e.getName();
      sortedMethodMap.put(key, fieldMethodMap.get(key));
    });
    fieldMethodMap.clear();
    fieldMethodMap.putAll(sortedMethodMap);
  }

  public static void writeStringLine(Sheet sheet, String input) {
    int lastRowNum = sheet.getLastRowNum();
    Row dataRow = sheet.createRow(lastRowNum + 1);
    Cell cell = dataRow.createCell(0);
    setCellValue(input, cell);
  }

  public static void writeEmptyLine(Sheet sheet) {
    int lastRowNum = sheet.getLastRowNum();
    sheet.createRow(lastRowNum + 1);
  }

  public static void writeOneData(Sheet sheet, Object[] dataArr) {
    int lastRowNum = sheet.getLastRowNum();
    Row dataRow = sheet.createRow(lastRowNum + 1);
    for (int i = 0; i < dataArr.length; i++) {
      Cell cell = dataRow.createCell(i);
      Object cellValue = dataArr[i];
      if (cellValue != null) {
        setCellValue(cellValue, cell);
      }
    }
  }

  public static void writeDataList(Sheet sheet, List<Object[]> list) {
    for (Object[] dataArr : list) {
      writeOneData(sheet, dataArr);
    }
  }

  public static Workbook getWorkBook(String fileName) {
    Workbook workbook = null;
    try {
      if (fileName.endsWith("xls")) {
        workbook = new HSSFWorkbook(new FileInputStream(new File(fileName)));
      } else if (fileName.endsWith("xlsx")) {
        workbook = new XSSFWorkbook(new FileInputStream(new File(fileName)));
      } else {
        throw new IllegalArgumentException("fileName not legal");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (workbook == null) {
      throw new IllegalArgumentException("workbook not exist");
    }
    return workbook;
  }

  public static Sheet getOrBuildSheet(Workbook workbook, String sheetName) {
    Sheet sheet;
    if (sheetName != null && !"".equals(sheetName.trim())) {
      sheet = workbook.getSheet(sheetName);
      if (sheet == null) {
        sheet = workbook.createSheet(sheetName);
      }
    } else {
      sheet = workbook.createSheet();
    }
    return sheet;
  }

  public static void persistToFile(Workbook workbook, String fileName) {
    FileOutputStream outputStream = null;
    try {
      outputStream = new FileOutputStream(new File(fileName));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    try {
      workbook.write(outputStream);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (outputStream != null) {
        try {
          outputStream.close();
        } catch (IOException ignore) {
        }
      }
    }
  }

  private static void setCellValue(Object cellValue, Cell cell) {
    if (cellValue instanceof Boolean) {
      cell.setCellValue((boolean) cellValue);
    } else if (cellValue instanceof String) {
      cell.setCellValue(cellValue.toString());
    } else if (cellValue instanceof Double || cellValue instanceof Integer
        || cellValue instanceof Long) {
      cell.setCellValue(Double.valueOf(cellValue.toString()));
    } else if (cellValue instanceof Date) {
      cell.setCellValue((Date) cellValue);
    } else if (cellValue instanceof Calendar) {
      cell.setCellValue((Calendar) cellValue);
    } else if (cellValue instanceof RichTextString) {
      cell.setCellValue((RichTextString) cellValue);
    } else {
      cell.setCellValue(cellValue.toString());
    }
  }

}
