package com.dkd.manage.mapper;

import com.dkd.manage.domain.Node;

import java.util.List;

public interface NodeMapper {
    /**
     * 查询点位列表
     */
    public List<Node> selectNodeList();

    /**
     *
     * 根据点位Id查询点位
     * @return
     */
    public Node selectNodeById(Long nodeId);

    /**
     * 新增点位
     */
    public int insertNode(Node node);

    /**
     *
     * 修改点位
     */
    public int updateNode(Node node);



    /**
     *
     * 批量删除点位
     * @return
     */
    public int deleteNodeByIds(Long[] nodeId);
}
