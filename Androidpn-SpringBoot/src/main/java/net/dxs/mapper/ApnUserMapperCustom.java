package net.dxs.mapper;

import java.util.List;

import net.dxs.pojo.ApnUser;

public interface ApnUserMapperCustom {

	List<ApnUser> queryUserSimplyInfoById(String id);
}
