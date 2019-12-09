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
      buildOneData(list, parameter);
    }
    return list;
  }

  public List<Object[]> buildExtParamList(List<RequestParameter> parameters) {
    List<Object[]> list = new ArrayList<>();
    for (RequestParameter parameter : parameters) {
      if (!parameter.getRestExt().isEmpty()) {
        list.add(new String[]{parameter.getParam() + "对象描述"});
        List<RequestParameter> parameterList = parameter.getRestExt().get(parameter.getParam());
        for (RequestParameter parameter1 : parameterList) {
          buildOneData(list, parameter1);
          buildExtParamList(parameterList);
        }
      }
    }
    return list;
  }

  public List<Object[]> buildExtRespList(List<Response> responses) {
    List<Object[]> list = new ArrayList<>();
    for (Response response : responses) {
      if (!response.getRespExt().isEmpty()) {
        list.add(new String[]{response.getResponse() + "对象描述"});
        List<Response> responses1 = response.getRespExt().get(response.getResponse());
        for (Response resp1 : responses1) {
          Object[] data = new Object[3];
          data[0] = resp1.getResponse();
          data[1] = resp1.getResponseType();
          data[2] = resp1.getResponseZh();
          list.add(data);
          buildExtRespList(responses1);
        }
      }
    }
    return list;
  }


  private void buildOneData(List<Object[]> list, RequestParameter parameter1) {
    Object[] data = new Object[5];
    data[0] = parameter1.getParam();
    data[1] = parameter1.getParamType();
    data[2] = parameter1.getParamZh();
    data[3] = parameter1.getRequired();
    data[4] = parameter1.getExample();
    list.add(data);
  }

  public List<Object[]> buildRespList() {
    List<Object[]> list = new ArrayList<>();
    for (Response res : responses) {
      Object[] data = new Object[3];
      data[0] = res.getResponse();
      data[1] = res.getResponseType();
      data[2] = res.getResponseZh();
      list.add(data);
    }
    return list;
  }
}
