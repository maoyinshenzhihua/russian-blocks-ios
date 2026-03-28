# iOS IPA 打包指南

## 一、打包方式概览

| 方式 | 需要 Mac | 难度 | 适用场景 |
|------|---------|------|---------|
| Xcode 直接打包 | ✅ | 简单 | 有 Mac 的开发者 |
| GitHub Actions 云打包 | ❌ | 中等 | 无 Mac，使用第三方工具安装 |
| 越狱设备打包 | ✅ | 复杂 | 越狱 iOS 设备 |

---

## 二、GitHub Actions 云打包（推荐）

### 2.1 准备工作

1. **GitHub 账号** - 注册 github.com
2. **代码仓库** - 将 iOS 项目上传到 GitHub
3. ** XcodeGen** - 项目需要 project.yml 配置文件

### 2.2 创建工作流文件

在仓库创建 `.github/workflows/build.yml`：

```yaml
name: Build iOS App

on:
  push:
    branches: [master]
  workflow_dispatch:

env:
  XCODE_VERSION: '15.0'
  IOS_DEPLOYMENT_TARGET: '13.0'
  SCHEME: YourAppName

jobs:
  build:
    runs-on: macos-14

    steps:
      - uses: actions/checkout@v4

      - name: Setup Xcode
        run: sudo xcode-select -s /Applications/Xcode_${{ env.XCODE_VERSION }}.app

      - name: Generate project
        run: |
          which xcodegen || brew install xcodegen
          cd ios/YourAppName
          xcodegen generate

      - name: Build
        run: |
          xcodebuild -project YourAppName.xcodeproj \
            -scheme ${{ env.SCHEME }} \
            -configuration Release \
            -destination 'generic/platform=iOS' \
            CODE_SIGN_IDENTITY="" \
            CODE_SIGNING_REQUIRED=NO \
            build

      - name: Create IPA
        run: |
          mkdir -p output
          APP_PATH=$(find ~/Library/Developer/Xcode/DerivedData -name "*.app" -type d | grep -v PlugIns | head -1)
          cp -r "$APP_PATH" ./output/
          cd output
          mkdir -p Payload
          cp -r *.app Payload/
          zip -r ../YourAppName.ipa Payload

      - name: Upload
        uses: actions/upload-artifact@v4
        with:
          name: YourAppName-ipa
          path: output/YourAppName.ipa
          retention-days: 7
```

### 2.3 推送触发构建

```bash
git add .
git commit -m "add workflow"
git push origin master
```

### 2.4 下载 IPA

1. 进入 GitHub 仓库 → **Actions** 页面
2. 点击构建任务 → **Artifacts**
3. 下载 IPA 文件

---

## 三、安装 IPA 到设备

### 3.1 使用第三方工具

| 工具 | 平台 | 特点 |
|------|------|------|
| 牛蛙助手 | Windows | 界面简洁，适合新手 |
| AltStore | Mac/Windows | 需要 Apple ID |
| Sideloadly | Mac/Windows | 简单易用 |
| 3uTools | Windows | 功能丰富 |

### 3.2 安装步骤（以牛蛙助手为例）

1. 下载并安装牛蛙助手
2. 连接 iOS 设备到电脑
3. 打开牛蛙助手，识别设备
4. 导入 IPA 文件
5. 点击安装
6. 在 iOS 设备上信任证书：
   - 设置 → 通用 → VPN与设备管理
   - 找到对应证书并信任

---

## 四、Xcode 本地打包

### 4.1 创建项目

使用 XcodeGen 生成项目：

```bash
brew install xcodegen
xcodegen generate
```

### 4.2 打包步骤

1. Xcode 打开 `.xcodeproj` 文件
2. 选择 **Release** 配置
3. 选择目标设备或通用 iOS 设备
4. **Product** → **Archive**
5. 等待编译完成
6. 在 Organizer 中选择 archive
7. 点击 **Distribute App**
8. 选择 **Save for Ad Hoc Deployment**
9. 导出 IPA 文件

### 4.3 命令行打包

```bash
# 编译
xcodebuild -project YourApp.xcodeproj \
  -scheme YourScheme \
  -configuration Release \
  -destination 'generic/platform=iOS' \
  CODE_SIGN_IDENTITY="" \
  CODE_SIGNING_REQUIRED=NO \
  build

# 打包 IPA
mkdir -p Payload
cp -r ~/Library/Developer/Xcode/DerivedData/*/Build/Products/Release-iphoneos/*.app Payload/
zip -r YourApp.ipa Payload
rm -rf Payload
```

---

## 五、常见问题

### 5.1 构建失败

| 错误 | 解决方法 |
|------|---------|
| Xcode 版本不兼容 | 降低 `PROJECT_FORMAT_VERSION` 或升级 Xcode |
| 找不到资源文件 | 检查 `project.yml` 中的 `sources` 配置 |
| 编译错误 | 查看日志定位具体代码问题 |

### 5.2 IPA 无法安装

| 原因 | 解决方法 |
|------|---------|
| 证书未信任 | 在设置中信任证书 |
| 系统版本不兼容 | 降低 `IOS_DEPLOYMENT_TARGET` |
| 设备不支持 | 检查设备是否在支持列表中 |

### 5.3 网络问题

推送代码时遇到网络错误：
```bash
git config --global --unset http.proxy
git config --global --unset https.proxy
```

---

## 六、自定义配置

### 6.1 修改最低 iOS 版本

在 `project.yml` 中：
```yaml
options:
  deploymentTarget:
    iOS: "12.0"
```

在 workflow 中：
```yaml
env:
  IOS_DEPLOYMENT_TARGET: '12.0'
```

### 6.2 修改 App 名称

在 `project.yml` 中：
```yaml
targets:
  YourApp:
    info:
      properties:
        CFBundleDisplayName: 你的App名称
```

---

*文档版本：v1.0*
*最后更新：2026-03-28*
