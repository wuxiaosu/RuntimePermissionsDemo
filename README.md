# RuntimePermissionsDemo
基于PermissionsDispatcher的动态权限demo，PermissionsDispatcher地址https://github.com/hotchemi/PermissionsDispatcher   
（部分定制rom弹出请求权限提示框时体验有些许不同，例如华为emui）  
# 效果图
<img src="https://raw.githubusercontent.com/wuxiaosu/RuntimePermissionsDemo/master/screenshorts/00.gif" width = "300" height = "500"/>
# 快速配置 
## 1.引入jar包 

```
apply plugin: 'android-apt'

dependencies {
  compile 'com.github.hotchemi:permissionsdispatcher:2.2.0'
  apt 'com.github.hotchemi:permissionsdispatcher-processor:2.2.0'
}
```  
## 2.配置   
在工程project目录 **build.gradle** 文件下添加 ： 

```
buildscript {
  dependencies {
    classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
  }
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
你需要：  
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
<table>
        <tbody>
            <tr>
                <th scope="col">权限组</th>
                <th scope="col">权限</th>
            </tr>

            <tr>
                <td>CALENDAR</td>
                <td>
                    <ul>
                        <li>
                            READ_CALENDAR
                        </li>
                    </ul>
                    <ul>
                        <li>
                            WRITE_CALENDAR
                        </li>
                    </ul>
                </td>
            </tr>

            <tr>
                <td>CAMERA</td>
                <td>
                    <ul>
                        <li>
                            CAMERA
                        </li>
                    </ul>
                </td>
            </tr>

            <tr>
                <td>CONTACTS</td>
                <td>
                    <ul>
                        <li>
                            READ_CONTACTS
                        </li>
                        <li>
                            WRITE_CONTACTS
                        </li>
                        <li>
                            GET_ACCOUNTS
                        </li>
                    </ul>
                </td>
            </tr>

            <tr>
                <td>LOCATION</td>
                <td>
                    <ul>
                        <li>
                            ACCESS_FINE_LOCATION
                        </li>
                        <li>
                            ACCESS_COARSE_LOCATION
                        </li>
                    </ul>
                </td>
            </tr>

            <tr>
                <td>MICROPHONE</td>
                <td>
                    <ul>
                        <li>
                            RECORD_AUDIO
                        </li>
                    </ul>
                </td>
            </tr>

            <tr>
                <td>PHONE</td>
                <td>
                    <ul>
                        <li>
                            READ_PHONE_STATE
                        </li>
                        <li>
                            CALL_PHONE
                        </li>
                        <li>
                            READ_CALL_LOG
                        </li>
                        <li>
                            WRITE_CALL_LOG
                        </li>
                        <li>
                            ADD_VOICEMAIL
                        </li>
                        <li>
                            USE_SIP
                        </li>
                        <li>
                            PROCESS_OUTGOING_CALLS
                        </li>
                    </ul>
                </td>
            </tr>

            <tr>
                <td>SENSORS</td>
                <td>
                    <ul>
                        <li>
                            BODY_SENSORS
                        </li>
                    </ul>
                </td>
            </tr>

            <tr>
                <td>SMS</td>
                <td>
                    <ul>
                        <li>
                            SEND_SMS
                        </li>
                        <li>
                            RECEIVE_SMS
                        </li>
                        <li>
                            READ_SMS
                        </li>
                        <li>
                            RECEIVE_WAP_PUSH
                        </li>
                        <li>
                            RECEIVE_MMS
                        </li>
                    </ul>
                </td>
            </tr>

            <tr>
                <td>
                    STORAGE
                </td>
                <td>
                    <ul>
                        <li>
                            READ_EXTERNAL_STORAGE
                        </li>
                        <li>
                            WRITE_EXTERNAL_STORAGE
                        </li>
                    </ul>
                </td>
            </tr>

        </tbody>
    </table>  
同一权限组内只需要授权一个，即同时授权了权限组内其他权限。
更多详情：https://developer.android.com/guide/topics/security/permissions.html?hl=zh-cn#defining
