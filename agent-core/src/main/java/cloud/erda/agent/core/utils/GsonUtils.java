package cloud.erda.agent.core.utils;

import com.google.gson.Gson;

import java.nio.charset.Charset;

public class GsonUtils {
    private static final Gson gson = new Gson();
    private static final Charset charset = Charset.forName("UTF-8");

    public static String toJson(Object src){
        return gson.toJson(src);
    }

    public static byte[] toBytes(Object obj){
        return gson.toJson(obj).getBytes(charset);
    }
}
