package io.cruii.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.cruii.pojo.dto.BiliTaskUserDTO;
import io.cruii.pojo.vo.BiliTaskUserVO;
import io.cruii.pojo.vo.OmhUserVO;

import java.util.List;

/**
 * @author cruii
 * Created on 2021/9/22
 */
public interface BilibiliUserService {
    void save(String dedeuserid, String sessdata, String biliJct);

    void save(BiliTaskUserDTO user);

    boolean isExist(String dedeuserid);

    Page<BiliTaskUserVO> list(Integer page, Integer size);

    List<String> listNotRunUserId();

    void delete(String dedeuserid);

    OmhUserVO getUser(String dedeuserid, String sessdata, String biliJct);
}
