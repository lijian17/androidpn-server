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
package org.androidpn.server.service;

import java.util.Date;
import java.util.List;

import org.androidpn.server.model.User;

/**
 * 用户管理业务服务接口
 * 
 * @author lijian
 * @date 2016-12-3 下午11:41:16
 */
public interface UserService {

	/** 根据id获取用户 */
    public User getUser(String userId);

	/** 获取用户集合 */
    public List<User> getUsers();
    
    public List<User> getUsersFromCreatedDate(Date createDate);

	/** 保存用户 */
    public User saveUser(User user) throws UserExistsException;

	/** 根据用户名获取用户 */
    public User getUserByUsername(String username) throws UserNotFoundException;

	/** 根据id移除用户 */
    public void removeUser(Long userId);

}
