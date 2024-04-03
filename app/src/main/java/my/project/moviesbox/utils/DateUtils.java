package my.project.moviesbox.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import my.project.moviesbox.R;

/**
  * @包名: my.project.moviesbox.utils
  * @类名: DateUtils
  * @描述: 时间相关工具类
  * @作者: Li Z
  * @日期: 2024/2/4 17:06
  * @版本: 1.0
 */
public class DateUtils {

    /**
     * 获取当前时间为昨天/今天？
     * @param updateTime
     * @return
     * @throws ParseException
     */
    public static String isYesterday (Date updateTime) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat format1 = new SimpleDateFormat("HH:mm");
        SimpleDateFormat format2 = new SimpleDateFormat("MM-dd HH:mm");
        SimpleDateFormat format3 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String todayStr = format.format(new Date());
        Date today = format.parse(todayStr);

        if((today.getTime()-updateTime.getTime())>0 && (today.getTime()-updateTime.getTime())<=86400000) {
            return Utils.getString(R.string.yesterday) + format1.format(updateTime);
        }
        else if((today.getTime()-updateTime.getTime())<=0){
            return Utils.getString(R.string.today) + format1.format(updateTime);
        }
        else{
            Calendar c1 = Calendar.getInstance();
            c1.setTime(new Date());
            Calendar c2 = Calendar.getInstance();
            c2.setTime(updateTime);
            boolean sameYear = c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR);
            return sameYear ? format2.format(updateTime) : format3.format(updateTime);
        }
    }

    /**
     * 获取当前日期是星期几
     * @param dt
     * @return + 1 当前日期是星期几
     */
    public static int getWeekOfDate(Date dt) {
        int[] weekDays = {6, 0, 1, 2, 3, 4, 5};
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekDays[w];
    }

    /**
     * 获取当前年份
     * @return
     */
    public static String getNowYear() {
        return String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
    }

    /**
     * 获取
     * @return
     */
    public int getNowTime() {
        return Integer.parseInt(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
    }
}
