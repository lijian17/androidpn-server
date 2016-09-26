/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.androidpn.server.console.vo;

import java.util.Date;

/**
 * session业务bean（session值对象）
 * 
 * <pre>
 * 一、PO:persistant object 持久对象,可以看成是与数据库中的表相映射的java对象。
 * 最简单的PO就是对应数据库中某个表中的一条记录，多个记录可以用PO的集合。PO中应该不包含任何对数据库的操作。 
 * 
 * 二、VO:value object值对象。通常用于业务层之间的数据传递，和PO一样也是仅仅包含数据而已。
 * 但应是抽象出的业务对象,可以和表对应,也可以不,这根据业务的需要.个人觉得同DTO(数据传输对象),在web上传递。  
 * 
 * 三、DAO:data access object 数据访问对象，此对象用于访问数据库。通常和PO结合使用，DAO中包含了各种数据库的操作方法。
 * 通过它的方法,结合PO对数据库进行相关的操作。  
 * 
 * 四、BO:business object 业务对象,封装业务逻辑的java对象,通过调用DAO方法,结合PO,VO进行业务操作。  
 * 
 * 五、POJO:plain ordinary java object 简单无规则java对象,我个人觉得它和其他不是一个层面上的东西,VO和PO应该都属于它。   
 * 
 * O/R Mapping 是 Object Relational Mapping（对象关系映射）的缩写。
 * 通俗点讲，就是将对象与关系数据库绑定，用对象来表示关系数据。
 * 在O/R Mapping的世界里，有两个基本的也是重要的东东需要了解，即VO，PO。 
 * 
 * VO，值对象(Value Object)，PO，持久对象(Persisent Object)，它们是由一组属性和属性的get和set方法组成。
 * 从结构上看，它们并没有什么不同的地方。但从其意义和本质上来看是完全不同的。 
 * 1．VO是用new关键字创建，由GC回收的。 
 *   PO则是向数据库中添加新数据时创建，删除数据库中数据时削除的。并且它只能存活在一个数据库连接中，断开连接即被销毁。 
 * 2．VO是值对象，精确点讲它是业务对象，是存活在业务层的，是业务逻辑使用的，它存活的目的就是为数据提供一个生存的地方。 
 *   PO则是有状态的，每个属性代表其当前的状态。它是物理数据的对象表示。
 * 	  使用它，可以使我们的程序与物理数据解耦，并且可以简化对象数据与物理数据之间的转换。 
 * 3．VO的属性是根据当前业务的不同而不同的，也就是说，它的每一个属性都一一对应当前业务逻辑所需要的数据的名称。 
 *   PO的属性是跟数据库表的字段一一对应的。  
 *   PO对象需要实现序列化接口。
 * </pre>
 * 
 * @author lijian-pc
 * @date 2016年7月31日 下午2:43:15
 */
public class SessionVO {

	/** 用户名 */
	private String username;

	/** 数据源 */
	private String resource;

	/** 会话当前状态 */
	private String status;

	/**   */
	private String presence;

	/** 客户端IP */
	private String clientIP;

	/** 会话创建时间 */
	private Date createdDate;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPresence() {
		return presence;
	}

	public void setPresence(String presence) {
		this.presence = presence;
	}

	public String getClientIP() {
		return clientIP;
	}

	public void setClientIP(String clientIP) {
		this.clientIP = clientIP;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

}
