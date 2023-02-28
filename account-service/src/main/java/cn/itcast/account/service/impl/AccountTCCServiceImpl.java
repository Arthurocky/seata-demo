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
     * 用户账户扣款<br/>
     * 1 扣减account表可用金额<br/>
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
        //1. 扣减金额
        accountMapper.deduct(userId, money);
        log.debug("扣减金额成功: {},userId={}", xid, userId);
        //2. 添加冻结记录
        AccountFreeze freeze = new AccountFreeze();
        freeze.setFreezeMoney(money);
        freeze.setState(AccountFreeze.State.TRY);
        freeze.setXid(xid);
        freeze.setUserId(userId);
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
        //1. 获取事务Id
        String xid = ctx.getXid();
        //2. 通过事务Id查询冻结记录
        AccountFreeze freeze = accountFreezeMapper.selectById(xid);
        //3. 回滚金额
        String userId = ctx.getActionContext("userId").toString();
        log.debug("开始回滚金额: {},userId={}", xid, userId);
        accountMapper.refund(userId, freeze.getFreezeMoney());
        //4. 更新冻结金额为0，且更新状态为cancel
        freeze = new AccountFreeze();
        freeze.setFreezeMoney(0);
        freeze.setState(AccountFreeze.State.CANCEL);
        freeze.setXid(xid);
        int count = accountFreezeMapper.updateById(freeze);
        log.debug("回滚-更新冻结状态：{}, userId={},count={}", xid, userId, count);
        //5. 本地事务控制
        return count == 1;

    }

}
