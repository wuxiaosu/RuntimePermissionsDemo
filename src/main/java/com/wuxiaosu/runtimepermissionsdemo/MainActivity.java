package com.wuxiaosu.runtimepermissionsdemo;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final static private String TAG = "RuntimePermissionsDemo";
    private Button mBtnCallPhone;
    private Button mBtnLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mBtnCallPhone = (Button) findViewById(R.id.btn_call_phone);
        mBtnLocation = (Button) findViewById(R.id.btn_location);

        mBtnCallPhone.setOnClickListener(this);
        mBtnLocation.setOnClickListener(this);
    }

    @Override
    public void onClick(@NonNull View v) {
        switch (v.getId()) {
            case R.id.btn_call_phone:
                MainActivityPermissionsDispatcher.showCallPhoneWithCheck(this);
                break;
            case R.id.btn_location:
                MainActivityPermissionsDispatcher.showLocationWithCheck(this);
                break;
        }
    }


    @NeedsPermission(Manifest.permission.CALL_PHONE)
    void showCallPhone() {
        Log.e(TAG, "========================== >> 打电话");
        //打电话
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "10010"));
        startActivity(intent);
    }

    @NeedsPermission({Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE})
    void showLocation() {
        Log.e(TAG, "========================== >> 定位");
        //定位
        Toast.makeText(this, "TODO: 定位", Toast.LENGTH_LONG).show();
    }

    /**
     * 向用户展示申请权限的理由
     *
     * @param request
     */
    @OnShowRationale({Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE})
    void showRationaleForLocation(PermissionRequest request) {
        Log.e(TAG, "showRationaleForLocation: ===================== >> 展示申请理由");
        showRationaleDialog(R.string.permission_location_rationale, request);
    }

    /**
     * 权限被拒绝
     */
    @OnPermissionDenied({Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE})
    void onLocationDenied() {
        Log.e(TAG, "onLocationDenied: ===================== >> 权限被拒绝");
        Toast.makeText(this, R.string.permission_location_denied, Toast.LENGTH_SHORT).show();
    }

    /**
     * 向用户展示申请权限的理由
     *
     * @param request
     */
    @OnShowRationale(Manifest.permission.CALL_PHONE)
    void showRationaleForCallPhone(PermissionRequest request) {
        Log.e(TAG, "showRationaleForCallPhone: ===================== >> 展示申请理由");
        showRationaleDialog(R.string.permission_call_phone_rationale, request);
    }

    /**
     * 权限被拒绝
     */
    @OnPermissionDenied(Manifest.permission.CALL_PHONE)
    void onCallPhoneDenied() {
        Log.e(TAG, "onCallPhoneDenied: ===================== >> 权限被拒绝");
        Toast.makeText(this, R.string.permission_call_phone_denied, Toast.LENGTH_SHORT).show();
    }

    /**
     * 用户勾选了不再询问
     */
    @OnNeverAskAgain(Manifest.permission.CALL_PHONE)
    void onCallPhoneNeverAskAgain() {
        Log.e(TAG, "onCallPhoneNeverAskAgain: ===================== >> 被设置不再询问");
//        Toast.makeText(this, R.string.permission_call_phone_never_askagain, Toast.LENGTH_SHORT).show();
        //给一个弱提示 或者弹出对话框让用户去设置界面
        new AlertDialog.Builder(this)
                .setPositiveButton("去啊,去啊", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        sendSettingsIntent(MainActivity.this);
                    }
                })
                .setNegativeButton("不去,滚", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                    }
                })
                .setCancelable(false)
                .setMessage("是不是现在去设置权限?")
                .show();
    }

    private void showRationaleDialog(@StringRes int messageResId, final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setPositiveButton(R.string.button_allow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(R.string.button_deny, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(messageResId)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    /**
     * 显示应用信息界面 (去设置权限)
     *
     * @param context
     * @return 有可能打开失败
     */
    public static boolean sendSettingsIntent(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
