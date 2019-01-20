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
package net.dxs.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import net.dxs.mapper.NotificationMapper;
import net.dxs.pojo.Notification;
import net.dxs.service.NotificationService;
import tk.mybatis.mapper.entity.Example;

/**
 * NotificationService的实现类
 * 
 * @author lijian
 * @date 2016-12-5 下午11:05:49
 */
@Service("notificationService")
public class NotificationServiceImpl implements NotificationService {

	@Autowired
	private NotificationMapper notificationMapper;

	public NotificationMapper getNotificationMapper() {
		return notificationMapper;
	}

	public void setNotificationMapper(NotificationMapper notificationMapper) {
		this.notificationMapper = notificationMapper;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void saveNotification(Notification notification) {
		notificationMapper.insert(notification);
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS)
	public List<Notification> findNotificationsByUsername(String username) {
		Example example = new Example(Notification.class);
		Example.Criteria criteria = example.createCriteria();
		if (!StringUtils.isEmptyOrWhitespace(username)) {
			criteria.andLike("username", username);
		}
		return notificationMapper.selectByExample(example);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteNotification(Notification notification) {
		notificationMapper.delete(notification);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteNotificationByUUID(String uuid) {
		Example example = new Example(Notification.class);
		Example.Criteria criteria = example.createCriteria();
		if (!StringUtils.isEmptyOrWhitespace(uuid)) {
			criteria.andLike("uuid", uuid);
		}
		notificationMapper.deleteByExample(example);
	}

}
