package com.alphathur.model;

import com.alphathur.util.ExcelTitle;
import com.alphathur.util.Order;
import lombok.Data;

@Data
public class RequestHeader {

  @Order(1)
  @ExcelTitle("请求路径")
  private String url;

  @Order(2)
  @ExcelTitle("请求方式")
  private String requestMethod;

  @Order(0)
  @ExcelTitle("接口名称")
  private String comment;
}
