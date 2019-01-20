package net.dxs.controller;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xmpp.packet.Presence;
import org.xmpp.packet.Presence.Show;

import net.dxs.pojo.ApnJSONResult;
import net.dxs.service.UserNotFoundException;
import net.dxs.vo.SessionVO;
import net.dxs.xmpp.session.ClientSession;
import net.dxs.xmpp.session.Session;
import net.dxs.xmpp.session.SessionManager;

/**
 * 控制器-处理与会话相关请求
 * 
 * @author lijian
 * @date 2019-01-20 17:49:56
 */
@RestController
@RequestMapping("session")
public class SessionController {

	@RequestMapping("/querySessionList")
	public ApnJSONResult queryUserList() throws UserNotFoundException, UnknownHostException {
		ClientSession[] sessions = new ClientSession[0];
		sessions = SessionManager.getInstance().getSessions().toArray(sessions);

		List<SessionVO> voList = new ArrayList<SessionVO>();
		for (ClientSession sess : sessions) {
			SessionVO vo = new SessionVO();
			vo.setUsername(sess.getUsername());
			vo.setResource(sess.getAddress().getResource());

			// Status
			if (sess.getStatus() == Session.STATUS_CONNECTED) {
				vo.setStatus("CONNECTED");
			} else if (sess.getStatus() == Session.STATUS_AUTHENTICATED) {
				vo.setStatus("AUTHENTICATED");
			} else if (sess.getStatus() == Session.STATUS_CLOSED) {
				vo.setStatus("CLOSED");
			} else {
				vo.setStatus("UNKNOWN");
			}
			// Presence
			if (!sess.getPresence().isAvailable()) {
				vo.setPresence("Offline");// 离线
			} else {
				Show show = sess.getPresence().getShow();
				if (show == null) {
					vo.setPresence("Online");// 在线的
				} else if (show == Presence.Show.away) {
					vo.setPresence("Away");// 离开
				} else if (show == Presence.Show.chat) {
					vo.setPresence("Chat");// 聊天
				} else if (show == Presence.Show.dnd) {
					vo.setPresence("Do Not Disturb");// 请勿打扰
				} else if (show == Presence.Show.xa) {
					vo.setPresence("eXtended Away");// 忙碌
				} else {
					vo.setPresence("Unknown");// 未知
				}
			}
			vo.setClientIP(sess.getHostAddress());
			vo.setCreatedDate(sess.getCreationDate());
			voList.add(vo);
		}

		return ApnJSONResult.ok(voList);
	}

}
