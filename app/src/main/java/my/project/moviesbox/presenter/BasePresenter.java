package my.project.moviesbox.presenter;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2023/12/30 22:58
 */
public interface BasePresenter {
    /**
     * 加载数据
     * @param firstTimeData 是否第一次加载数据
     */
    void loadData(boolean firstTimeData);
}
