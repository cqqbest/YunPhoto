package com.cq.YunPhoto.Manager.WebSocket.disruptor;


import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.lmax.disruptor.dsl.Disruptor;
import jdk.nashorn.internal.ir.annotations.Reference;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * disruptor配置
 */

@Configuration
public class PictureEditEventDisruptorConfig {
    @Resource
    private PictureEditEventWorkHandler pictureEditEventWorkHandler;

    @Bean//("pictureEditEventDisruptor")
    public Disruptor<PictureEditEvent> disruptor() {

        //指定RingBuffer大小,必须为2的幂次方
        int bufferSize = 1024*256;
        //创建Disruptor
        Disruptor<PictureEditEvent> disruptor = new Disruptor<>(PictureEditEvent::new, bufferSize,
                ThreadFactoryBuilder.create().setNamePrefix("pictureEditEventDisruptor").build()
        );
        //设置消费者
        disruptor.handleEventsWith(pictureEditEventWorkHandler);

        //启动
        disruptor.start();
        return disruptor;
    }
}
