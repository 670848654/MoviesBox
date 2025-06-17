package my.project.moviesbox.view;

import android.app.Activity;
import android.content.Intent;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/11/1 17:14
 */
public class DirectoryChangeActivity extends DirectoryActivity {
    private int position;

    @Override
    protected void getData() {
        super.getData();
        position = getIntent().getIntExtra("position", position);
    }

    @Override
    public void onCreateResult(String directoryId) {

    }

    @Override
    protected void closeActivity(String directoryId) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("type", type);
        resultIntent.putExtra("directoryId", directoryId);
        resultIntent.putExtra("position", position);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
