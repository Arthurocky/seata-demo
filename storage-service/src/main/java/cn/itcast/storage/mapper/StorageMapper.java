package cn.itcast.storage.mapper;

import cn.itcast.storage.entity.Storage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;


public interface StorageMapper extends BaseMapper<Storage> {
    @Update("update tb_storage set `count` = `count` - #{count} where commodity_code = #{code}")
    int deduct(@Param("code") String commodityCode, @Param("count") int count);
}
