# Log4j2ActiveScan
结合长亭的rad浏览器爬虫，自动fuzz爬取到的链接参数，实现主动扫描log4j2 rce漏洞。

漏洞检测支持以下类型

* Url
* Cookie
* Header
* Body(x-www-form-urlencoded)
* Body(json)

# 使用方法
``` java -Dtarget=https://xxx.com/login.jsp -jar .\Log4j2ActiveScan-1.0-SNAPSHOT.jar ```

通过target参数指定扫描目标， 如不指定，则从当前目录加载targets.txt文件中指定目标列表。

# 参考
https://github.com/izj007/Log4j2Scan