# RuntimePermissionsDemo
基于PermissionsDispatcher的动态权限demo，PermissionsDispatcher地址https://github.com/hotchemi/PermissionsDispatcher   
（部分定制rom弹出请求权限提示框时体验有些许不同，例如华为emui）  
# 效果图
![00.gif](https://raw.githubusercontent.com/wuxiaosu/RuntimePermissionsDemo/master/screenshorts/00.gif)
# 快速配置  
## 引入jar包 

```
dependencies {
  compile('com.github.hotchemi:permissionsdispatcher:${latest.version}') {
      // if you don't use android.app.Fragment you can exclude support for them
      exclude module: "support-v13"
  }
  annotationProcessor 'com.github.hotchemi:permissionsdispatcher-processor:${latest.version}'
}
```  
# 开始使用  
## 1.添加注解  
注解列表（总共也只有这几个注解）：  
> Tip：注解的方法不能为 private

注解 | 必须 | 说明
---|---|---
@RuntimePermissions | √ | 注册，表示在Activity或Fragment中处理动态权限
@NeedsPermission | √ | 注释一个执行时需要一个或多个权限的方法
@OnShowRationale | | 注释一个说明方法，该方法说明为什么需要权限，该方法通过一个 **PermissionRequest** 对象可以终止或继续此权限请求
@OnPermissionDenied | | 当用户拒绝权限时调用此注解注释的方法
@OnNeverAskAgain | | 当用户勾选了“不再询问”后又调用检测权限时调用此注解注释的方法

```
@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    @NeedsPermission(Manifest.permission.CAMERA)
    void showCamera() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.sample_content_fragment, CameraPreviewFragment.newInstance())
                .addToBackStack("camera")
                .commitAllowingStateLoss();
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    void showRationaleForCamera(final PermissionRequest request) {
        new AlertDialog.Builder(this)
            .setMessage(R.string.permission_camera_rationale)
            .setPositiveButton(R.string.button_allow, (dialog, button) -> request.proceed())
            .setNegativeButton(R.string.button_deny, (dialog, button) -> request.cancel())
            .show();
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    void showDeniedForCamera() {
        Toast.makeText(this, R.string.permission_camera_denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    void showNeverAskForCamera() {
        Toast.makeText(this, R.string.permission_camera_neverask, Toast.LENGTH_SHORT).show();
    }
}
```
## 2.生成帮助类  
在编译时， PermissionsDispatcher会产生一个类 **Mainactivitypermissionsdispatcher** ([Activity Name] + permissionsdispatcher)，你可以用它来安全地访问这些权限保护方法。酱紫：

```
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    findViewById(R.id.button_camera).setOnClickListener(v -> {
      // NOTE: delegate the permission handling to generated method
      MainActivityPermissionsDispatcher.showCameraWithCheck(this);
    });
    findViewById(R.id.button_contacts).setOnClickListener(v -> {
      // NOTE: delegate the permission handling to generated method
      MainActivityPermissionsDispatcher.showContactsWithCheck(this);
    });
}

@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    // NOTE: delegate the permission handling to generated method
    MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
}
```
## 如果你使用了 AndroidAnnotations  
你需要添加 [AndroidAnnotationsPermissionsDispatcherPlugin](https://github.com/AleksanderMielczarek/AndroidAnnotationsPermissionsDispatcherPlugin) 的依赖：  
在项目project的 **build.gradle** 中添加  

```
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```
在项目moudle的 **build.gradle** 中添加 

```
dependencies {
	//PermissionsDispatcher has to be above AndroidAnnotations
    compile 'com.github.hotchemi:permissionsdispatcher:2.1.3'
    apt 'com.github.hotchemi:permissionsdispatcher-processor:2.1.3'
    compile 'org.androidannotations:androidannotations-api:4.0.0'
    apt 'org.androidannotations:androidannotations:4.0.0'
    apt 'com.github.AleksanderMielczarek:AndroidAnnotationsPermissionsDispatcherPlugin:0.1.0'
}
```
更多详情：https://github.com/hotchemi/PermissionsDispatcher   
# 附录
危险权限和权限组  
如果设备运行的是 Android 6.0（API 级别 23）或更高版本，并且应用的 targetSdkVersion 是 23 或更高版本，则应用在运行时需要向用户请求权限（危险权限，即可能影响用户隐私或设备正常操作的权限）。用户授权同一危险权限组内的权限，系统会自动授权该权限组内的其他权限。

权限组 | 权限 | 描述
---|---|---
CALENDAR（日历） | READ_CALENDAR（读取日程提醒）| 允许程序读取用户的日程信息
|| WRITE_CALENDAR（写入日程提醒）| 写入日程,但不可读取
CAMERA（相机） | CAMERA（拍照权限）| 允许访问摄像头进行拍照
CONTACTS（联系人） | READ_CONTACTS（读取联系人）| 允许应用访问联系人通讯录信息
|| WRITE_CONTACTS（写入联系人）| 写入联系人,但不可读取 
|| GET_ACCOUNTS（访问账户Gmail列表）| 访问GMail账户列表
LOCATION（定位）| ACCESS_FINE_LOCATION（获取精确位置）| 通过GPS芯片接收卫星的定位信息,定位精度达10米以内 
|| ACCESS_COARSE_LOCATION（获取错略位置）| 通过WiFi或移动基站的方式获取用户错略的经纬度信息,定位精度大概误差在30~1500米
MICROPHONE（麦克风）|RECORD_AUDIO（录音）| 通过手机或耳机的麦克风录制声音
PHONE（手机）|READ_PHONE_STATE（读取电话状态）| 访问电话状态
|| CALL_PHONE（拨打电话）| 允许程序从非系统拨号器里输入电话号码
|| READ_CALL_LOG（读取拨号日志）| 允许应用程序读取用户的拨号日志
|| WRITE_CALL_LOG（写入拨号日志）| 允许应用程序写入用户的拨号日志
|| ADD_VOICEMAIL（添加语音信箱）| 允许应用程序添加系统语音邮件
|| USE_SIP（使用SIP视频）| 允许程序使用SIP视频服务
|| PROCESS_OUTGOING_CALLS（处理拨出电话）| 允许程序监视,修改或放弃播出电话
SENSORS（传感器）| BODY_SENSORS（体传感器）| 允许应用程序访问用户使用的传感器的数据来测量他/她的身体内发生的事情，如心率
SMS（短信）| SEND_SMS（发送短信）| 发送短信
|| RECEIVE_SMS（接收短信）| 接收短信
|| READ_SMS（读取短信内容）| 读取短信内容
|| RECEIVE_WAP_PUSH（接收Wap Push）| 接收WAP PUSH信息
|| RECEIVE_MMS（接收彩信）| 接收彩信
STORAGE（存储） | READ_EXTERNAL_STORAGE（读取外部存储）| 允许应用程序从外部存储读取
|| WRITE_EXTERNAL_STORAGE（写入外部存储）| 允许应用程序写入外部存储
更多详情：https://developer.android.com/guide/topics/security/permissions.html?hl=zh-cn#defining
