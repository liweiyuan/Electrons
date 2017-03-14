package com.ziyuan.channel;

import com.google.common.util.concurrent.RateLimiter;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.ziyuan.ElectronsHolder;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * AbstractChannel
 *
 * @author ziyuan
 * @since 2017-03-08
 */
public abstract class AbstractChannel implements Channel {

    /**
     * logger
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractChannel.class);

    /**
     * 限流
     */
    private RateLimiter rateLimiter;

    /**
     * 是否开启限速
     */
    @Getter
    private boolean limitRate;

    /**
     * 开启
     */
    protected volatile boolean opened;

    /**
     * 真正的电路
     */
    protected RingBuffer<ElectronsHolder> buffer;

    public boolean publish(ElectronsHolder electronsHolder) throws Exception {
        if (!opened) {
            return false;
        }
        //如果没有配置限速，直接返回
        if (!limitRate) {
            return false;
        }
        if (this.rateLimiter == null) {
            return false;
        }
        int weight = electronsHolder.getElectron().getWeight();
        //等待
        rateLimiter.acquire(weight);
        long next = buffer.tryNext();
        //the remaining capacity of the buffer < the size of the buffer * 0.2 日志输出提示告警
        if (buffer.remainingCapacity() < buffer.getBufferSize() * 0.2) {
            LOGGER.warn("commandBus consume warn message, remainingCapacity size:" + buffer.remainingCapacity() + ",conRingBuffer size:" + buffer.getBufferSize());
        }
        ElectronsHolder eh = buffer.get(next);
        eh.setElectron(electronsHolder.getElectron());
        buffer.publish(next);
        return true;
    }

    @Override
    public void open(Disruptor<ElectronsHolder> disruptor) {
        synchronized (this) {
            buffer = disruptor.start();
        }
    }

    @Override
    public void close() {
        if (!this.opened) {
            return;
        }
        synchronized (this) {
            if (this.opened) {
                this.opened = false;
            }
        }
    }

    @Override
    public void confLimitRate(boolean limitRate, double perSecond, boolean warmup, int warmupPeriod, TimeUnit unit) {
        if (limitRate) {
            this.limitRate = true;
            if (warmup) {
                this.rateLimiter = RateLimiter.create(perSecond, warmupPeriod, unit);
            } else {
                this.rateLimiter = RateLimiter.create(perSecond);
            }
        } else {
            this.limitRate = false;
        }
    }
}