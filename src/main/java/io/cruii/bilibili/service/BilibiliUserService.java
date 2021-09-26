package io.cruii.bilibili.service;

import io.cruii.bilibili.vo.BilibiliUserVO;

import java.util.List;

/**
 * @author cruii
 * Created on 2021/9/22
 */
public interface BilibiliUserService {
    void save(String dedeuserid, String sessdata, String biliJct);

    boolean isExist(String dedeuserid);

    List<BilibiliUserVO> list();
}
