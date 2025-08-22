package com.example.zzk;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.example.zzk.feign.GoFeign;
import com.example.zzk.model.DTO;
import com.example.zzk.model.Po;
import com.example.zzk.result.ApiResponse;
import com.example.zzk.model.InfoAddVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@SpringBootTest
class JsonTestApplicationTests {
    @Autowired
    private GoFeign goFeign;

    @Test
    public void testJsonConversion() {
        List<String> list = new ArrayList<>();

        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list.add("5");
        ApiResponse<String> goApi = goFeign.getGoApi(list);
        System.out.println("goApi = " + goApi);
        System.out.println("goApi.getData() = " + goApi.getData());

    }

    @Test
    public void testPostRequest(){
        InfoAddVo info = new InfoAddVo();
        info.setName("John Doe");
        info.setAge(30);
        info.setPhone("123-456-7890");
        info.setSex(1);

        ApiResponse<InfoAddVo> response = goFeign.postInfo(info);
        System.out.println("Response Data: " + response);
        System.out.println("Response Message: " + response.getData());
    }

    @Test
    public void testSSolveNumber(){
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        ApiResponse<Integer> response = goFeign.solveNumber(numbers);
        System.out.println("Response Data: " + response);
        System.out.println("Response Message: " + response.getData());
    }
}
