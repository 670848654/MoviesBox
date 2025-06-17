package my.project.moviesbox.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.fourthline.cling.model.meta.Device;

import java.util.List;

import my.project.moviesbox.R;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: UpnpDevicesAdapter
  * @描述: 投屏设备列表适配器
  * @作者: Li Z
  * @日期: 2024/3/10 22:02
  * @版本: 1.0
 */
public class UpnpDevicesAdapter extends BaseQuickAdapter<Device, BaseViewHolder> {
    public UpnpDevicesAdapter(List<Device> data) {
        super(R.layout.item_device, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Device clingDevice) {
        helper.setText(R.id.title, clingDevice.getDetails().getFriendlyName());
    }
}
