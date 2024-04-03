package my.project.moviesbox.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.cling.entity.ClingDevice;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: UpnpDevicesAdapter
  * @描述: 投屏设备列表适配器
  * @作者: Li Z
  * @日期: 2024/3/10 22:02
  * @版本: 1.0
 */
public class UpnpDevicesAdapter extends BaseQuickAdapter<ClingDevice, BaseViewHolder> {
    public UpnpDevicesAdapter(List<ClingDevice> data) {
        super(R.layout.item_device, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, ClingDevice clingDevice) {
        helper.setText(R.id.title, clingDevice.getDevice().getDetails().getFriendlyName());
    }
}
