package com.zea.springboot.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zea.springboot.controller.exception.ServiceException;
import com.zea.springboot.controller.request.BaseRequest;
import com.zea.springboot.controller.request.LoginRequest;
import com.zea.springboot.controller.request.PassWordRequest;
import com.zea.springboot.dto.LoginDTO;
import com.zea.springboot.entity.Admin;
import com.zea.springboot.mapper.AdminMapper;
import com.zea.springboot.service.IAdminService;
import com.zea.springboot.utils.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class AdminService implements IAdminService{
    @Autowired
    AdminMapper adminMapper;

    @Override
    public List<Admin> list() {
        return adminMapper.list();
    }

    @Override
    public PageInfo<Admin> page(BaseRequest baseRequest) {
        PageHelper.startPage(baseRequest.getPageNum(),baseRequest.getPageSize());
        List<Admin> admins = adminMapper.listByCondition(baseRequest);
        return new PageInfo<>(admins);
    }

    @Override
    public void save(Admin admin) {
        admin.setPassword(SecureUtil.md5(admin.getPassword())); //设置md5加密
        try{
            adminMapper.save(admin);
        } catch (DuplicateKeyException e){
            log.error("数据插入失败, username:{}", admin.getUsername(), e);
            throw new ServiceException("用户名重复");
        }
    }

    @Override
    public Admin getById(Integer id) {
        return adminMapper.getById(id);
    }

    @Override
    public void update(Admin admin) {
        admin.setUpdateTime(new Date());
        adminMapper.updateById(admin);
    }

    @Override
    public void deleteById(Integer id) {
        adminMapper.deleteById(id);
    }

    @Override
    public LoginDTO login(LoginRequest loginRequest) {
        loginRequest.setPassword(SecureUtil.md5(loginRequest.getPassword()));
        Admin admin = adminMapper.getByUsernameAndPassword(loginRequest);
        if(admin == null){
            throw new ServiceException("用户名或密码错误");
        }

        if(!admin.getStatus()){
            throw new ServiceException("当前用户已被禁用，请联系管理员处理");
        }

        LoginDTO loginDTO = new LoginDTO();
        BeanUtils.copyProperties(admin,loginDTO);

        //生成token
        String token = TokenUtils.genToken(String.valueOf(admin.getId()),admin.getPassword());
        loginDTO.setToken(token);
        return loginDTO;
    }

    @Override
    public void changePassWord(PassWordRequest passWordRequest) {
        passWordRequest.setNewPW(SecureUtil.md5(passWordRequest.getNewPW()));
        boolean flag = adminMapper.changePassWord(passWordRequest);
        if(flag == false){
            throw new ServiceException("修改失败");
        }
    }


}
