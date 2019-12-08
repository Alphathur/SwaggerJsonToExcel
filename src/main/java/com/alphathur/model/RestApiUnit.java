package com.alphathur.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class RestApiUnit {

  private RequestHeader header;
  private List<RequestParameter> parameters = new ArrayList<>();
  private List<Response> responses = new ArrayList<>();

  private String tag;

  public List<Object[]> buildTitle() {
    List<Object[]> list = new ArrayList<>();
    Object[] data = new Object[5];
    data[1] = header.getUrl();
    data[0] = header.getComment();
    data[2] = header.getRequestMethod();
    list.add(data);
    return list;
  }

  public List<Object[]> buildParamList() {
    List<Object[]> list = new ArrayList<>();
    for (RequestParameter parameter : parameters) {
      Object[] data = new Object[5];
      data[0] = parameter.getParam();
      data[1] = parameter.getParamType();
      data[2] = parameter.getParamZh();
      data[3] = parameter.getRequired();
      data[4] = parameter.getExample();
      list.add(data);
    }
    return list;
  }

  public List<Object[]> buildRespList() {
    List<Object[]> list = new ArrayList<>();
    for (Response res : responses) {
      Object[] data = new Object[5];
      data[0] = res.getResponse();
      data[1] = res.getResponseType();
      data[2] = res.getResponseZh();
      list.add(data);
    }
    return list;
  }
}
