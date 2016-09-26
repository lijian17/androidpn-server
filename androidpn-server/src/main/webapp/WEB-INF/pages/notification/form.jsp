<%@ page language="java" errorPage="/error.jsp" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8"%>
<%@ include file="/includes/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>通知中心-Androidpn后台管理中心</title>
	<meta name="menu" content="notification" />    
</head>

<body>

<h1>发送通知</h1>

<%--<div style="background:#eee; margin:20px 0px; padding:20px; width:500px; border:solid 1px #999;">--%>
<div style="margin:20px 0px;">
<form action="notification.do?action=send" method="post" style="margin: 0px;">
<table width="600" cellpadding="4" cellspacing="0" border="0">
<tr>
	<td width="20%">发送给:</td>
	<td width="80%">
		<input type="radio" name="broadcast" value="Y" checked="checked" />  全体 (广播) 
        <input type="radio" name="broadcast" value="N" /> 指定设备 
	</td>
</tr>
<tr id="trUsername" style="display:none;">
	<td>用户名:</td>
	<td><input type="text" id="username" name="username" value="" style="width:380px;" /></td>
</tr>
<tr>
	<td>消息标题:</td>
	<td><input type="text" id="title" name="title" value="钓鱼岛（中华人民共和国东海固有群岛）" style="width:380px;" /></td>
</tr>
<tr>
	<td>消息正文:</td>
	<td><textarea id="message" name="message" style="width:380px; height:80px;" >钓鱼岛，亦称钓鱼台、钓鱼屿、钓鱼山，是中国东海钓鱼岛列岛的主岛，也是中国自古以来的固有领土。位于北纬25°44.6′，东经123°28.4′，距浙江温州市约358千米、福建福州市约385千米、台湾基隆市约190千米，周围海域面积约为17.4万平方公里。长约3641米，宽约1905米，面积约3.91平方千米，最高海拔约362米，地势北部较平坦，东南侧山岩陡峭，东侧岩礁颇似尖塔，中央山脉横贯东西。钓鱼岛盛产山茶、棕榈、仙人掌、海芙蓉等珍贵中药材，栖息着大批海鸟，有“花鸟岛”的美称。</textarea></td>
</tr>
<%--
<tr>
	<td>Ticker:</td>
	<td><input type="text" id="ticker" name="ticker" value="" style="width:380px;" /></td>
</tr>
--%>
<tr>
	<td>链接:</td>
	<td><input type="text" id="uri" name="uri" value="" style="width:380px;" />
	    <br/><span style="font-size:0.8em">示例) http://www.baidu.com, geo:25.7519730000,123.4861640000, tel:150-1099-0415</span>
	</td>
</tr>
<tr>
	<td>&nbsp;</td>
	<td><input type="submit" value="提交" /></td>
</tr>
</table> 
</form>
</div>

<script type="text/javascript"> 
//<![CDATA[
 
$(function() {
	$('input[name=broadcast]').click(function() {
		if ($('input[name=broadcast]')[0].checked) {
			$('#trUsername').hide();
		} else {
			$('#trUsername').show();
		}
	});
	
	if ($('input[name=broadcast]')[0].checked) {
		$('#trUsername').hide();
	} else {
		$('#trUsername').show();
	}	
});
 
//]]>
</script>

</body>
</html>
