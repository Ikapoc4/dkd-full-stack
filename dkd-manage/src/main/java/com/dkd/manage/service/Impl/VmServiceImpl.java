package com.dkd.manage.service.Impl;

import com.dkd.common.exception.ServiceException;
import com.dkd.manage.domain.VM;
import com.dkd.manage.mapper.VmMapper;
import com.dkd.manage.service.IVmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 设备 服务层处理
 *
 * @author dkd
 */
@Service
public class VmServiceImpl implements IVmService {

    @Autowired
    private VmMapper vmMapper;

    @Override
    public List<VM> findAllVM() {
        return vmMapper.findAllVM();
    }

    @Override
    public VM findVMById(Long vmId) {
        return vmMapper.findVMById(vmId);
    }

    @Override
    public int add(VM vm) {
        if (!checkVmCodeUnique(vm)) {
            throw new ServiceException("新增设备" + vm.getVmCode() + "失败，设备编号已存在");
        }
        return vmMapper.add(vm);
    }

    @Override
    public int update(VM vm) {
        if (!checkVmCodeUnique(vm)) {
            throw new ServiceException("修改设备" + vm.getVmCode() + "失败，设备编号已存在");
        }
        return vmMapper.update(vm);
    }

    @Override
    public int delete(Long[] vmIds) {
        return vmMapper.delete(vmIds);
    }

    @Override
    public boolean checkVmCodeUnique(VM vm) {
        // 新增时 vm.getVmId() 是 null（自增主键前端不传），用 -1L 顶替
        Long vmId = (vm.getVmId() == null) ? -1L : vm.getVmId();

        // 去数据库查：这个设备编号有没有被用过
        VM info = vmMapper.selectVMByCode(vm.getVmCode());

        // ★ 这里必须用上面算好的 vmId，不能再用 vm.getVmId()
        //   否则新增时 vm.getVmId() 是 null → vm.getVmId().longValue() 直接空指针
        if (info != null && info.getVmId().longValue() != vmId.longValue()) {
            return false;   // 编号被"别人"占了 —— NOT_UNIQUE
        }
        return true;        // 没被占，或占的就是我自己 —— UNIQUE
    }
}
