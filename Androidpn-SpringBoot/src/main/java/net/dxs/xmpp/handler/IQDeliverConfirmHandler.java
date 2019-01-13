package net.dxs.xmpp.handler;

import org.dom4j.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;

import net.dxs.service.NotificationService;
import net.dxs.service.ServiceLocator;
import net.dxs.xmpp.UnauthorizedException;
import net.dxs.xmpp.session.ClientSession;
import net.dxs.xmpp.session.Session;

/**
 * 本类处理TYPE_IQ类型为androidpn:iq:deliverconfirm的协议(消息回执)
 * 
 * @author lijian
 * @date 2016-12-6 下午11:17:38
 */
public class IQDeliverConfirmHandler extends IQHandler {

	private static final String NAMESPACE = "androidpn:iq:deliverconfirm";
	private NotificationService notificationService;

	public IQDeliverConfirmHandler() {
		notificationService = ServiceLocator.getNotificationService();
	}

	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
		IQ reply = null;

		ClientSession session = sessionManager.getSession(packet.getFrom());
		if (session == null) {
			log.error("未找到Key的Session " + packet.getFrom());
			reply = IQ.createResultIQ(packet);
			reply.setChildElement(packet.getChildElement().createCopy());
			reply.setError(PacketError.Condition.internal_server_error);
			return reply;
		}

		if (session.getStatus() == Session.STATUS_AUTHENTICATED) {
			if (IQ.Type.set.equals(packet.getType())) {
				Element element = packet.getChildElement();
				String uuid = element.elementText("uuid");
				notificationService.deleteNotificationByUUID(uuid);
			}
		}
		return null;
	}

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

}
