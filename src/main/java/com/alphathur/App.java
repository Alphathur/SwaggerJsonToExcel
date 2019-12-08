package com.alphathur;

import com.alphathur.generator.RestfulApiGen;
import java.io.IOException;

public class App {

  public static void main(String[] args)
      throws IOException {
    String jsonFileName = "/Users/zhuhuiyuan/Downloads/swagger-json.json";
    String excelFileName = "/Users/zhuhuiyuan/Downloads/4.xlsx";
    String sheetName = "sheet10";
    RestfulApiGen.genRestfulExcel(jsonFileName, excelFileName, sheetName);
  }
}
