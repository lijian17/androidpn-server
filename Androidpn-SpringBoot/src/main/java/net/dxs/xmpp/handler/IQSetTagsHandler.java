package net.dxs.xmpp.handler;

import org.dom4j.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;

import net.dxs.xmpp.UnauthorizedException;
import net.dxs.xmpp.session.ClientSession;
import net.dxs.xmpp.session.Session;
import net.dxs.xmpp.session.SessionManager;

/**
 * 本类处理TYPE_IQ类型为androidpn:iq:setalias的协议(设置标签集)
 * 
 * @author lijian
 * @date 2016-12-10 下午5:09:01
 */
public class IQSetTagsHandler extends IQHandler {

	private static final String NAMESPACE = "androidpn:iq:settags";
	private SessionManager sessionManager;

	public IQSetTagsHandler() {
		sessionManager = SessionManager.getInstance();
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
				String username = element.elementText("username");
				String tagsStr = element.elementText("tags");
				String[] tagsArr = tagsStr.split(",");
				if (tagsArr != null && tagsArr.length > 0) {
					for (String tag : tagsArr) {
						sessionManager.setUserTag(username, tag);
					}
					System.out.println("设置用户标签集成功");
				}
			}
		}
		return null;
	}

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

}
