package com.alphathur;

import com.alphathur.generator.RestfulApiGenerator;
import java.io.IOException;
import java.net.URL;

public class App {

  public static void main(String[] args) throws IOException {
    String jsonFileName = "/Users/zhuhuiyuan/Downloads/swagger-json1.json";
    String excelFileName = "/Users/zhuhuiyuan/Downloads/4.xlsx";

    //generate excel by online way
    RestfulApiGenerator generator1 = new RestfulApiGenerator(
        new URL("http://localhost:8081/v2/api-docs"), excelFileName, "sheet13");

    //generate excel by local json file
    RestfulApiGenerator generator2 = new RestfulApiGenerator(jsonFileName, excelFileName,
        "sheet14");

    generator1.genRestfulExcel();
    generator2.genRestfulExcel();
  }
}
