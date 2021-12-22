package cn.xxx.crawler;

import cn.xxx.entity.ConstEnum;
import cn.xxx.entity.RequestInfo;
import cn.xxx.entity.ScanItem;
import cn.xxx.utils.EexcuteUtil;
import cn.xxx.utils.HttpUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Future;

@Slf4j
@Component
public class ActiveCraw {
    private final String crawlerPath = "rad_windows_amd64.exe";
    private String outputPath = "result";
    private final String targetsPath = "targets.txt";

    @Autowired
    MakeRequest makeRequest;

    private final String[] STATIC_FILE_EXT = new String[]{
            "png",
            "jpg",
            "gif",
            "pdf",
            "bmp",
            "js",
            "css",
            "ico",
            "woff",
            "woff2",
            "ttf",
            "otf",
            "ttc",
            "svg",
            "psd",
            "exe",
            "zip",
            "rar",
            "7z",
            "msi",
            "tar",
            "gz",
            "mp3",
            "mp4",
            "mkv",
            "swf",
            "xls",
            "xlsx",
            "doc",
            "docx",
            "ppt",
            "pptx",
            "iso"
    };

    public int startCraw() {
        List<String> targets = getTargets();
        int ret = 0;
        for (String target : targets) {
            File file = new File(outputPath);
            if (file.exists()) {
                file.delete();
            }
            String[] params = {"-json", outputPath, "-t", target};
            try {
                ret = EexcuteUtil.execCmd(crawlerPath, params);
                JSONArray results = getResult();
                Future<Map<String, ScanItem>> future = makeRequest.doPOC(buildRequest(results));
                makeRequest.finalCheck(future);
            }catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return ret;
    }

    private JSONArray getResult() {
        String resultStr = null;
        try {
            File file = new File(outputPath);//定义一个file对象，用来初始化FileReader
            FileReader reader = new FileReader(file);//定义一个fileReader对象，用来初始化BufferedReader
            BufferedReader bReader = new BufferedReader(reader);//new一个BufferedReader对象，将文件内容读取到缓存
            StringBuilder sb = new StringBuilder();//定义一个字符串缓存，将字符串存放缓存中
            String s = "";
            while ((s = bReader.readLine()) != null) {//逐行读取文件内容，不读取换行符和末尾的空格
                sb.append(s + "\n");//将读取的字符串添加换行符后累加存放在缓存中
            }
            bReader.close();
            resultStr = sb.toString();
        }catch (Exception e) {
            log.error(e.getMessage());
        }

        return JSONArray.parseArray(resultStr);
    }

    private List<RequestInfo> buildRequest(JSONArray requests) {
        List<RequestInfo> requestList = new ArrayList<>();

        for (Object req : requests) {
            RequestInfo requestInfo = new RequestInfo();
            try {
                JSONObject reqJson = (JSONObject) req;
                String url = reqJson.getString("URL");
                Integer endIndex = url.contains("?") ? url.indexOf("?") : url.length();
                if (isStaticFile(url)) {
                    continue;
                }
                MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<>();
                for (Map.Entry entry : reqJson.getJSONObject("Header").entrySet()) {
                    headersMap.add(entry.getKey().toString(), entry.getValue().toString());
                }
                headersMap.add("Referer", url.substring(0, url.lastIndexOf("/")));
                HttpHeaders headers = new HttpHeaders(headersMap);

                String method = reqJson.getString("Method");
                MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();

                if (method.equals("POST")) {
                    String contentType = headers.getContentType().getType() + "/" + headers.getContentType().getSubtype();
                    String paramsStr = new String(Base64.getDecoder().decode(reqJson.getString("b64_body")));
                    if (contentType.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
                        String[] paramsArray = paramsStr.split("&");
                        for (String param : paramsArray) {
                            String key = param.substring(0, param.indexOf("="));
                            String value = param.substring(param.indexOf("=") + 1);
                            params.add(key, value);
                        }
                        requestInfo.setParam_type(ConstEnum.PARAM_BODY);
                    } else if (contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
                        JSONObject paramsJson = JSONObject.parseObject(paramsStr);
                        for (String key : paramsJson.keySet()) {
                            params.add(key, paramsJson.getString(key));
                        }
                    }
                } else if (method.equals("GET")) {
                    if (url.contains("?")) {
                        requestInfo.setParam_type(ConstEnum.PARAM_URL);
                    } else {
                        requestInfo.setParam_type(ConstEnum.PARAM_NONE);
                    }

                }
                RequestEntity<MultiValueMap<String, String>> requestEntity = new RequestEntity<>(params, headersMap, HttpMethod.resolve(method), URI.create(reqJson.getString("URL")));
                requestInfo.setRequestEntity(requestEntity);
                requestList.add(requestInfo);
            }catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        return requestList;
    }

    private boolean isStaticFile(String url) {
        return Arrays.stream(STATIC_FILE_EXT).anyMatch(s -> s.equalsIgnoreCase(HttpUtils.getUrlFileExt(url)));
    }

    private List<String> getTargets() {
        List<String> targets = new ArrayList<>();
        try {
            if (System.getProperty("target") != null && System.getProperty("target").length() > 0) {
                targets.add(System.getProperty("target"));
            } else {
                BufferedReader  reader = new BufferedReader(new InputStreamReader(new FileInputStream(targetsPath), "UTF-8"));
                String temp;
                while ((temp = reader.readLine()) != null) {
                    targets.add(temp);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return targets;
    }
}
