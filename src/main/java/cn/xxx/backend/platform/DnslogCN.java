package cn.xxx.backend.platform;

import cn.xxx.backend.IBackend;
import cn.xxx.poc.IPOC;
import cn.xxx.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Component
@Slf4j
public class DnslogCN implements IBackend {

    RestTemplate restTemplate;
    HttpHeaders headers = new HttpHeaders();

    String platformUrl = "http://www.dnslog.cn/";
    String rootDomain = "";
    String dnsLogResultCache = "";

    public DnslogCN() {
        this.restTemplate = new RestTemplate();
        this.initDomain();
    }

    private void initDomain() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(platformUrl + "/getdomain.php", String.class);
            List<String> cookies =new ArrayList<String>();
            rootDomain = response.getBody();
            cookies.add(response.getHeaders().get("Set-Cookie").get(0));
            headers.put(HttpHeaders.COOKIE, cookies);
//            startSessionHeartbeat();
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    private void startSessionHeartbeat() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                flushCache();
            }
        }, 0, 2 * 60 * 1000); //2min
    }

    @Override
    public String getName() {
        return "Dnslog.cn";
    }

    @Override
    public String getNewPayload() {
        return Utils.getCurrentTimeMillis() + Utils.GetRandomString(5) + "." + rootDomain;
    }

    public boolean flushCache() {
        try {
            RequestEntity request = new RequestEntity(headers, HttpMethod.GET, URI.create(platformUrl + "getrecords.php"));
            dnsLogResultCache = restTemplate.exchange(request, String.class).getBody().toLowerCase();

            log.info("Got Dnslog Result OK!: {}", dnsLogResultCache);
            return true;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean flushCache(int count) {
        return flushCache();
    }

    @Override
    public boolean CheckResult(String domain) {
        return dnsLogResultCache.contains(domain.toLowerCase());
    }

    @Override
    public boolean getState() {
        return rootDomain != "";
    }

    @Override
    public int[] getSupportedPOCTypes() {
        return new int[]{IPOC.POC_TYPE_LDAP, IPOC.POC_TYPE_RMI};
    }
}
