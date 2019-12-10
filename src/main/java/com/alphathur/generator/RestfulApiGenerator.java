package com.alphathur.generator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alphathur.model.RequestHeader;
import com.alphathur.model.RequestParameter;
import com.alphathur.model.Response;
import com.alphathur.model.RestApiUnit;
import com.alphathur.util.ExportExcelUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class RestfulApiGenerator {

  /**
   * url represent swagger api-docs remote url
   */
  private URL url;

  /**
   * jsonFile represent the swagger json file's absolute path
   */
  private String jsonFile;

  /**
   * excelFile represent the output file's absolute path
   */
  private String excelFile;

  /**
   * sheetName represent the output sheet in excel
   */
  private String sheetName;

  public RestfulApiGenerator(URL url, String excelFile, String sheetName) {
    this.url = url;
    this.excelFile = excelFile;
    this.sheetName = sheetName;
  }

  public RestfulApiGenerator(String jsonFile, String excelFile, String sheetName) {
    this.jsonFile = jsonFile;
    this.excelFile = excelFile;
    this.sheetName = sheetName;
  }

  private static List<Response> buildResponse(String ref, JSONObject definitions) {
    List<Response> responses = new ArrayList<>();
    JSONObject refJson = definitions.getJSONObject(ref);

    if (refJson != null && !refJson.isEmpty()) {
      JSONObject properties = refJson.getJSONObject("properties");
      if (properties == null) {
        return responses;
      }
      for (String property : properties.keySet()) {
        Response response = new Response();
        response.setResponse(property);
        JSONObject proJson = properties.getJSONObject(property);
        if (proJson.containsKey("ref")) {
          response.setResponseType("object");
          String currentRef = proJson.getString("ref");
          String objRef = currentRef.substring(currentRef.lastIndexOf("/") + 1);
          if (!objRef.equals(ref)) {
            setRespExtMapValue(response, objRef, definitions);
          }
        } else {
          response.setResponseType(proJson.getString("type"));
        }
        response.setResponseZh(proJson.getString("description"));
        responses.add(response);
      }
    }
    return responses;
  }

  private static List<RequestParameter> buildRequest(String ref, JSONObject definitions) {
    List<RequestParameter> requestParameters = new ArrayList<>();
    JSONObject refJson = definitions.getJSONObject(ref);
    if (refJson != null && !refJson.isEmpty()) {
      JSONObject properties = refJson.getJSONObject("properties");
      if (properties == null) {
        return requestParameters;
      }
      for (String property : properties.keySet()) {
        RequestParameter requestParameter = new RequestParameter();
        requestParameter.setParam(property);
        JSONObject proJson = properties.getJSONObject(property);
        requestParameter.setParamType(proJson.getString("type"));
        if (proJson.containsKey("ref")) {
          requestParameter.setParamType("object");
          String currentRef = proJson.getString("ref");
          String objRef = currentRef.substring(currentRef.lastIndexOf("/") + 1);
          if (!objRef.equals(ref)) {
            setRestExtMapValue(requestParameter, objRef, definitions);
          }
        } else {
          requestParameter.setParamType(proJson.getString("type"));
        }
        requestParameter.setParamZh(proJson.getString("description"));
        requestParameter.setExample(proJson.getString("example"));
        requestParameter.setRequired(proJson.getBoolean("required"));
        requestParameters.add(requestParameter);
      }

    }
    return requestParameters;
  }

  private static void setRespExtMapValue(Response response, String ref, JSONObject definitions) {
    response.getRespExt().put(response.getResponse(), buildResponse(ref, definitions));
  }

  private static void setRestExtMapValue(RequestParameter requestParameter, String ref,
      JSONObject definitions) {
    requestParameter.getRestExt().put(requestParameter.getParam(), buildRequest(ref, definitions));
  }

  private static String getRequestMethod(JSONObject currentPath) {
    if (currentPath == null || currentPath.keySet().isEmpty()) {
      return null;
    }
    return currentPath.keySet().iterator().next();
  }

  public void genRestfulExcel() throws IOException {
    String jsonText = getOriginJson();
    List<RestApiUnit> restApiUnits = genDocUnit(jsonText);
    writeToExcel(restApiUnits);
  }

  private String getOriginJson() throws IOException {
    if (jsonFile != null) {
      return IOUtils
          .toString(new FileInputStream(new File(jsonFile)), Charset.forName("UTF-8"));
    }
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
    int status = con.getResponseCode();
    if (status != 200) {
      throw new RuntimeException("connect error");
    }
    BufferedReader in = new BufferedReader(
        new InputStreamReader(con.getInputStream()));
    String inputLine;
    StringBuilder content = new StringBuilder();
    while ((inputLine = in.readLine()) != null) {
      content.append(inputLine);
    }
    in.close();
    con.disconnect();
    return content.toString();
  }

  private List<RestApiUnit> genDocUnit(String jsonText) {

    JSONObject jsonObject = JSONObject
        .parseObject(jsonText.replace("$", "")); //解决fastjson无法读取$开头的key

    JSONObject paths = jsonObject.getJSONObject("paths");
    Set<String> urlSet = paths.keySet();

    JSONObject definitions = jsonObject.getJSONObject("definitions");

    List<RestApiUnit> apiUnits = new ArrayList<>();
    for (String path : urlSet) {
      JSONObject currentPath = paths.getJSONObject(path);
      String requestMethod = getRequestMethod(currentPath);
      if (requestMethod == null) {
        continue;
      }

      RestApiUnit apiUnit = new RestApiUnit();

      JSONObject paramObj = currentPath.getJSONObject(requestMethod);
      RequestHeader requestHeader = new RequestHeader();
      requestHeader.setUrl("http://{baseUrl}" + path);
      requestHeader.setRequestMethod(requestMethod);
      requestHeader.setComment(paramObj.getString("summary"));

      apiUnit.setHeader(requestHeader);

      JSONArray currentTags = paramObj.getJSONArray("tags");
      if (currentTags != null && !currentTags.isEmpty()) {
        apiUnit.setTag(currentTags.getString(0));
      }

      List<RequestParameter> parameters = new ArrayList<>();
      JSONArray jsonArray = paramObj.getJSONArray("parameters");
      if (jsonArray != null && !jsonArray.isEmpty()) {
        for (int i = 0; i < jsonArray.size(); i++) {
          JSONObject param = jsonArray.getJSONObject(i);
          JSONObject schema = param.getJSONObject("schema");
          if (schema != null) {
            String ref = schema.getString("ref");
            if (ref != null) {
              String objRef = ref.substring(ref.lastIndexOf("/") + 1);
              parameters.addAll(buildRequest(objRef, definitions));
            }
            String type = schema.getString("type");
            if (type != null) {
              if (type.equals("array")) {
                JSONObject items = schema.getJSONObject("items");
                ref = items.getString("ref");
                if (ref != null) {
                  String objRef = ref.substring(ref.lastIndexOf("/") + 1);
                  parameters.addAll(buildRequest(objRef, definitions));
                }
              } else {
                //返回单个字段
                RequestParameter requestParameter = new RequestParameter();
                requestParameter.setParamType(type);
                parameters.add(requestParameter);
              }
            }
          } else { //get请求或者post+form提交
            String name = param.getString("name");
            String type = param.getString("type");
            Boolean required = param.getBoolean("required");
            String description = param.getString("description");
            String example = param.getString("example");
            RequestParameter parameter = new RequestParameter();
            parameter.setParam(name);
            parameter.setParamType(type);
            parameter.setRequired(required);
            parameter.setParamZh(description);
            parameter.setExample(example);
            parameters.add(parameter);
          }
        }
      }

      apiUnit.setParameters(parameters);

      JSONObject responses = paramObj.getJSONObject("responses");
      JSONObject schema = responses.getJSONObject("200").getJSONObject("schema");

      List<Response> docResponses = new ArrayList<>();
      if (schema != null) {
        String ref = schema.getString("ref");
        if (ref != null) {
          String objRef = ref.substring(ref.lastIndexOf("/") + 1);
          docResponses.addAll(buildResponse(objRef, definitions));
        }
        String type = schema.getString("type");
        if (type != null) {
          if (type.equals("array")) {
            JSONObject items = schema.getJSONObject("items");
            ref = items.getString("ref");
            if (ref != null) {
              String objRef = ref.substring(ref.lastIndexOf("/") + 1);
              docResponses.addAll(buildResponse(objRef, definitions));
            }
          } else {
            //返回单个字段
            Response docResp = new Response();
            docResp.setResponseType(type);
            docResponses.add(docResp);
          }
        }
      }

      apiUnit.setResponses(docResponses);
      apiUnits.add(apiUnit);
    }
    return apiUnits;
  }

  private void writeToExcel(List<RestApiUnit> restApiUnits) {

    Map<String, List<RestApiUnit>> apiGroupMap = restApiUnits.stream()
        .collect(Collectors.groupingBy(RestApiUnit::getTag));

    Workbook workbook = ExportExcelUtil.getWorkBook(excelFile);

    Sheet sheet = ExportExcelUtil.getOrBuildSheet(workbook, sheetName);

    Object[] headTitle = ExportExcelUtil.buildTitleArr(RequestHeader.class);
    Object[] paramTitle = ExportExcelUtil.buildTitleArr(RequestParameter.class);
    Object[] respTitle = ExportExcelUtil.buildTitleArr(Response.class);

    apiGroupMap.forEach((k, apiUnits) -> {
      for (RestApiUnit apiUnit : apiUnits) {

        ExportExcelUtil.writeOneData(sheet, headTitle);
        ExportExcelUtil.writeDataList(sheet, apiUnit.buildTitle());

        ExportExcelUtil.writeOneData(sheet, paramTitle);
        if (apiUnit.getParameters().isEmpty()) {
          ExportExcelUtil.writeStringLine(sheet, "无");
        } else {
          ExportExcelUtil.writeDataList(sheet, apiUnit.buildParamList());
        }

        List<Object[]> restExtDatas = apiUnit.buildExtParamList(apiUnit.getParameters());
        if (!restExtDatas.isEmpty()) {
          ExportExcelUtil.writeDataList(sheet, restExtDatas);
        }

        ExportExcelUtil.writeOneData(sheet, respTitle);
        if (apiUnit.getResponses().isEmpty()) {
          ExportExcelUtil.writeStringLine(sheet, "无");
        } else {
          ExportExcelUtil.writeDataList(sheet, apiUnit.buildRespList());
        }
        List<Object[]> respExtDatas = apiUnit.buildExtRespList(apiUnit.getResponses());
        if (!respExtDatas.isEmpty()) {
          ExportExcelUtil.writeDataList(sheet, respExtDatas);
        }
        ExportExcelUtil.writeEmptyLine(sheet);
      }
    });

    ExportExcelUtil.persistToFile(workbook, excelFile);
  }

}
