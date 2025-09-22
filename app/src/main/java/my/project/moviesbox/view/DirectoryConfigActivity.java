package my.project.moviesbox.view;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.WindowManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import my.project.moviesbox.R;
import my.project.moviesbox.database.manager.TDirectoryManager;
import my.project.moviesbox.databinding.DialogDirectoryBinding;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.utils.Utils;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/11/1 17:14
 */
public class DirectoryConfigActivity extends DirectoryActivity {
    @Override
    protected void getData() {
        super.getData();
    }

    @Override
    protected void initAdapter() {
        super.initAdapter();
        directoryAdapter.addChildClickViewIds(R.id.edit, R.id.delete);
        directoryAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            Utils.setVibration(view);
            String directoryId = list.get(position).getId();
            String name = list.get(position).getName();
            String type = list.get(position).getType();
            int source = list.get(position).getSource();
            switch (view.getId()) {
                case R.id.edit:
                    editDirectory(position, name, type, source);
                    break;
                case R.id.delete:
                    deleteDirectory(position, directoryId, type);
                    break;
            }
        });
    }

    @Override
    public void onCreateResult(String directoryId) {
        directoryAdapter.addData(1, TDirectoryManager.queryById(directoryId, true));
        for (int i=0,size=directoryAdapter.getData().size(); i<size; i++) {
            directoryAdapter.notifyItemChanged(i);
        }
    }

    @Override
    protected void closeActivity(String directoryId) {

    }

    /**
     * 重命名
     * @param position
     * @param name
     * @param type
     * @param source
     */
    private void editDirectory(int position, String name, String type, int source) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.DialogStyle);
        builder.setTitle(getString(R.string.editListName));
        DialogDirectoryBinding dialogBinding = DialogDirectoryBinding.inflate(LayoutInflater.from(this));
        TextInputLayout textInputLayout = dialogBinding.name;
        textInputLayout.getEditText().setText(name);
        textInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                textInputLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        textInputLayout.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                Objects.requireNonNull(alertDialog.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        });
        builder.setPositiveButton("修改", (dialog, which) -> {
            String text = textInputLayout.getEditText().getText().toString().replaceAll(" ", "");
            if (!Utils.isNullOrEmpty(text)) {
                boolean nameExist = TDirectoryManager.checkNameExist(text, source, type);
                if (nameExist || text.equals(getString(R.string.defaultList))) {
                    textInputLayout.setError(getString(R.string.listNameAlreadyExists));
                    application.showToastMsg(getString(R.string.listNameAlreadyExists), DialogXTipEnum.ERROR);
                } else {
                    list.get(position).setName(text);
                    TDirectoryManager.update(list.get(position));
                    application.showToastMsg(getString(R.string.listRenameComplete), DialogXTipEnum.SUCCESS);
                    directoryAdapter.notifyItemChanged(position);
                    dialog.dismiss();
                }
            }
            else {
                textInputLayout.setError(getString(R.string.listNameCannotBeEmpty));
                application.showToastMsg(getString(R.string.listNameCannotBeEmpty), DialogXTipEnum.ERROR);
            }
        });
        builder.setNegativeButton(getString(R.string.defaultNegativeBtnText), null);
        builder.setCancelable(false);
        alertDialog = builder.setView(dialogBinding.getRoot()).create();
        alertDialog.show();
        Utils.dialogSetRenderEffect(this);
        alertDialog.setOnDismissListener(dialog -> Utils.dialogClearRenderEffect(this));
    }

    /**
     * 删除
     * @param position
     * @param directoryId
     * @param type
     */
    private void deleteDirectory(int position, String directoryId, String type) {
        Utils.showAlert(this,
                R.drawable.round_warning_24,
                getString(R.string.otherOperation),
                getString(R.string.listDeletePrompt),
                true,
                getString(R.string.defaultPositiveBtnText),
                getString(R.string.defaultNegativeBtnText),
                null,
                (dialogInterface, i) -> {
                    TDirectoryManager.delete(directoryId, type);
                    application.showToastMsg(getString(R.string.listDeleteComplete), DialogXTipEnum.SUCCESS);
                    directoryAdapter.removeAt(position);
                },
                (dialogInterface, i) -> dialogInterface.dismiss(),
                null);
    }

    @Override
    protected void onDestroy() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("type", type);
        setResult(DIRECTORY_CONFIG_RESULT_CODE, resultIntent);
        finish();
        super.onDestroy();
    }
}
