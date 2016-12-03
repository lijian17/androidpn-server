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
package org.androidpn.server.service.impl;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityExistsException;

import org.androidpn.server.dao.UserDao;
import org.androidpn.server.model.User;
import org.androidpn.server.service.UserExistsException;
import org.androidpn.server.service.UserNotFoundException;
import org.androidpn.server.service.UserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * UserService的实现类
 * 
 * @author lijian
 * @date 2016-12-3 下午11:42:28
 */
public class UserServiceImpl implements UserService {

    protected final Log log = LogFactory.getLog(getClass());

    private UserDao userDao;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public User getUser(String userId) {
        return userDao.getUser(new Long(userId));
    }

    public List<User> getUsers() {
        return userDao.getUsers();
    }
    
    public List<User> getUsersFromCreatedDate(Date createDate) {
    	 return userDao.getUsersFromCreatedDate(createDate);
    }

    public User saveUser(User user) throws UserExistsException {
		try {
			return userDao.saveUser(user);
		} catch (DataIntegrityViolationException e) {
			e.printStackTrace();
			log.warn(e.getMessage());
			throw new UserExistsException("用户 '" + user.getUsername()
					+ "' 已经存在!");
		} catch (EntityExistsException e) { // 需要JPA
			// JPA全称Java Persistence API.
			// JPA通过JDK5.0注解或XML描述对象－关系表的映射关系，并将运行期的实体对象持久化到数据库中。
			e.printStackTrace();
			log.warn(e.getMessage());
			throw new UserExistsException("User '" + user.getUsername()
					+ "' 需要JPA!");
		}
	}

    public User getUserByUsername(String username) throws UserNotFoundException {
        return (User) userDao.getUserByUsername(username);
    }

    public void removeUser(Long userId) {
        log.debug("removing user: " + userId);
        userDao.removeUser(userId);
    }

}
