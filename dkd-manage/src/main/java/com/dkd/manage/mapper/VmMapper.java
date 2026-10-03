package com.dkd.manage.mapper;

import com.dkd.manage.domain.VM;

import java.util.List;

public interface VmMapper {
    List<VM> findAllVM();

    VM findVMById(Long vmId);

    int add(VM vm);

    int update(VM vm);

    int delete(Long[] vmIds);

    VM selectVMByCode(String vmCode);
}
