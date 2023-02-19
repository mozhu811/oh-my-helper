package io.cruii.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author cruii
 * Created on 2023/2/16
 */
@NoArgsConstructor
@Data
public class BigVipTaskCombine {
    private VipInfo vipInfo;
    private PointInfo pointInfo;
    private TaskInfo taskInfo;
    private Integer currentTs;

    @NoArgsConstructor
    @Data
    public static class VipInfo {
        private Integer type;
        private Integer status;
        private Long dueDate;
        private Integer vipPayType;
        private Integer startTime;
        private Integer paidType;
        private Integer mid;
        private Integer role;
        private Integer tvVipStatus;
        private Integer tvVipPayType;
        private Integer tvDueDate;
    }

    @NoArgsConstructor
    @Data
    public static class PointInfo {
        private Integer point;
        private Integer expirePoint;
        private Integer expireTime;
        private Integer expireDays;
    }

    @NoArgsConstructor
    @Data
    public static class TaskInfo {
        private List<Modules> modules;
        private SingTaskItem singTaskItem;
        private Integer scoreMonth;
        private Integer scoreLimit;

        @NoArgsConstructor
        @Data
        public static class SingTaskItem {
            private List<Histories> histories;
            private Integer count;
            private Integer baseScore;

            @NoArgsConstructor
            @Data
            public static class Histories {
                private String day;
                private Boolean signed;
                private Integer score;
                private Boolean isToday;
            }
        }

        @NoArgsConstructor
        @Data
        public static class Modules {
            private String moduleTitle;
            private List<CommonTaskItem> commonTaskItem;
            private String moduleSubTitle;

            @NoArgsConstructor
            @Data
            public static class CommonTaskItem {
                private String taskCode;
                private Integer state;
                private String title;
                private String icon;
                private String subtitle;
                private String explain;
                private Integer vipLimit;
                private Integer completeTimes;
                private Integer maxTimes;
                private Integer recallNum;
            }
        }
    }
}
