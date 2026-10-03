package com.dkd.manage.service.Impl;

import com.dkd.manage.domain.Node;
import com.dkd.manage.mapper.NodeMapper;
import com.dkd.manage.service.INodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NodeServiceImpl implements INodeService {

    @Autowired
    private NodeMapper nodeMapper;

    @Override
    public List<Node> findAllNodes() {
        return nodeMapper.selectNodeList();
    }

    @Override
    public Node findNodeById(Long nodeId) {
        return nodeMapper.selectNodeById(nodeId);
    }

    @Override
    public int addNode(Node node) {
        return nodeMapper.insertNode(node);
    }

    @Override
    public int updateNode(Node node) {
        return nodeMapper.updateNode(node);
    }


    @Override
    public int deleteNodes(Long[] nodeIds) {
        return nodeMapper.deleteNodeByIds(nodeIds);
    }
}
