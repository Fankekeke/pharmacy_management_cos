package cc.mrbird.febs.cos.entity;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 药品名称记录
 *
 * @author FanK
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DrugRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 药品现名
     */
    private String drugName;

    /**
     * 图片
     */
    private String images;

    /**
     * 曾用名
     */
    private String formerName;

    /**
     * 添加员工ID
     */
    private Integer staffId;

    /**
     * 备注
     */
    private String content;

    /**
     * 创建时间
     */
    private String createDate;


}
