export const shortcuts = [
    {
        text: '今日',
        value() {
            var end = new Date();
            var start = new Date();
            end.setTime(start.getTime());
            start.setHours(0, 0, 0);
            end.setHours(23, 59, 59);
            return [start, end];
        }
    },
    {
        text: '本周',
        value() {
            var now = new Date(); // 当前日期
            var nowDayOfWeek = now.getDay(); // 今天本周的第几天
            var nowDay = now.getDate(); // 当前日
            var nowMonth = now.getMonth(); // 当前月
            var day = nowDayOfWeek || 7;
            var start = new Date(now.getFullYear(), nowMonth, nowDay + 1 - day);
            var end = new Date(now.getFullYear(), nowMonth, nowDay + 7 - day);
            start.setTime(start.getTime());
            end.setTime(end.getTime());
            start.setHours(0, 0, 0);
            end.setHours(23, 59, 59);
            return [start, end];
        }
    },
    {
        text: '本月',
        value() {
            var now = new Date(); // 当前日期
            var nowYear = now.getYear(); // 当前年
            nowYear += (nowYear < 2000) ? 1900 : 0;
            var nowMonth = now.getMonth(); // 当前月
            // 本月天数
            var monthStartDate = new Date(nowYear, nowMonth, 1);
            var monthEndDate = new Date(nowYear, nowMonth + 1, 1);
            var days = (monthEndDate - monthStartDate) / (1000 * 60 * 60 * 24);
            var start = new Date(nowYear, nowMonth, 1);
            var end = new Date(nowYear, nowMonth, days);
            start.setTime(start.getTime());
            end.setTime(end.getTime());
            start.setHours(0, 0, 0);
            end.setHours(23, 59, 59);
            return [start, end];
        }
    },
    {
        text: '上周',
        value() {
            var now = new Date(); // 当前日期
            var nowDayOfWeek = now.getDay(); // 今天本周的第几天
            var nowDay = now.getDate(); // 当前日
            var nowMonth = now.getMonth(); // 当前月
            var day = nowDayOfWeek || 7;
            var start = new Date(now.getFullYear(), nowMonth, nowDay + 1 - day);
            var end = new Date(now.getFullYear(), nowMonth, nowDay + 7 - day);
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
            end.setTime(end.getTime() - 3600 * 1000 * 24 * 7);
            start.setHours(0, 0, 0);
            end.setHours(23, 59, 59);
            return [start, end];
        }
    },
    {
        text: '上月',
        value() {
            var now = new Date(); // 当前日期
            var nowYear = now.getYear(); // 当前年
            nowYear += (nowYear < 2000) ? 1900 : 0;
            var nowMonth = now.getMonth() - 1; // 上月
            if (nowMonth < 0) {
                nowMonth = 11;
                nowYear -= 1;
            }
            // 本月天数
            var monthStartDate = new Date(nowYear, nowMonth, 1);
            var monthEndDate = new Date(nowYear, nowMonth + 1, 1);
            var days = (monthEndDate - monthStartDate) / (1000 * 60 * 60 * 24);
            var start = new Date(nowYear, nowMonth, 1);
            var end = new Date(nowYear, nowMonth, days);
            start.setTime(start.getTime());
            end.setTime(end.getTime());
            start.setHours(0, 0, 0);
            end.setHours(23, 59, 59);
            return [start, end];
        }
    },
]
