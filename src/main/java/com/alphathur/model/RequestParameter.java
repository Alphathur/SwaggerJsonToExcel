package com.alphathur.model;

import com.alphathur.util.ExcelTitle;
import com.alphathur.util.Order;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestParameter {

  @Order(1)
  @ExcelTitle("参数名称")
  private String param;

  @Order(2)
  @ExcelTitle("参数类型")
  private String paramType;

  @Order(3)
  @ExcelTitle("参数说明")
  private String paramZh;

  @Order(4)
  @ExcelTitle("是否必填")
  private Boolean required;

  @Order(5)
  @ExcelTitle("示例")
  private String example;

  @Order(6)
  @ExcelTitle("对象描述")
  private LinkedHashMap<String, List<RequestParameter>> restExt = new LinkedHashMap<>();
}
