package io.cruii;

import cn.hutool.core.util.URLUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cruii
 * Created on 2022/10/14
 */
//@SpringBootTest
//@RunWith(org.springframework.test.context.junit4.SpringRunner.class)
public class BilibiliUserTest {
    @Test
    public void parseUrl() throws MalformedURLException {
        String url="https://passport.biligame.com/crossDomain?DedeUserID=287969457\u0026DedeUserID__ckMd5=e6f2d9a03ca037bd\u0026Expires=1681227537\u0026SESSDATA=605d4f97,1681227537,eac30*a1\u0026bili_jct=94896bdce596eff2d226d531d2b0bd85\u0026gourl=https%3A%2F%2Fwww.bilibili.com";
        String decodeUrl = URLUtil.decode(url);
        System.out.println(decodeUrl);
        URL url1 = new URL(decodeUrl);
        System.out.println(getUrlParams(url1.getQuery()));
    }

    public static Map<String, Object> getUrlParams(String param) {
        Map<String, Object> map = new HashMap<String, Object>(0);
        if (param.isEmpty()) {
            return map;
        }
        String[] params = param.split("&");
        for (String s : params) {
            String[] p = s.split("=");
            if (p.length == 2) {
                map.put(p[0], p[1]);
            }
        }
        return map;
    }
}
