package cn.itcast.account.service.impl;

import cn.itcast.account.entity.AccountFreeze;
import cn.itcast.account.mapper.AccountFreezeMapper;
import cn.itcast.account.mapper.AccountMapper;
import cn.itcast.account.service.AccountTCCService;
import io.seata.core.context.RootContext;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @version 1.0
 * @description 说明
 * @package cn.itcast.account.service.impl
 */
@Service
@Slf4j
public class AccountTCCServiceImpl implements AccountTCCService {

    @Resource
    private AccountMapper accountMapper;

    @Resource
    private AccountFreezeMapper accountFreezeMapper;

    /**
     * 用户账户扣款
     * 1 扣减account表可用金额
     * 2 记录冻结金额和事务状态到account_freeze表
     *
     * @param userId
     * @param money
     */
    @Override
    @Transactional
    public void deduct(String userId, int money)
    {
        String xid = RootContext.getXID();
        //通过事务ID查询冻结表，如果存在记录说明cancel已被执行过，拒绝业务
        AccountFreeze one = accountFreezeMapper.selectById(xid);
        if (null!=one){
            //说明cancel已经执行过，拒绝业务
            log.info("Try::Cancel已经执行过了!, 不处理:{}", xid);
            return;
        }
        //1. 扣减金额
        accountMapper.deduct(userId, money);
        log.debug("扣减金额成功: {},userId={}", xid, userId);
        //2. 添加冻结记录,将冻结金额和事务状态记录到account_freeze表
        //2.1 构建pojo
        AccountFreeze freeze = new AccountFreeze();
        //2.2 给pojo属性赋值
        freeze.setFreezeMoney(money);
        freeze.setState(AccountFreeze.State.TRY);
        freeze.setXid(xid);
        freeze.setUserId(userId);
        //2.3 mapper.insert
        accountFreezeMapper.insert(freeze);
        log.debug("冻结金额成功! {},userId={}", xid, userId);
        //3. 添加本地事务控制
    }


    /**
     * 确认业务，根据xid删除account_freeze表的冻结记录
     *
     * @param ctx
     * @return
     */
    @Override
    public boolean confirm(BusinessActionContext ctx)
    {
        //1. 获取事务Id
        String xid = ctx.getXid();
        log.debug("进入confirm: {}", xid);
        //2. 通过事务Id删除数据
        int count = accountFreezeMapper.deleteById(xid);
        log.debug("删除冻结记录 {},count={}", xid, count);
        return 1 == count;
    }

    /**
     * 撤消回滚，需要把冻结金额还原<br/>
     * 1 修改account_freeze表，冻结金额为0，state为2<br/>
     * 2 修改account表，恢复可用金额<br/>
     *
     * @param ctx
     * @return
     */
    @Override
    @Transactional
    public boolean cancel(BusinessActionContext ctx)
    {
        log.info("Cancel::执行了...:{}", ctx.getXid());
        //1. 获取事务Id
        String xid = ctx.getXid();
        //2. 通过事务Id查询冻结记录
        AccountFreeze freeze = accountFreezeMapper.selectById(xid);
        //判断cancel有没有执行
        if (null == freeze) {
            //try没有执行,允许空回滚,并记录到冻结表中
            //2.1 构建pojo
            freeze = new AccountFreeze();
            //2.2 给pojo属性赋值
            freeze.setFreezeMoney(0);
            freeze.setState(AccountFreeze.State.CANCEL);
            freeze.setXid(xid);
            accountFreezeMapper.insert(freeze);
            return true;
        }
        //幂等处理
        if(freeze.getState() != AccountFreeze.State.TRY){
            //如果状态不为1(try),说明业务已经执行过了，则不能重复执行业务
            log.info("");
            return true;
        }
        //3. 回滚金额
        /*String xid = ctx.getXid();
        log.debug("开始回滚金额: {},userId={}", xid, userId);
        accountMapper.refund(userId, freeze.getFreezeMoney());
        //4. 更新冻结金额为0，且更新状态为cancel
        freeze = new AccountFreeze();
        freeze.setFreezeMoney(0);
        freeze.setState(AccountFreeze.State.CANCEL);
        freeze.setXid(xid);*/
        String userId = ctx.getActionContext("userId").toString();
        AccountFreeze updatePojo = new AccountFreeze();
        updatePojo.setXid(ctx.getXid());
        updatePojo.setFreezeMoney(0);
        updatePojo.setState(AccountFreeze.State.CANCEL);// 代表执行了cancel
        accountFreezeMapper.updateById(updatePojo);
        int count = accountFreezeMapper.updateById(updatePojo);
        log.debug("回滚-更新冻结状态：{}, userId={},count={}", xid, userId, count);
        //5. 本地事务控制
        //return count == 1;
        return true;
    }
}
