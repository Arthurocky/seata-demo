package cn.itcast.storage.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


@Data
@TableName("tb_storage")
public class Storage {
    @TableId
    private Long id;
    private String commodityCode;
    private Integer count;
}
