package cloud.erda.agent.core.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author zhaihongwei
 * @since 2021/9/8
 */
public class Md5Utils {

    public static String encode(String key) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(key.getBytes());
        return new BigInteger(1, md.digest()).toString(16);
    }
}
