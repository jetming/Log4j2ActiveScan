package cn.xxx.entity;

import lombok.Data;
import org.springframework.http.RequestEntity;
import org.springframework.util.MultiValueMap;

@Data
public class RequestInfo {
    RequestEntity<MultiValueMap<String, String>> requestEntity;

    byte param_type;
}
