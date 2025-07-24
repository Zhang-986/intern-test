package com.example.zzk;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.example.zzk.model.DTO;
import com.example.zzk.model.Po;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@Slf4j
@SpringBootTest
class JsonTestApplicationTests {
    // List转String
    @Test
    void convertListToString() {
        // 准备测试数据
        List<String> groups = Arrays.asList("销售小组", "测试小组", "商务小组");
        DTO dto = new DTO();
        dto.setInfo(groups);

        Po vo = new Po();

        // 使用Spring BeanUtils复制 bean属性
        System.out.println("=== Spring BeanUtils测试 ===");
        BeanUtils.copyProperties(dto, vo);
        System.out.println("结果：" + vo.getInfo());

        // 使用Hutool BeanUtil
        System.out.println("=== Hutool BeanUtil测试 ===");
        Po vo2 = new Po();
        BeanUtil.copyProperties(dto, vo2);
        System.out.println("结果：" + vo2.getInfo());
    }

    @Test
    void convertStringToList() {
        // String转List测试
        Po sourceVo = new Po();
        sourceVo.setInfo("[\"销售小组\",\"测试小组\",\"商务小组\"]");

        DTO targetDto = new DTO();

        // Spring BeanUtils
        BeanUtils.copyProperties(sourceVo, targetDto);
        System.out.println("Spring结果：" + targetDto.getInfo());

        // Hutool BeanUtil
        DTO targetDto2 = new DTO();
        BeanUtil.copyProperties(sourceVo, targetDto2);
        System.out.println("Hutool结果：" + targetDto2.getInfo());
    }
    @Test
    // 推荐：使用JSON工具显式转换
    public void safeCopyWithConvert() {
        DTO dto = new DTO();
        dto.setInfo(Arrays.asList("销售小组", "测试小组", "商务小组"));
        String jsonString = JSON.toJSONString(dto.getInfo());

        // DB交互
        Po vo = new Po();
        vo.setInfo(jsonString);
        System.out.println("转换后的JSON字符串：" + vo.getInfo());
        List<String> strings = JSON.parseArray(vo.getInfo(), String.class);
        System.out.println("转换后的List：" + strings);
    }
}
