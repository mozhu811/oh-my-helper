package io.cruii.bilibili.component;

import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tencentcloudapi.scf.v20180416.models.Function;
import com.tencentcloudapi.scf.v20180416.models.ListFunctionsResponse;
import io.cruii.bilibili.config.TencentApiConfig;
import io.cruii.bilibili.util.ScfUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.HttpCookie;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2021/6/12
 */
@Log4j2
@Component
public class GameBettor {

    public TencentApiConfig apiConfig;

    public GameBettor(TencentApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    public static final String QUESTIONS_API = "https://api.bilibili.com/x/esports/guess/collection/question";

    public static final int REQUEST_COINS = 10;

    @Scheduled(cron = "0 10 0 * * ?")
    public void bet() throws InterruptedException {
        // 获取所有用户提交的cookie
        ListFunctionsResponse listFunctionsResponse = ScfUtil.listFunctions(apiConfig);
        List<String[]> cookies = Arrays.stream(listFunctionsResponse.getFunctions())
                .parallel()
                .map(Function::getDescription)
                .map(s -> s.split(";"))
                .filter(cookie -> {
                    HttpCookie sessdataCookie = new HttpCookie("SESSDATA", cookie[1]);
                    HttpCookie dedeUserID = new HttpCookie("DedeUserID", cookie[0]);
                    String coinResp = HttpRequest.get("https://account.bilibili.com/site/getCoin")
                            .cookie(sessdataCookie, dedeUserID)
                            .execute().body();
                    Double coins = null;
                    if (JSONUtil.isJsonObj(coinResp)) {
                        JSONObject coinData = JSONUtil.parseObj(coinResp).getJSONObject("data");

                        coins = coinData.getDouble("money");
                        if (coins < 300d) {
                            log.info("用户[{}]硬币不足300，将不执行赛事预测", cookie[0]);
                        }
                    }
                    return coins != null && coins > 300d;
                }).collect(Collectors.toList());

        // 登录
        login(cookies);

        // 查询当日竞猜
        JSONArray contestsAndQuestions = queryQuestions().getJSONArray("list");
        if (contestsAndQuestions.isEmpty()) {
            log.info("本日赛事预测已经截止");
            return;
        }

        for (int i = 0; i < contestsAndQuestions.size(); i++) {
            log.info(">>>>>>>>>> 开始获取今日第{}场赛事信息 <<<<<<<<<<", i + 1);
            JSONObject jsonObject = JSONUtil.parseObj(contestsAndQuestions.get(i));
            JSONObject contest = jsonObject.getJSONObject("contest");
            int contestId = contest.getInt("id");
            String contestGameStage = contest.getStr("game_stage");
            JSONObject question = JSONUtil.parseObj(jsonObject.getJSONArray("questions").get(0));
            int questionId = question.getInt("id");
            String title = question.getStr("title");
            String seasonName = contest.getByPath("season.title", String.class);
            log.info(seasonName + " " + contestGameStage + ":" + title);

            JSONObject blueTeam = JSONUtil.parseObj(question.getJSONArray("details").get(0));
            JSONObject redTeam = JSONUtil.parseObj(question.getJSONArray("details").get(1));

            Double oddsBlue = blueTeam.getDouble("odds");
            Double oddsRed = redTeam.getDouble("odds");

            JSONObject selected = oddsBlue >= oddsRed ? redTeam : blueTeam;
            log.info("投注队伍: {}，当前赔率收益: {}", selected.getStr("option"), selected.getDouble("odds") * REQUEST_COINS);
            doPredict(contestId, questionId, selected.getInt("detail_id"), cookies);
            taskSuspend();
        }
    }

    private void doPredict(int contestId, int questionId, int selectedTeamId, List<String[]> cookies) {
        log.info(">>>>>>>>>> 开始进行投注 <<<<<<<<<<");
        cookies.parallelStream().forEach(cookie -> {
            String requestBody = "oid=" + contestId +
                    "&main_id=" + questionId
                    + "&detail_id=" + selectedTeamId
                    + "&count=" + 10
                    + "&is_fav=0"
                    + "&csrf=" + cookie[2];
            String betResponse = HttpRequest.post("https://api.bilibili.com/x/esports/guess/add")
                    .header(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_2_3) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36 Edg/89.0.774.54")
                    .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                    .header(HttpHeaders.REFERER, "https://www.bilibili.com/")
                    .header(HttpHeaders.CONNECTION, "keep-alive")
                    .body(requestBody)
                    .cookie(new HttpCookie("DedeUserID", cookie[0]),
                            new HttpCookie("SESSDATA", cookie[1]),
                            new HttpCookie("bili_jct", cookie[2]))
                    .execute().body();

            JSONObject betRespJson = JSONUtil.parseObj(betResponse);
            if (betRespJson.getInt("code") == 0) {
                log.info("用户[{}]投注成功", cookie[0]);
            } else {
                log.error("用户[{}]{}", cookie[0], betRespJson.getStr("message"));
            }
        });
    }

    private void login(List<String[]> cookies) {
        log.info(">>>>>>>>>> 执行用户登录 <<<<<<<<<<");
        cookies.parallelStream().forEach(cookie -> {
            HttpCookie sessdata = new HttpCookie("SESSDATA", cookie[1]);
            JSONObject loginResponse = JSONUtil.parseObj(HttpRequest.get("https://api.bilibili.com/x/web-interface/nav")
                    .header(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_2_3) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36 Edg/89.0.774.54")
                    .cookie(sessdata)
                    .execute().body());
            if (loginResponse.getInt("code") != 0) {
                log.error("用户[{}]登录失败", cookie[0]);
            }
        });
    }

    private void taskSuspend() throws InterruptedException {
        Random random = new Random();
        int sleepTime = (int) ((random.nextDouble() + 0.5) * 3000);
        log.info(">>>>>>>>>> 随机暂停{}ms <<<<<<<<<<\n", sleepTime);
        Thread.sleep(sleepTime);
    }

    private JSONObject queryQuestions() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String gid = "";
        String sids = "";

        String url = UriComponentsBuilder.fromHttpUrl(QUESTIONS_API)
                .queryParam("pn", 1)
                .queryParam("ps", 50)
                .queryParam("gid", gid)
                .queryParam("sids", sids)
                .queryParam("stime", today + " 00:00:00")
                .queryParam("etime", today + " 23:59:59")
                .queryParam("pn", 1)
                .queryParam("ps", 50)
                .queryParam("stime", today + "+00:00:00")
                .queryParam("etime", today + "+23:59:59").toUriString();

        String response = HttpRequest.get(URLUtil.normalize(url, true))
                .header(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_2_3) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36 Edg/89.0.774.54")
                .execute().body();
        JSONObject responseJson = JSONUtil.parseObj(response);
        if (0 != responseJson.getInt("code")) {
            throw new RuntimeException("获取赛事信息失败");
        }
        return responseJson.getJSONObject("data");
    }
}
