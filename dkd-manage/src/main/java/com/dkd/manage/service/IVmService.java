package com.dkd.manage.service;

import com.dkd.manage.domain.VM;

import java.util.List;

public interface IVmService {
    List<VM> findAllVM();

    VM findVMById(Long vmId);

    int add(VM vm);

    int update(VM vm);

    int delete(Long[] vmIds);

    boolean checkVmCodeUnique(VM vm) ;


}
