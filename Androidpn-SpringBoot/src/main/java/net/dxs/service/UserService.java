package net.dxs.service;

import java.util.List;

import net.dxs.pojo.ApnUser;

public interface UserService {

	public void saveUser(ApnUser user) throws Exception;

	public void updateUser(ApnUser user);

	public void deleteUser(String userId);

	public ApnUser queryUserById(String userId);

	public ApnUser getUserByUsername(String username);

	public List<ApnUser> queryUserList(ApnUser user);

	public List<ApnUser> queryUserListPaged(ApnUser user, Integer page, Integer pageSize);

	public ApnUser queryUserByIdCustom(String userId);

	public void saveUserTransactional(ApnUser user);
}
