package cn.xxx.entity;

import lombok.Data;
import org.springframework.http.RequestEntity;

@Data
public class ScanItem {
    private String params;

    private String response;

    private RequestEntity requestEntity;

    public ScanItem(String params, RequestEntity requestEntity, String response) {
        this.params = params;
        this.requestEntity = requestEntity;
        this.response = response;
    }
}
