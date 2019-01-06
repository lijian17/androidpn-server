# Androidpn server
Androidpn 推送服务器

===================================================================================================================
ANDROIDPN-SERVER README
=======================
http://androidpn.sourceforge.net/

Push Notification Service for Android

This is an open source project to provide push notification support for Android
-- a xmpp based notification server and a client tool kit. 

androidpn-server--推送服务器端
相关连客户端代码库地址：https://github.com/lijian17/androidpn-client
====================================================================================================================

# 一、运行说明：
## 1、androidpn-server-bin-tomcat版本
### 导入说明：
1、打开eclipse。方法：双击eclipse.exe。
2、菜单File->Import
3、General->Existing Projects into Workspce，点击next
4、Browse选择要导入的项目路径，点击Finish。
5、项目导入完成。
### 项目运行错误处理
1、错误说明：import javax.servlet.http.HttpServletRequest 提示错误
2、原因分析：在eclipse中导入项目后，Server的library不会一同导过来。
3、解决：
1）右击项目，选择properties
2）搜索Java Build Path
3）选中libraries,点击Add Library
4）点击Server RunTime
5）点击next，选择服务器，点击finish
6）点击Apply and Close

## 2、Androidpn-SpringBoot版本








