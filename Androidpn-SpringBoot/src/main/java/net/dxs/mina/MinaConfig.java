package net.dxs.mina;

import java.net.InetSocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.dxs.xmpp.codec.XmppCodecFactory;
import net.dxs.xmpp.net.XmppIoHandler;

@Configuration
public class MinaConfig {
	private static final Log log = LogFactory.getLog(MinaConfig.class);

	private static final int SOCKET_PORT = 5222;

	@Bean
	public LoggingFilter loggingFilter() {
		return new LoggingFilter();
	}

	@Bean
	public ExecutorFilter executorFilter() {
		return new ExecutorFilter();
	}

	@Bean
	public IoHandler ioHandler() {
		return new XmppIoHandler();
	}

	@Bean
	public InetSocketAddress inetSocketAddress() {
		return new InetSocketAddress(SOCKET_PORT);
	}

	@Bean
	public IoAcceptor ioAcceptor() throws Exception {
		log.info("正在启动socket服务端...");
		// 监听传入 连接的对象
		NioSocketAcceptor acceptor = new NioSocketAcceptor();

		acceptor.setDefaultLocalAddress(inetSocketAddress());

		// 创建一个handler来实时处理客户端的连接和请求，这个handler 类必须实现 IoHandler这个接口。
		acceptor.setHandler(ioHandler());

		// 记录所有的信息，比如创建session(会话)，接收消息，发送消息，关闭会话等
		acceptor.getFilterChain().addLast("logger", loggingFilter());
		acceptor.getFilterChain().addLast("executor", executorFilter());
		// ProtocolCodecFilter(协议编解码过滤器).这个过滤器用来转换二进制或协议的专用数据到消息对象中
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new XmppCodecFactory()));

		acceptor.setReuseAddress(true);

		// 设置读写缓冲区大小
//		acceptor.getSessionConfig().setReadBufferSize(2048);
		// 空闲时间 通道均在10 秒内无任何操作就进入空闲状态
//		acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
		acceptor.getSessionConfig().setReaderIdleTime(30);

		acceptor.bind();
		log.info("socket 服务端 已经启动...");
		return acceptor;
	}

//	@Autowired
//	private NioSocketAcceptor ioAcceptor;

//	@Autowired
//	private XmppIoHandler xmppHandler;

//	@Autowired
//	private DefaultIoFilterChainBuilder filterChainBuilder;

//	@Autowired
//	private CustomEditorConfigurer customEditorConfigurer;

//	public MinaConfig() {
//		System.out.println("MinaComponent-start");
//		NioSocketAcceptor ioAcceptor = new NioSocketAcceptor();
//		ioAcceptor.setDefaultLocalAddress(new InetSocketAddress(5222));
//		ioAcceptor.setHandler(new XmppIoHandler());
//		ioAcceptor.setFilterChainBuilder(new DefaultIoFilterChainBuilder());
//		ioAcceptor.setReuseAddress(true);
//
//		SocketSessionConfig sessionConfig = ioAcceptor.getSessionConfig();
//		sessionConfig.setReaderIdleTime(30);
//		try {
//			ioAcceptor.bind();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		System.out.println("MinaComponent-end");
//	}
}
