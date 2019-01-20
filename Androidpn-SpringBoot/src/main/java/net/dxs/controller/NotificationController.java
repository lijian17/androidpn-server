package net.dxs.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import net.dxs.utils.Config;
import net.dxs.xmpp.push.NotificationManager;
import net.dxs.xmpp.ssl.SSLConfig;

/**
 * 控制器-处理与通知相关请求
 * 
 * @author lijian
 * @date 2019-01-20 20:33:12
 */
@RestController
@RequestMapping("notification")
public class NotificationController {

	private static final Log log = LogFactory.getLog(NotificationController.class);

	@Bean
	public NotificationManager notificationManager() {
		return new NotificationManager();
	}

	@RequestMapping(value = "send", method = RequestMethod.POST)
	public String send(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String broadcast = null;
		String username = null;
		String alias = null;
		String tag = null;
		String title = null;
		String message = null;
		String uri = null;
		String imageUrl = null;

		String apiKey = Config.getString("apiKey", "");
		log.debug("apiKey=" + apiKey);

		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload servletFileUpload = new ServletFileUpload(factory);
		List<FileItem> fileItem = servletFileUpload.parseRequest(request);
		for (FileItem item : fileItem) {
			if ("broadcast".equals(item.getFieldName())) {
				broadcast = item.getString("utf-8");
			} else if ("username".equals(item.getFieldName())) {
				username = item.getString("utf-8");
			} else if ("alias".equals(item.getFieldName())) {
				alias = item.getString("utf-8");
			} else if ("tag".equals(item.getFieldName())) {
				tag = item.getString("utf-8");
			} else if ("title".equals(item.getFieldName())) {
				title = item.getString("utf-8");
			} else if ("message".equals(item.getFieldName())) {
				message = item.getString("utf-8");
			} else if ("uri".equals(item.getFieldName())) {
				uri = item.getString("utf-8");
			} else if ("image".equals(item.getFieldName())) {
				imageUrl = uploadImage(request, item);
			}

			if (broadcast.equals("0")) {
				notificationManager().sendBroadcast(apiKey, title, message, uri, imageUrl);
			} else if (broadcast.equals("1")) {
				notificationManager().sendNotifcationToUser(apiKey, username, title, message, uri, imageUrl, true);
			} else if (broadcast.equals("2")) {
				notificationManager().sendNotificationByAlias(apiKey, alias, title, message, uri, imageUrl, true);
			} else if (broadcast.equals("3")) {
				notificationManager().sendNotificationByTag(apiKey, tag, title, message, uri, imageUrl, true);
			}
		}

		return "redirect:/";
	}

	/**
	 * 上传图片，并返回对应图片的访问地址
	 * 
	 * @param request
	 * @param fileItem
	 * @return
	 * @throws IOException
	 */
	private String uploadImage(HttpServletRequest request, FileItem fileItem) throws IOException {
		// 将获得一个磁盘路径
		String uploadPath = SSLConfig.class.getResource("/").getPath();
		File uploadDir = new File(uploadPath);
		// 如果文件目录不存在，则创建之
		if (!uploadDir.exists()) {
			uploadDir.mkdirs();
		}
		// 图片类型的文件，其他类型的不以处理
		if (fileItem != null && fileItem.getContentType().startsWith("image")) {
			// 获得文件后缀名
			String suffix = fileItem.getName().substring(fileItem.getName().lastIndexOf("."));
			// 为保证图片名称唯一性，在前面拼接当前时间
			String fileName = fileItem.getName().replace(suffix, getFormatNowDate() + suffix);
			InputStream is = fileItem.getInputStream();
			FileOutputStream fos = new FileOutputStream(uploadPath + "/" + fileName);
			byte[] b = new byte[1024];
			int len = 0;
			while ((len = is.read(b)) > 0) {
				fos.write(b, 0, len);
				fos.flush();
			}
			fos.close();
			is.close();
			// 当前服务器域名
			String serverName = request.getServerName();
			// 端口号
			int serverPort = request.getServerPort();
			String imageUrl = "http://" + serverName + ":" + serverPort + "/upload/" + fileName;
			System.out.println("imageUrl:" + imageUrl);
			return imageUrl;
		}
		return "";
	}

	/**
	 * 获得一个当前时间的格式化时间
	 * 
	 * @return
	 */
	private String getFormatNowDate() {
		Date nowTime = new Date(System.currentTimeMillis());
		SimpleDateFormat sdFormatter = new SimpleDateFormat("-yyyyMMddHHmmssSSS");
		String retStrFormatNowDate = sdFormatter.format(nowTime);
		return retStrFormatNowDate;
	}

}
