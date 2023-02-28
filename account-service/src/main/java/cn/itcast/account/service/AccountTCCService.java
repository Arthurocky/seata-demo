package cn.itcast.account.service;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;


/**
 * @author Arthurocky
 */
@LocalTCC
public interface AccountTCCService {

    /**
     * 用户账户扣款
     * 1 扣减account表可用金额
     * 2 记录冻结金额和事务状态到account_freeze表
     * @param userId
     * @param money
     *
     */
    @TwoPhaseBusinessAction(name = "deduct",
        commitMethod = "confirm",rollbackMethod = "cancel")
    void deduct(@BusinessActionContextParameter(paramName = "userId") String userId,
                @BusinessActionContextParameter(paramName = "money") int money);

    /**
     * 确认业务，根据xid删除account_freeze表的冻结记录
     * @param ctx
     * @return
     */
    boolean confirm(BusinessActionContext ctx);

    /**
     * 撤消回滚，需要把冻结金额还原
     * 1 修改account_freeze表，冻结金额为0，state为2
     * 2 修改account表，恢复可用金额
     * @param ctx
     * @return
     */
    boolean cancel(BusinessActionContext ctx);
}
