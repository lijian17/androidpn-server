package net.dxs.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import com.github.pagehelper.PageHelper;

import net.dxs.mapper.ApnUserMapper;
import net.dxs.mapper.ApnUserMapperCustom;
import net.dxs.pojo.ApnUser;
import net.dxs.service.UserService;
import tk.mybatis.mapper.entity.Example;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private ApnUserMapper userMapper;

	@Autowired
	private ApnUserMapperCustom userMapperCustom;

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void saveUser(ApnUser user) throws Exception {
		userMapper.insert(user);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void updateUser(ApnUser user) {
		userMapper.updateByPrimaryKeySelective(user);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteUser(String userId) {
		userMapper.deleteByPrimaryKey(userId);
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS)
	public ApnUser queryUserById(String userId) {
		return userMapper.selectByPrimaryKey(userId);
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS)
	public ApnUser getUserByUsername(String username) {
		Example example = new Example(ApnUser.class);
		Example.Criteria criteria = example.createCriteria();
		if(!StringUtils.isEmptyOrWhitespace(username)) {
			criteria.andLike("username", username);
		}
		return userMapper.selectOneByExample(example);
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS)
	public List<ApnUser> queryUserList(ApnUser user) {
		Example example = new Example(ApnUser.class);
		Example.Criteria criteria = example.createCriteria();
		if (!StringUtils.isEmptyOrWhitespace(user.getUsername())) {
			// criteria.andEqualTo("username", user.getUsername());
			criteria.andLike("username", "%" + user.getUsername() + "%");
		}
		if (!StringUtils.isEmptyOrWhitespace(user.getEmail())) {
			criteria.andLike("email", "%" + user.getEmail() + "%");
		}
		List<ApnUser> userList = userMapper.selectByExample(example);
		return userList;
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS)
	public List<ApnUser> queryUserListPaged(ApnUser user, Integer page, Integer pageSize) {
		// 开始分页
		PageHelper.startPage(page, pageSize);

		Example example = new Example(ApnUser.class);
		Example.Criteria criteria = example.createCriteria();

		if (!StringUtils.isEmptyOrWhitespace(user.getUsername())) {
			criteria.andLike("username", "%" + user.getUsername() + "%");
		}
		example.orderBy("createdDate").desc();
		List<ApnUser> userList = userMapper.selectByExample(example);

		return userList;
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS)
	public ApnUser queryUserByIdCustom(String userId) {
		List<ApnUser> userList = userMapperCustom.queryUserSimplyInfoById(userId);
		if (userList != null && !userList.isEmpty()) {
			return (ApnUser) userList.get(0);
		}
		return null;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void saveUserTransactional(ApnUser user) {
		userMapper.insert(user);
		userMapper.updateByPrimaryKeySelective(user);
	}

}
