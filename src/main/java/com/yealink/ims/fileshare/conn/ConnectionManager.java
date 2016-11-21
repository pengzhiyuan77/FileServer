package com.yealink.ims.fileshare.conn;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 文件共享连接管理
 * author:pengzhiyuan
 * Created on:2016/6/1.
 */
public class ConnectionManager {

    private static final ConnectionManager instance = new ConnectionManager();

    public static ConnectionManager getInstance() {
        return instance;
    }

    private ConnectionManager() {
    }

    private AtomicLong atomicLong = new AtomicLong();

    private ConcurrentHashMap<ChannelId, Connection> conns = new ConcurrentHashMap<ChannelId, Connection>();

    public Connection newConnection(ChannelHandlerContext ctx) {
        Connection conn = new Connection(atomicLong.incrementAndGet(), ctx);
        conn.setChannelId(ctx.channel().id());
        return conn;
    }

    public Enumeration<ChannelId> keys() {
        return conns.keys();
    }

    /**
     * 所有连接
     * @return
     */
    public List<ChannelId> listAllConn() {
        Enumeration<ChannelId> keys = conns.keys();
        List<ChannelId> connList = new ArrayList<ChannelId>();
        while (keys.hasMoreElements()) {
            Connection c = conns.get(keys.nextElement());
            if (c != null) {
                connList.add(c.getChannelId());
            }
        }
        return connList;
    }

    /**
     * 获取digest对应的所有所有连接
     * @return
     */
    public List<Connection> listAllConnByDigest(String digest) {
        Enumeration<ChannelId> keys = conns.keys();
        List<Connection> connList = new ArrayList<Connection>();
        while (keys.hasMoreElements()) {
            Connection c = conns.get(keys.nextElement());
            if (c != null && c.getDigest().equals(digest)) {
                connList.add(c);
            }
        }
        return connList;
    }

    /**
     * 增加新连接
     * @param channelId
     * @param conn
     * @return
     */
    public void addConn(ChannelId channelId, Connection conn) {
        conns.put(channelId, conn);
    }

    /**
     * 获取连接
     * @param channelId
     * @return
     */
    public Connection getConn(ChannelId channelId) {
        return conns.get(channelId);
    }

    /**
     * 删除连接
     * @param channelId
     * @return
     */
    public Connection removeConn(ChannelId channelId) {
        return conns.remove(channelId);
    }

    /**
     * 删除连接
     * @param conn
     * @return
     */
    public Connection removeConn(Connection conn) {
        Enumeration<ChannelId> keys = conns.keys();
        while (keys.hasMoreElements()) {
            ChannelId channelId = keys.nextElement();
            Connection v = conns.get(channelId);
            if (conn.equals(v)) {
                return conns.remove(channelId);
            }
        }
        return null;
    }

    /**
     * 关闭所有连接
     */
    public void closeAllConn() {
        Enumeration<ChannelId> keys = conns.keys();
        while (keys.hasMoreElements()) {
            ChannelId key = keys.nextElement();
            Connection v = conns.get(key);
            v.getCtx().pipeline().close();
        }
    }

    /**
     * 关闭所有对应digest的连接
     * 并删除
     * @param digest
     */
    public void closeAllConnByDigest(String digest) {
        Enumeration<ChannelId> keys = conns.keys();
        while (keys.hasMoreElements()) {
            ChannelId key = keys.nextElement();
            Connection v = conns.get(key);
            if (v != null && v.getDigest().equals(digest)) {
                v.getCtx().pipeline().close();
                removeConn(key);
            }
        }
    }

}
