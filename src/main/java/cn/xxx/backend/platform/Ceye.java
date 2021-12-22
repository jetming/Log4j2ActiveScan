package cn.xxx.backend.platform;

import cn.xxx.backend.IBackend;
import cn.xxx.poc.IPOC;
import cn.xxx.utils.Utils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class Ceye implements IBackend {

    RestTemplate restTemplate;
    String platformUrl = "http://api.ceye.io/";
    String rootDomain = "<rootDomain>";
    String token = "<token>";

    public Ceye(String rootDomain, String token) {
        this.rootDomain = rootDomain;
        this.token = token;
    }

    public Ceye() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String getName() {
        return "Ceye.io";
    }

    @Override
    public String getNewPayload() {
        return Utils.getCurrentTimeMillis() + Utils.GetRandomString(5).toLowerCase() + "." + rootDomain;
    }

    @Override
    public boolean CheckResult(String domain) {
        try {
            JSONObject jObj = restTemplate.getForObject(platformUrl + "v1/records?token=" + token + "&type=dns&filter=" + domain.toLowerCase().substring(0, domain.indexOf(".")), JSONObject.class);
            if (jObj != null && jObj.containsKey("data") && jObj.getString("data").contains(domain)) {
                return true;
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return false;
        }
        return false;
    }

    @Override
    public boolean flushCache(int count) {
        return flushCache();
    }

    @Override
    public boolean flushCache() {
        return true;
    }

    @Override
    public boolean getState() {
        return true;
    }

    @Override
    public int[] getSupportedPOCTypes() {
        return new int[]{IPOC.POC_TYPE_LDAP, IPOC.POC_TYPE_RMI};
    }
}
