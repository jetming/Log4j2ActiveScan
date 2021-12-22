package cn.xxx.entity;

import lombok.Data;

@Data
public class ParamInfo {
    byte type;
    String name;
    String value;

    public String buildParameterStr(RequestInfo req, String param, String exp, byte paramType) {
        String origin = null;

        if(!origin.contains(param)) {
            return origin;
        }else {
            int beginIndex = origin.indexOf(param) + param.length() + 1;
            int endIndex = origin.indexOf('&', beginIndex);
            return origin.substring(0,beginIndex) + exp + origin.substring(endIndex);
        }
    }
}
