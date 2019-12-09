package com.alphathur.model;

import com.alphathur.util.ExcelTitle;
import com.alphathur.util.Order;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.Data;

@Data
public class Response {

  @Order(1)
  @ExcelTitle("参数名称")
  private String response;

  @Order(2)
  @ExcelTitle("参数类型")
  private String responseType;

  @Order(3)
  @ExcelTitle("参数说明")
  private String responseZh;

  @Order(4)
  @ExcelTitle("对象描述")
  private LinkedHashMap<String, List<Response>> respExt = new LinkedHashMap<>();
}
