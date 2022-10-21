package io.cruii.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.cruii.pojo.dto.BilibiliUserDTO;
import io.cruii.pojo.vo.BilibiliUserVO;

import java.util.List;

/**
 * @author cruii
 * Created on 2021/9/22
 */
public interface BilibiliUserService {
    void save(String dedeuserid, String sessdata, String biliJct);

    void save(BilibiliUserDTO user);

    boolean isExist(String dedeuserid);

    Page<BilibiliUserVO> list(Integer page, Integer size);

    List<String> listNotRunUserId();
}
