package my.project.moviesbox.contract;

/**
  * @包名: my.project.moviesbox.contract
  * @类名: BaseView
  * @描述: 通用视图回调接口
  * @作者: Li Z
  * @日期: 2024/1/22 19:51
  * @版本: 1.0
 */
public interface BaseView {
     /**
      * @方法名称: loadingView
      * @方法描述: 用于显示加载中视图
      * @日期: 2024/1/22 19:52
      * @作者: Li Z
      * @param 
      * @return 
      */
    void loadingView();

     /**
      * @方法名称: errorView
      * @方法描述: 用于显示加载失败视图
      * @日期: 2024/1/22 19:52
      * @作者: Li Z
      * @param msg 错误文本信息
      * @return 
      */
    void errorView(String msg);

     /**
      * @方法名称: emptyView
      * @方法描述: 用于显示空数据视图
      * @日期: 2024/1/22 19:52
      * @作者: Li Z
      * @param 
      * @return 
      */
    void emptyView();
}
