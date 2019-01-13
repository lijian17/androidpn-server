package net.dxs.controller;

import java.util.Date;
import java.util.List;

import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.dxs.pojo.ApnJSONResult;
import net.dxs.pojo.ApnUser;
import net.dxs.service.UserService;

/**
 * 控制器-处理与用户相关请求
 * 
 * @author lijian
 * @date 2019-01-08 22:36:56
 */
@RestController
@RequestMapping("user")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private Sid sid;

	@RequestMapping("/getUsers")
	public ApnJSONResult getUsers() {
		return null;
	}

	@RequestMapping("/saveUser")
	public ApnJSONResult saveUser() throws Exception {
		String userId = sid.nextShort();

		ApnUser user = new ApnUser();
		user.setUsername(userId);
		user.setPassword("abc123");
		user.setCreatedDate(new Date());

		userService.saveUser(user);

		return ApnJSONResult.ok("保存成功");
	}

	@RequestMapping("/updateUser")
	public ApnJSONResult updateUser() {
		ApnUser user = new ApnUser();
		user.setPassword("4595e4d1c5de48318d7fb3cc8e20953c");
		user.setUpdatedDate(new Date());

		userService.updateUser(user);

		return ApnJSONResult.ok("保存成功");
	}

	@RequestMapping("/deleteUser")
	public ApnJSONResult deleteUser(String userId) {
		userService.deleteUser(userId);
		return ApnJSONResult.ok("删除成功");
	}

	@RequestMapping("/queryUserById")
	public ApnJSONResult queryUserById(String userId) {
		return ApnJSONResult.ok(userService.queryUserById(userId));
	}

	@RequestMapping("/queryUserList")
	public ApnJSONResult queryUserList() {
		ApnUser user = new ApnUser();
		List<ApnUser> userList = userService.queryUserList(user);
		return ApnJSONResult.ok(userList);
	}

	@RequestMapping("/queryUserListPaged")
	public ApnJSONResult queryUserListPaged(Integer page) {
		if (page == null) {
			page = 1;
		}
		int pageSize = 10;
		ApnUser user = new ApnUser();
		// user.setNickname("lijian");
		List<ApnUser> userList = userService.queryUserListPaged(user, page, pageSize);
		return ApnJSONResult.ok(userList);
	}

	@RequestMapping("/queryUserByIdCustom")
	public ApnJSONResult queryUserByIdCustom(String userId) {
		return ApnJSONResult.ok(userService.queryUserByIdCustom(userId));
	}

	@RequestMapping("/saveUserTransactional")
	public ApnJSONResult saveUserTransactional() {
		String userId = sid.nextShort();
		ApnUser user = new ApnUser();
		user.setName(userId);
		user.setUsername("lijian" + new Date());
		user.setUpdatedDate(new Date());
		userService.saveUserTransactional(user);
		return ApnJSONResult.ok("保存成功");
	}

}
