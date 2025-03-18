package com.cq.YunPhoto.Manager.WebSocket.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;


/**
 * Disruptor生产者
 */
@Component
public class PictureEditEventProducer {

    @Resource
    private Disruptor<PictureEditEvent> disruptor;


    /**
     * 发布事件
     * @param event
     */
    public void publish(PictureEditEvent event){
        //获取环形队列序号
        RingBuffer<PictureEditEvent> ringBuffer = disruptor.getRingBuffer();
        long next = ringBuffer.next();
        //填充事件
        PictureEditEvent pictureEditEvent = disruptor.get(next);
        pictureEditEvent.setPictureId(event.getPictureId());
        pictureEditEvent.setUser(event.getUser());
        pictureEditEvent.setPictureEditRequestMessage(event.getPictureEditRequestMessage());
        pictureEditEvent.setSession(event.getSession());
        //发布事件
        ringBuffer.publish(next);
    }

    /**
     * 优雅停机
     */
    @PreDestroy
    public void close() {
        disruptor.shutdown();
    }
}
