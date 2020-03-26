package com.may.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

@Data
@NoArgsConstructor
@TableName("tb_test")
public class TBTest {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String clo1;
    private String clo2;
    private String clo3;
    private String clo4;
    private String clo5;
    private String clo6;
    private String clo7;
    private String clo8;
    private String clo9;
    private String clo10;
    private String clo11;
    private String clo12;
    private String clo13;
    private String clo14;
    private String clo15;
    private String clo16;
    private String clo17;
    private String clo18;
    private String clo19;
    private String clo20;
    private String clo21;
    private String clo22;
    private String clo23;
    private String clo24;
    private String clo25;
    private String clo26;
    private String clo27;
    private String clo28;
    private String clo29;
    private String clo30;
}
