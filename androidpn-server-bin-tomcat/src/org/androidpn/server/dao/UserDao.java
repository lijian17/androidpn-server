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
package org.androidpn.server.dao;

import java.util.Date;
import java.util.List;

import org.androidpn.server.model.User;
import org.androidpn.server.service.UserNotFoundException;

/**
 * User DAO (Data Access Object) interface.
 * 
 * @author lijian
 * @date 2016-12-3 下午11:34:48
 */
public interface UserDao {

	/** 根据id获取用户 */
    public User getUser(Long id);

	/** 保存用户 */
    public User saveUser(User user);

	/** 根据id移除用户 */
    public void removeUser(Long id);

	/** 根据id检测用户是否存在 */
    public boolean exists(Long id);

	/** 获取用户集合 */
    public List<User> getUsers();
    
    public List<User> getUsersFromCreatedDate(Date createDate);

	/** 根据用户名获取用户 */
    public User getUserByUsername(String username) throws UserNotFoundException;

}
