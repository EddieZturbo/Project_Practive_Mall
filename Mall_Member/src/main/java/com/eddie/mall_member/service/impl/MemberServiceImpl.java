package com.eddie.mall_member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.Query;
import com.eddie.mall_member.dao.MemberDao;
import com.eddie.mall_member.dao.MemberLevelDao;
import com.eddie.mall_member.entity.MemberEntity;
import com.eddie.mall_member.entity.MemberLevelEntity;
import com.eddie.mall_member.exception.PhoneException;
import com.eddie.mall_member.exception.UsernameException;
import com.eddie.mall_member.service.MemberService;
import com.eddie.mall_member.vo.MemberUserLoginVo;
import com.eddie.mall_member.vo.MemberUserRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberUserRegisterVo vo) {
        MemberEntity memberEntity = new MemberEntity();

        //验证用户名和手机号是否唯一。感知异常，异常机制
        checkPhoneUnique(vo.getPhone());
        checkUserNameUnique(vo.getUserName());

        //若以上未出现异常则进行对象的封装 并且储存到对应的数据表中
        memberEntity.setMobile(vo.getPhone());
        memberEntity.setUsername(vo.getUserName());

        //TODO 密码储存（ 加密处理 使用Spring家族提供的）
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);

        //设置默认等级 以及一些默认数据
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(levelEntity.getId());
        memberEntity.setPassword(encode);
        memberEntity.setMobile(vo.getPhone());
        memberEntity.setGender(0);
        memberEntity.setCreateTime(new Date());
        memberEntity.setNickname(vo.getUserName().toLowerCase());
        //保存数据
        this.baseMapper.insert(memberEntity);
    }

    @Override
    public void checkUserNameUnique(String userName) throws UsernameException {
        MemberDao baseMapper = this.baseMapper;
        MemberEntity memberEntity = baseMapper.selectOne(
                new LambdaQueryWrapper<MemberEntity>()
                        .eq(MemberEntity::getUsername, userName));
        if (null != memberEntity) {
            //表明数据库中已经存在该用户名
            throw new UsernameException();//抛出指定的异常
        }
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneException {
        MemberDao baseMapper = this.baseMapper;
        MemberEntity memberEntity = baseMapper.selectOne(
                new LambdaQueryWrapper<MemberEntity>()
                        .eq(MemberEntity::getMobile, phone));
        if (null != memberEntity) {
            //表明数据库中已经存在该用户名
            throw new PhoneException();//抛出指定的异常
        }
    }

    @Override
    public MemberEntity login(MemberUserLoginVo vo) {
        String loginacct = vo.getLoginacct();
        //登录验证失败情况一：数据库中尚未注册此账户(根据账号(手机号/用户名)去数据库中查询是否有数据)
        MemberEntity memberEntity = this.baseMapper.selectOne(
                new LambdaQueryWrapper<MemberEntity>()
                        .eq(MemberEntity::getMobile, loginacct)
                        .or()
                        .eq(MemberEntity::getUsername, loginacct));
        if(null == memberEntity){
            //数据库中尚未注册该用户
            return null;
        }

        //数据库中已经注册过该用户
        //登录验证失败情况二：数据库中存在此用户 账户密码输入错误
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();//TODO 密码验证使用BCryptPasswordEncoder进行matches
        //Tip:数据库中拿的加密过的放在第二个参数位置
        boolean matches = bCryptPasswordEncoder.matches(vo.getPassword(),memberEntity.getPassword());
        if(matches){
            return memberEntity;//验证成功 返回实体对象
        }else{
            return null;//验证失败 密码错误
        }
    }

}