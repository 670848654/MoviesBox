package my.project.moviesbox.contract;

/**
  * @包名: my.project.moviesbox.contract
  * @类名: BaseLoadDataCallback
  * @描述: 通用回调接口
  * @作者: Li Z
  * @日期: 2024/1/22 19:51
  * @版本: 1.0
 */
public interface BaseLoadDataCallback {
     /**
      * @方法名称: error
      * @方法描述: 失败回调方法
      * @日期: 2024/1/22 19:51
      * @作者: Li Z
      * @param msg 错误信息
      * @return
      */
    void error(String msg);

}