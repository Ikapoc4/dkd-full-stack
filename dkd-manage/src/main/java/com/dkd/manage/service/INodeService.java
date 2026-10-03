package com.dkd.manage.service;

import com.dkd.manage.domain.Node;

import java.util.List;

public interface INodeService {

    List<Node> findAllNodes();

    Node findNodeById(Long nodeId);

    int addNode(Node node);

    int updateNode(Node node);

    int deleteNodes(Long[] nodeIds);

}
