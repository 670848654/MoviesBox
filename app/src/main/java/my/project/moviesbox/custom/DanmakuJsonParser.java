package my.project.moviesbox.custom;

import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONObject;

import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.android.JSONSource;

/**
  * @包名: my.project.moviesbox.custom
  * @类名: JsonDanmakuParser
  * @描述: 自定义JSON格式弹幕解析示例
  * @作者: Li Z
  * @日期: 2024/1/22 20:05
  * @版本: 1.0
 */
public class DanmakuJsonParser extends BaseDanmakuParser {
    @Override
    protected IDanmakus parse() {
        if (mDataSource != null && mDataSource instanceof JSONSource) {
            JSONSource jsonSource = (JSONSource) mDataSource;
            return doParse(jsonSource.data());
        }
        return new Danmakus();
    }

    /**
     * @param danmakuDara 弹幕数据
     * @return
     */
    private Danmakus doParse(JSONArray danmakuDara) {
        Danmakus danmakus = new Danmakus();
        if (danmakuDara == null || danmakuDara.length() == 0) {
            return danmakus;
        }
        danmakus = _parse(danmakuDara, danmakus);
        return danmakus;
    }

    private Danmakus _parse(JSONArray jsonArray, Danmakus danmakus) {
        if (danmakus == null) {
            danmakus = new Danmakus();
        }
        try {
            if (jsonArray == null || jsonArray.length() == 0) {
                return danmakus;
            }
            for (int i = 0, size = jsonArray.length(); i < size; i++) {
                JSONObject danmuObj = jsonArray.getJSONObject(i);
                long time = (long) (Float.parseFloat(danmuObj.getString("time"))); // 出现时间
                int color = Color.WHITE;
                String colorStr = danmuObj.getString("color");
                if (colorStr.length() == 7)
                    color = Color.parseColor(colorStr);
                int danmuType = Integer.parseInt(danmuObj.getString("type"));
                int textSize = Integer.parseInt(danmuObj.getString("textSize"));
                BaseDanmaku item = mContext.mDanmakuFactory.createDanmaku(danmuType, mContext);
                if (item != null) {
                    item.setTime(time + i* 500L);
                    item.textColor = color;
                    item.textShadowColor = color <= Color.BLACK ? Color.WHITE : Color.BLACK;
                    item.index = i;
                    item.flags = mContext.mGlobalFlagValues;
                    item.setTimer(mTimer);
                    item.text = danmuObj.getString("text");
//                    item.textShadowColor = Color.GRAY;
                    item.underlineColor = Color.TRANSPARENT;
                    item.borderColor = Color.TRANSPARENT;
                    item.priority = 0;
//                    item.textSize = Utils.dpToPx(Utils.getContext(), 14);
                    item.textSize = textSize * (mDispDensity - 0.6f);
                    danmakus.addItem(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return danmakus;
    }
}
