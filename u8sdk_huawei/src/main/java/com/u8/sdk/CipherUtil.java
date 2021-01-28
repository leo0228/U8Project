package com.u8.sdk;

import com.huawei.hms.support.log.common.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class CipherUtil {
    private static final String publicKey = "MIIBojANBgkqhkiG9w0BAQEFAAOCAY8AMIIBigKCAYEAk9pqTWwPj1bxixybAYyvHcUG4VunyKU3nXAubFB4flI+4s7IpKdicZr3Rz+F7fEUL1dkX0FceI7qyWfm1wEM/T6h1Jy+pZmYqOCR5zA5kY4o2vDEAOBiE3WxG/pQKEtKt/KH4D6exsx19Q9xlCLymsaK3ENJazx8U6cCDMXxPJBOI81UISOQl9f2zM3wA7QBRoEipAkEr+TKO+VaacCNoa0hV86mDLEad8olgmOV+9MqT+qW7nSDE+qgPldG4igG7uWD8O/j11uig227+gij0I1ORfhrhYUKbx281Xj3qmyrUvOOzYmP1zUGvov8i8QQkt6zefEAR7Sm/3it7moH5LWYXQ9sZw3Gkpaz7d8eWG6tbWgsFO6VfSbgV29EXy+YavEDOAiLhLsFLKFg8Z3GwZ+L7yViimyHb/yub2ruqAopFpV3onydaWthHZQg5nyOrA+p0N9H/RMgsLYj93+P4q3y3Yw20JUHi96is9jccLTxn5dtBIfTyF6GSuL4HA7HAgMBAAE=";

    /** *校验签名信息
     * @param content 结果字符串
     * @param sign 签名字符串
     * @param publicKey 支付公钥
     * @param是否校验通过
     */
    public static boolean doCheck(String content, String sign) {

        if ( sign == null) {
            return false;
        }

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedKey = Base64.decode(publicKey);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
            java.security.Signature signature = null;
            signature = java.security.Signature.getInstance("SHA256WithRSA");
            signature.initVerify(pubKey);
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            byte[] bsign = Base64.decode(sign);
            return signature.verify(bsign);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
