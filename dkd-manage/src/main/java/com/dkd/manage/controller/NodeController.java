package com.dkd.manage.controller;

import com.dkd.common.annotation.Log;
import com.dkd.common.core.controller.BaseController;
import com.dkd.common.core.domain.AjaxResult;
import com.dkd.common.core.page.TableDataInfo;
import com.dkd.common.enums.BusinessType;
import com.dkd.manage.domain.Node;
import com.dkd.manage.service.INodeService;
import org.aspectj.weaver.loadtime.Aj;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.dkd.common.utils.PageUtils.startPage;



@RestController
@RequestMapping("/manage/node")
public class NodeController extends BaseController {

    @Autowired
    private INodeService nodeService;

    @GetMapping("/nodeList")
    public TableDataInfo selectNodeList() {
        startPage();
        List<Node> list = nodeService.findAllNodes();
        return getDataTable(list);

    }

    @PreAuthorize("@ss.hasPermi('manage:node:query')")
    @GetMapping("/{nodeId}")
    public AjaxResult getInfo(@PathVariable Long nodeId) {
        return success(nodeService.findNodeById(nodeId));
    }

    @PreAuthorize("@ss.hasPermi('manage:node:add')")
    @Log(title = "点位管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Node node) {
        node.setCreateBy(getUsername());
        return toAjax(nodeService.addNode(node));
    }

    // ★ 订正④：同样，返回 AjaxResult：
    //     public AjaxResult edit(@Validated @RequestBody Node node) {
    //         node.setUpdateBy(getUsername());
    //         return toAjax(nodeService.updateNode(node));
    //     }
    @PreAuthorize("@ss.hasPermi('manage:node:edit')")
    @Log(title = "点位管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult updateNode(@Validated @RequestBody Node node) {
        node.setUpdateBy(getUsername());
        return toAjax(nodeService.updateNode(node));
    }

    @PreAuthorize("@ss.hasPermi('manage:node:remove')")
    @Log(title = "点位管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/delete/{nodeIds}")
    public AjaxResult deleteNodes(@PathVariable Long[] nodeIds) {
        return toAjax(nodeService.deleteNodes(nodeIds));
    }

}
