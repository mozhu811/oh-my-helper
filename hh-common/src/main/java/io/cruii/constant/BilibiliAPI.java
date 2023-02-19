package io.cruii.constant;

/**
 * @author cruii
 * Created on 2021/08/13
 */
public interface BilibiliAPI {
    String GET_QR_CODE_LOGIN_URL_OLD = "https://passport.bilibili.com/qrcode/getLoginUrl";
    /**
     * 申请二维码
     */
    String GET_QR_CODE_LOGIN_URL = "https://passport.bilibili.com/x/passport-login/web/qrcode/generate";

    String GET_QR_CODE_LOGIN_INFO_URL_OLD = "https://passport.bilibili.com/qrcode/getLoginInfo";
    /**
     * 扫码登录
     */
    String GET_QR_CODE_LOGIN_INFO_URL = "https://passport.bilibili.com/x/passport-login/web/qrcode/poll";

    /**
     * 导航栏用户信息
     */
    String GET_USER_INFO_NAV = "https://api.bilibili.com/x/web-interface/nav";

    /**
     * 获取用户空间详细信息
     * 不需要登录
     */
    String GET_USER_SPACE_INFO = "https://api.bilibili.com/x/space/acc/info";

    /**
     * 获取用户空间详细信息
     * 需要登录
     */
    String GET_USER_SPACE_DETAILS_INFO = "https://api.bilibili.com/x/space/myinfo";
    /**
     * 查询硬币变化情况
     */
    String GET_COIN_CHANGE_LOG = "https://api.bilibili.com/x/member/web/coin/log";

    /**
     * 查询每日奖励状态
     */
    String GET_EXP_REWARD_STATUS = "https://api.bilibili.com/x/member/web/exp/reward";

    /**
     * 获取已关注的UP发布的视频动态列表
     */
    String GET_FOLLOWED_UP_POST_VIDEO = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/dynamic_new";

    /**
     * 根据分区获取热门视频
     */
    String GET_TREND_VIDEO = "https://api.bilibili.com/x/web-interface/ranking/region";

    /**
     * 上报视频播放心跳
     */
    String REPORT_HEARTBEAT = "https://api.bilibili.com/x/click-interface/web/heartbeat";

    String VIDEO_CLICK = "https://api.bilibili.com/x/click-interface/click/web/h5";

    /**
     * 获取视频详细信息
     */
    String GET_VIDEO_DETAILS = "https://api.bilibili.com/x/web-interface/view";

    /**
     * 分享视频
     */
    String SHARE_VIDEO = "https://api.bilibili.com/x/web-interface/share/add";

    /**
     * 漫画签到
     */
    String MANGA_SIGN = "https://manga.bilibili.com/twirp/activity.v1.Activity/ClockIn";

    /**
     * 获取今日通过投币获得的经验值
     */
    String GET_COIN_EXP_TODAY = "https://api.bilibili.com/x/web-interface/coin/today/exp";

    /**
     * 获取账户硬币余额
     */
    String GET_COIN = "https://account.bilibili.com/site/getCoin";

    /**
     * 判断视频是否被投币
     */
    String CHECK_DONATE_COIN = "https://api.bilibili.com/x/web-interface/archive/coins";

    /**
     * 投币
     */
    String DONATE_COIN = "https://api.bilibili.com/x/web-interface/coin/add";

    /**
     * 硬币换银瓜子.
     */
    String SILVER_2_COIN = "https://api.live.bilibili.com/xlive/revenue/v1/wallet/silver2coin";

    /**
     * 查询直播间钱包
     */
    String BILI_LIVE_WALLET = "https://api.live.bilibili.com/xlive/revenue/v1/wallet/myWallet?need_bp=1&need_metal=1&platform=pc";

    /**
     * 点赞视频
     */
    String LIKE_VIDEO = "https://api.bilibili.com/x/web-interface/archive/like";

    /**
     * 直播签到
     */
    String BILI_LIVE_CHECK_IN = "https://api.live.bilibili.com/xlive/web-ucenter/v1/sign/DoSign";

    /**
     * 获取礼物背包
     */
    String LIST_GIFTS = "https://api.live.bilibili.com/xlive/web-room/v1/gift/bag_list";

    /**
     * 获取直播间信息
     */
    @Deprecated
    String GET_LIVE_ROOM_INFO_OLD = "https://api.live.bilibili.com/room/v1/Room/getRoomInfoOld";

    String GET_LIVE_ROOM_INFO = "https://api.bilibili.com/x/space/acc/info";

    /**
     * 送出直播间礼物
     */
    String SEND_GIFT = "https://api.live.bilibili.com/gift/v2/live/bag_send";

    /**
     * 充电
     */
    String CHARGE = "https://api.bilibili.com/x/ugcpay/web/v2/trade/elec/pay/quick";

    /**
     * 获取充电信息
     */
    String GET_CHARGE_INFO = "https://api.bilibili.com/x/ugcpay/web/v2/trade/elec/panel";

    /**
     * 充电留言.
     */
    String COMMIT_CHARGE_COMMENT = "https://api.bilibili.com/x/ugcpay/trade/elec/message";

    /**
     * 领取大会员权益
     */
    String GET_VIP_REWARD = "https://api.bilibili.com/x/vip/privilege/receive";

    /**
     * 领取大会员漫画奖励
     * {
     *     "code": 0,
     *     "msg": "",
     *     "data": {
     *         "amount": 0,
     *         "id": 0
     *     }
     * }
     *
     * {
     *     "code": 1,
     *     "msg": "已经领取过该奖励或者未达到领取条件哦~"
     * }
     */
    String GET_MANGA_VIP_REWARD = "https://manga.bilibili.com/twirp/user.v1.User/GetVipReward";

    /**
     * 阅读漫画
     * {"code":0, "msg":"", "data":{}}
     */
    String READ_MANGA = "https://manga.bilibili.com/twirp/bookshelf.v1.Bookshelf/AddHistory?device=pc&platform=web";

    /**
     * 获取勋章墙
     */
    String GET_MEDAL_WALL = "https://api.live.bilibili.com/xlive/web-ucenter/user/MedalWall";

    /**
     * 操作用户关系
     */
    String RELATION_MODIFY = "https://api.bilibili.com/x/relation/modify";

    String GET_JURY_CASE = "https://api.bilibili.com/x/credit/v2/jury/case/next";

    String JURY_VOTE = "https://api.bilibili.com/x/credit/v2/jury/vote";

    String VIP_SIGN = "https://api.bilibili.com/pgc/activity/score/task/sign";

    /**
     * 大会员接任务
     *
     * {"code":0,"data":{},"message":"success"}
     */
    String VIP_QUEST_RECEIVE = "https://api.biliapi.com/pgc/activity/score/task/receive";

    /**
     * 大会员任务信息
     *
     * @see io.cruii.model.BigVipTaskCombine
     */
    String VIP_QUEST_INFO = "https://api.biliapi.com/x/vip_point/task/combine";

    /**
     * 完成大会员任务
     *
     * {"code":0,"message":"success"}
     */
    String VIP_QUEST_VIEW_COMPLETE = "https://api.bilibili.com/pgc/activity/deliver/task/complete";

    /**
     * 完成大会员每日任务
     *
     * {"code":0,"message":"success"}
     */
    String VIP_QUEST_COMPLETE = "https://api.bilibili.com/pgc/activity/score/task/complete";

    String GET_QUESTIONS = "https://api.bilibili.com/x/esports/guess/collection/question";

    String GET_GUESS_INFO = "https://api.bilibili.com/x/esports/guess/collection/statis";

    String GUESS_ADD = "https://api.bilibili.com/x/esports/guess/add";
}
