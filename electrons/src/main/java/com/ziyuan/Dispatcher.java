package com.ziyuan;

import com.ziyuan.channel.Channel;
import com.ziyuan.events.Electron;
import com.ziyuan.events.ElectronsWrapper;
import com.ziyuan.events.ListenerCollectWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dispatcher 分发器，用来把任务放到各个channel中
 *
 * @author ziyuan
 * @since 2017-03-08
 */
public final class Dispatcher {

    /**
     * 是否已经开始了
     */
    private AtomicBoolean started = new AtomicBoolean(false);

    /**
     * 通道
     */
    private Map<String, Channel> channelMap = new HashMap<>();

    /**
     * 针对有after逻辑的特殊管道
     */
    private static final String SPEC_CHANNEL_PREFIX = "spec_channel:";

    /**
     * normal channel的key
     */
    private static final String NORMAL_CHANNEL_KEY = "normal_channel:";

    /**
     * config
     */
    private Config conf;

    /**
     * 线程池
     */
    private ExecutorService pool;

    /**
     * key 事件的包装类  value 该事件的监听器包装类
     */
    private Map<ElectronsWrapper, ListenerCollectWrapper> wrapperMap;

    public void start() {
        if (started.get()) {
            return;
        }
        started.set(true);
    }

    public void stop() {

    }

    public Dispatcher(Map<ElectronsWrapper, ListenerCollectWrapper> wrapperMap, Config config) {
        this.conf = config;
        this.wrapperMap = wrapperMap;
        //初始化pool
        pool = Executors.newFixedThreadPool(conf.getCircuitNum(), new ThreadFactory() {

            final AtomicInteger cursor = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "Electrons Thread : thread" + cursor.incrementAndGet());
            }
        });

    }

    /**
     * 根据事件找到一个channel
     *
     * @param wrapper wrapper
     * @return 根据wrapper选中的channel
     */
    private Channel selectOne(ElectronsWrapper wrapper) {
        return null;
    }

    /**
     * 分发
     *
     * @param electron 电子
     * @param tag      tag
     * @param sync     是否同步
     */
    public void dispatch(String tag, Electron electron, boolean sync) {
    }
}