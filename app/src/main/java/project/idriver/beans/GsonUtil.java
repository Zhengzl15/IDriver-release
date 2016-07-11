package project.idriver.beans;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * Created by ryan_wu on 16/1/25.
 */
public class GsonUtil {
    private static Gson gson = new Gson();

    // object to string
    public static String toJson(Object bean) {
        return gson.toJson(bean);
    }

    // string to object
    public static <T> T fromJson(String json, Class<T> typeOfT){
        return gson.fromJson(json, (Type) typeOfT);
    }

}
