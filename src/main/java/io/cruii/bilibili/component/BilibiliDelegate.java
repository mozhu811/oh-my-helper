package io.cruii.bilibili.component;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.cruii.bilibili.constant.BilibiliAPI;
import io.cruii.bilibili.entity.BilibiliUser;
import io.cruii.bilibili.entity.TaskConfig;
import io.cruii.bilibili.exception.RequestException;
import io.cruii.bilibili.util.CosUtil;
import io.cruii.bilibili.util.HttpUtil;
import io.cruii.bilibili.util.ProxyUtil;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2021/9/14
 */
@Log4j2
public class BilibiliDelegate {
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36";

    @Getter
    private final TaskConfig config;

    private String proxyHost;

    private Integer proxyPort;

    public BilibiliDelegate(String dedeuserid, String sessdata, String biliJct) {
        TaskConfig taskConfig = new TaskConfig();
        taskConfig.setDedeuserid(dedeuserid);
        taskConfig.setSessdata(sessdata);
        taskConfig.setBiliJct(biliJct);
        taskConfig.setUserAgent(UA);
        this.config = taskConfig;
        setProxy();
    }

    public BilibiliDelegate(TaskConfig config) {
        this.config = config;
        if (CharSequenceUtil.isBlank(config.getUserAgent())) {
            config.setUserAgent(UA);
        }
        setProxy();
    }

    public void setProxy() {
        String proxy = ProxyUtil.get();

        setProxy(proxy);
    }

    private void setProxy(String proxy) {
        this.proxyHost = proxy.split(":")[0];
        this.proxyPort = Integer.parseInt(proxy.split(":")[1]);
    }

    /**
     * 获取用户B站导航栏状态信息
     *
     * @return B站用户信息 {@link BilibiliUser}
     */
    public BilibiliUser getUser() {
        JSONObject resp = doGet(BilibiliAPI.GET_USER_INFO_NAV);

        // 解析响应信息
        JSONObject data = resp.getJSONObject("data");
        // 是否登录成功
        Boolean isLogin = data.getBool("isLogin");

        if (Boolean.FALSE.equals(isLogin)) {
            log.warn("账号Cookie已失效, {}, {}", config.getDedeuserid(), config.getSessdata());

            // 如果没有登录成功，则返回简要信息
            return getUser(config.getDedeuserid());
        }

        // 登录成功，获取详细信息
        // 获取头像
        try {
            byte[] faces = getAvatarStream(data.getStr("face"));

            String path = "avatars" + File.separator + config.getDedeuserid() + ".png";
            File avatarFile = new File(path);
            if (avatarFile.exists()) {
                String localMd5 = SecureUtil.md5().digestHex(avatarFile);
                String remoteMd5 = SecureUtil.md5().digestHex(faces);
                if (!localMd5.equals(remoteMd5)) {
                    FileUtil.writeBytes(faces, avatarFile);
                }
            } else {
                FileUtil.writeBytes(faces, avatarFile);
            }

            // 上传到 oss
            CosUtil.upload(avatarFile);
        } catch (Exception e) {
            log.error("获取头像失败", e);
        }

        String uname = data.getStr("uname");
        // 获取硬币数
        String coins = data.getStr("money");

        // 获取大会员信息
        JSONObject vip = data.getJSONObject("vip");

        // 获取等级信息
        JSONObject levelInfo = data.getJSONObject("level_info");
        Integer currentLevel = levelInfo.getInt("current_level");

        // 获取勋章墙
        JSONObject medalWallResp = getMedalWall();
        List<JSONObject> medals = medalWallResp.getJSONObject("data")
                .getJSONArray("list")
                .stream()
                .map(JSONUtil::parseObj)
                .map(medalObj -> {
                    JSONObject medal = JSONUtil.createObj();
                    medal.set("name", medalObj.getByPath("medal_info.medal_name", String.class));
                    medal.set("level", medalObj.getByPath("medal_info.level", Integer.class));
                    medal.set("colorStart", medalObj.getByPath("medal_info.medal_color_start", Integer.class));
                    medal.set("colorEnd", medalObj.getByPath("medal_info.medal_color_end", Integer.class));
                    medal.set("colorBorder", medalObj.getByPath("medal_info.medal_color_border", Integer.class));
                    return medal;
                })
                .sorted((o1, o2) -> o2.getInt("level") - o1.getInt("level"))
                .limit(2L)
                .collect(Collectors.toList());

        BilibiliUser info = new BilibiliUser();
        info.setDedeuserid(config.getDedeuserid());
        info.setUsername(uname);
        info.setCoins(coins);
        info.setLevel(currentLevel);
        info.setCurrentExp(levelInfo.getInt("current_exp"));
        info.setNextExp(currentLevel == 6 ? 0 : levelInfo.getInt("next_exp"));
        info.setMedals(JSONUtil.toJsonStr(medals));
        info.setVipType(vip.getInt("type"));
        info.setVipStatus(vip.getInt("status"));
        info.setIsLogin(true);

        return info;
    }

    /**
     * 当Cookie过期时获取用户的部分信息
     *
     * @param userId B站uid
     * @return B站用户信息 {@link BilibiliUser}
     */
    public BilibiliUser getUser(String userId) {
        Map<String, String> params = new HashMap<>();
        params.put("mid", userId);
        JSONObject resp = doGet(BilibiliAPI.GET_USER_SPACE_INFO, params);
        JSONObject baseInfo = resp.getJSONObject("data");
        if (resp.getInt("code") == -404 || baseInfo == null) {
            log.error("用户[{}]不存在", userId);
            return null;
        }
        byte[] faces = getAvatarStream(baseInfo.getStr("face"));

        String path = "avatars" + File.separator + config.getDedeuserid() + ".png";
        File avatarFile = new File(path);
        if (avatarFile.exists()) {
            String localMd5 = SecureUtil.md5().digestHex(avatarFile);
            String remoteMd5 = SecureUtil.md5().digestHex(faces);
            if (!localMd5.equals(remoteMd5)) {
                FileUtil.writeBytes(faces, avatarFile);
            }
        } else {
            FileUtil.writeBytes(faces, avatarFile);
        }

        // 上传到 oss
        CosUtil.upload(avatarFile);

        BilibiliUser info = new BilibiliUser();
        info.setDedeuserid(userId);
        info.setUsername(baseInfo.getStr("name"));
        info.setLevel(baseInfo.getInt("level"));
        info.setIsLogin(false);

        return info;
    }

    /**
     * 获取勋章墙
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getMedalWall() {
        Map<String, String> params = new HashMap<>();
        params.put("target_id", config.getDedeuserid());
        return doGet(BilibiliAPI.GET_MEDAL_WALL, params);
    }

    /**
     * 检查Cookie有效性
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject checkCookie() {
        return doGet(BilibiliAPI.GET_USER_INFO_NAV, null);
    }

    /**
     * 查询硬币变化情况
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getCoinChangeLog() {
        return doGet(BilibiliAPI.GET_COIN_CHANGE_LOG);
    }

    /**
     * 查询每日奖励状态
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getExpRewardStatus() {
        return doGet(BilibiliAPI.GET_EXP_REWARD_STATUS);
    }

    /**
     * 获取已关注的UP最近发布的视频的BVID
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getFollowedUpPostVideo() {
        Map<String, String> params = new HashMap<>();
        params.put("uid", config.getDedeuserid());
        params.put("type_list", "8");
        params.put("from", "");
        params.put("platform", "web");

        return doGet(BilibiliAPI.GET_FOLLOWED_UP_POST_VIDEO, params);
    }

    /**
     * 根据分区ID获取3日热榜视频
     *
     * @param regionId 分区ID
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getTrendVideo(String regionId) {
        Map<String, String> params = new HashMap<>();
        params.put("rid", regionId);
        params.put("day", "3");

        return doGet(BilibiliAPI.GET_TREND_VIDEO, params);
    }

    /**
     * 观看视频
     *
     * @param bvid       视频的BVID
     * @param playedTime 播放时长，秒
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject playVideo(String bvid, int playedTime) {
        Map<String, String> params = new HashMap<>();
        params.put("bvid", bvid);
        params.put("played_time", String.valueOf(playedTime));
        return doPost(BilibiliAPI.REPORT_HEARTBEAT, params);
    }

    /**
     * 分享视频
     *
     * @param bvid 视频的BVID
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject shareVideo(String bvid) {
        Map<String, String> params = new HashMap<>();
        params.put("bvid", bvid);
        params.put("csrf", config.getBiliJct());

        return doPost(BilibiliAPI.SHARE_VIDEO, params);
    }


    /**
     * 漫画签到
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject mangaCheckIn(String platform) {
        Map<String, String> params = new HashMap<>();
        params.put("platform", platform);
        return doPost(BilibiliAPI.MANGA_SIGN, params);
    }

    /**
     * 获取今日通过投币获得的经验值
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getCoinExpToday() {
        return doGet(BilibiliAPI.GET_COIN_EXP_TODAY);
    }

    /**
     * 获取视频详细信息
     *
     * @param bvid 视频BVID
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getVideoDetails(String bvid) {
        Map<String, String> params = new HashMap<>();
        params.put("bvid", bvid);
        return doGet(BilibiliAPI.GET_VIDEO_DETAILS, params);
    }

    /**
     * 获取硬币账户余额
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getCoin() {
        return doGet(BilibiliAPI.GET_COIN);
    }


    /**
     * 判断视频是否被投币
     *
     * @param bvid 视频的bvid
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject checkDonateCoin(String bvid) {
        Map<String, String> params = new HashMap<>();
        params.put("bvid", bvid);
        return doGet(BilibiliAPI.CHECK_DONATE_COIN, params);
    }

    /**
     * 投币
     *
     * @param bvid    视频的bvid
     * @param numCoin 投币数
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject donateCoin(String bvid, int numCoin, int isLike) {
        Map<String, String> params = new HashMap<>();
        params.put("bvid", bvid);
        params.put("multiply", String.valueOf(numCoin));
        params.put("select_like", String.valueOf(isLike));
        params.put("cross_domain", "true");
        params.put("csrf", config.getBiliJct());

        Map<String, String> headers = new HashMap<>();
        headers.put("Referer", "https://www.bilibili.com/video/" + bvid);
        headers.put("Origin", "https://www.bilibili.com");

        return doPost(BilibiliAPI.DONATE_COIN, params, headers);
    }

    /**
     * 获取直播间钱包
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getLiveWallet() {
        return doGet(BilibiliAPI.BILI_LIVE_WALLET);
    }

    /**
     * 银瓜子兑换硬币
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject silver2Coin() {
        Map<String, String> params = new HashMap<>();
        params.put("csrf_token", config.getBiliJct());
        params.put("csrf", config.getBiliJct());

        return doPost(BilibiliAPI.SILVER_2_COIN, params);
    }

    /**
     * 直播签到
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject liveCheckIn() {
        return doGet(BilibiliAPI.BILI_LIVE_CHECK_IN);
    }

    /**
     * 获取背包中的礼物
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject listGifts() {
        return doGet(BilibiliAPI.LIST_GIFTS);
    }

    /**
     * 获取直播间信息
     *
     * @param userId 主播id
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getLiveRoomInfo(String userId) {
        Map<String, String> params = new HashMap<>();
        params.put("mid", userId);

        return doGet(BilibiliAPI.GET_LIVE_ROOM_INFO, params);
    }

    /**
     * 赠送直播间礼物
     *
     * @param userId  主播的uid
     * @param roomId  主播的房间id
     * @param bagId   背包id
     * @param giftId  礼物id
     * @param giftNum 礼物数量
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject donateGift(String userId, String roomId,
                                 String bagId, String giftId, int giftNum) {
        Map<String, String> params = new HashMap<>();
        params.put("biz_id", roomId);
        params.put("ruid", userId);
        params.put("gift_id", giftId);
        params.put("bag_id", bagId);
        params.put("gift_num", String.valueOf(giftNum));
        params.put("uid", config.getDedeuserid());
        params.put("csrf", config.getBiliJct());
        params.put("send_ruid", "0");
        params.put("storm_beat_id", "0");
        params.put("price", "0");
        params.put("platform", "pc");
        params.put("biz_code", "live");

        return doPost(BilibiliAPI.SEND_GIFT, params);
    }

    /**
     * 获取充电信息
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getChargeInfo() {
        Map<String, String> params = new HashMap<>();
        params.put("mid", config.getDedeuserid());

        return doGet(BilibiliAPI.GET_CHARGE_INFO, params);
    }

    /**
     * 充电
     *
     * @param couponBalance B币券金额
     * @param upUserId      充电对象的userId
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject doCharge(int couponBalance, String upUserId) {
        Map<String, String> params = new HashMap<>();
        params.put("bp_num", String.valueOf(couponBalance));
        params.put("is_bp_remains_prior", "true");
        params.put("up_mid", upUserId);
        params.put("otype", "up");
        params.put("oid", config.getDedeuserid());
        params.put("csrf", config.getBiliJct());

        return doPost(BilibiliAPI.CHARGE, params);
    }

    /**
     * 提交充电留言
     *
     * @param orderNo 充电订单号
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject doChargeComment(String orderNo) {
        Map<String, String> params = new HashMap<>();
        params.put("order_id", orderNo);
        params.put("message", "up的作品很棒");
        params.put("csrf", config.getBiliJct());

        return doPost(BilibiliAPI.COMMIT_CHARGE_COMMENT, params);
    }

    /**
     * 获取大会员漫画权益
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getMangaVipReward() {
        Map<String, String> params = new HashMap<>();
        params.put("reason_id", "1");

        return doPost(BilibiliAPI.GET_MANGA_VIP_REWARD, params);
    }

    /**
     * 领取大会员权益
     *
     * @param type 1 = B币券  2 = 会员购优惠券
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getVipReward(int type) {
        Map<String, String> params = new HashMap<>();
        params.put("type", String.valueOf(type));
        params.put("csrf", config.getBiliJct());

        return doPost(BilibiliAPI.GET_VIP_REWARD, params);
    }

    /**
     * 阅读漫画
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject readManga() {
        Map<String, String> params = new HashMap<>(4);
        params.put("device", "pc");
        params.put("platform", "web");
        params.put("comic_id", "26009");
        params.put("ep_id", "300318");

        return doPost(BilibiliAPI.READ_MANGA, params);
    }

    public JSONObject followUser(String uid) {
        Map<String, String> params = new HashMap<>();
        params.put("fid", uid);
        params.put("act", "1");
        params.put("re_src", "11");
        params.put("csrf", config.getBiliJct());

        return doPost(BilibiliAPI.RELATION_MODIFY, params);
    }

    /**
     * 获取B站用户头像文件流
     *
     * @param avatarUrl 头像地址
     * @return 头像文件流
     */
    private byte[] getAvatarStream(String avatarUrl) {
        URI uri;
        try {
            uri = new URIBuilder(avatarUrl).build();
        } catch (URISyntaxException e) {
            log.error("解析头像地址失败", e);
            return new byte[0];
        }
        HttpGet httpGet = new HttpGet(uri);

        try (CloseableHttpResponse response = HttpUtil.buildHttpClient().execute(httpGet)) {
            return EntityUtils.toByteArray(response.getEntity());
        } catch (Exception e) {
            log.error("获取头像文件流失败", e);
        }
        return new byte[0];
    }

    /**
     * 匿名处理
     *
     * @param username B站用户名
     * @return 使用*号覆盖后的用户名
     */
    @Deprecated
    private String coverUsername(String username) {
        StringBuilder sb = new StringBuilder();

        if (username.length() > 2) {
            // 当用户名长度大于2时，使中间的字符被*号覆盖
            for (int i = 0; i < username.length(); i++) {
                if (i > 0 && i < username.length() - 1) {
                    sb.append("*");
                } else {
                    sb.append(username.charAt(i));
                }
            }
        } else {
            // 当用户名长度不大于2时，只覆盖最后一个字符
            sb.append(username.charAt(0)).append("*");
        }

        return sb.toString();
    }

    private JSONObject doGet(String url) {
        return doGet(url, MapUtil.empty());
    }

    /**
     * 实际处理B站API访问
     *
     * @param url    访问API地址
     * @param params 查询字符串参数 {@link MultiValueMap}
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    private JSONObject doGet(String url, Map<String, String> params) {

        URI uri = HttpUtil.buildUri(url, params);

        HttpGet httpGet = new HttpGet(uri);
        httpGet.setHeader("User-Agent", config.getUserAgent());
        httpGet.setHeader("Connection", "keep-alive");
        httpGet.setHeader("Cookie", "bili_jct=" + config.getBiliJct() +
                ";SESSDATA=" + config.getSessdata() +
                ";DedeUserID=" + config.getDedeuserid() + ";");


        return call(url, params, httpGet);
    }

    private JSONObject doPost(String url, Map<String, String> params) {
        return doPost(url, params, null);
    }

    private JSONObject doPost(String url, Map<String, String> params, Map<String, String> headers) {
        URI uri = HttpUtil.buildUri(url, params);
        HttpPost httpPost = new HttpPost(uri);
        List<NameValuePair> formData = new ArrayList<>();
        params.forEach((key, value) -> formData.add(new BasicNameValuePair(key, value)));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formData, StandardCharsets.UTF_8);
        httpPost.setEntity(entity);

        httpPost.setHeader("Connection", "keep-alive");
        httpPost.setHeader("User-Agent", config.getUserAgent());
        httpPost.setHeader("Referer", "https://www.bilibili.com/");
        httpPost.setHeader("Cookie", "bili_jct=" + config.getBiliJct() +
                ";SESSDATA=" + config.getSessdata() +
                ";DedeUserID=" + config.getDedeuserid() + ";");
        if (headers != null) {
            headers.forEach(httpPost::setHeader);
        }
        return call(url, params, httpPost);
    }

    private JSONObject call(String url, Map<String, String> params, HttpUriRequest request) {
        try (CloseableHttpClient httpClient = HttpUtil.buildHttpClient(proxyHost, proxyPort);
             CloseableHttpResponse response = httpClient.execute(request)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            log.debug("==============");
            log.debug("请求 API: {}", url);
            log.debug("请求参数: {}", params);
            log.debug("响应结果: {}", responseBody);
            log.debug("==============");
            EntityUtils.consume(response.getEntity());
            return JSONUtil.parseObj(responseBody);
        } catch (Exception e) {
            log.error("请求API[{}]失败", url, e);
            throw new RequestException(url, e);
        }
    }
}
