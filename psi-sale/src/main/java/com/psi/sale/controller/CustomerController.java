package com.psi.sale.controller;

import com.psi.sale.dto.CustomerDTO;
import com.psi.sale.dto.CustomerQueryDTO;
import com.psi.sale.dto.CustomerSaveDTO;
import com.psi.sale.mq.producer.CustomerSyncProducer;
import com.psi.sale.service.CustomerService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/sale/customer")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerSyncProducer customerSyncProducer;

    public CustomerController(CustomerService customerService, CustomerSyncProducer customerSyncProducer) {
        this.customerService = customerService;
        this.customerSyncProducer = customerSyncProducer;
    }

    @GetMapping("/{id}")
    public CommonResult<CustomerDTO> getById(@PathVariable Long id) {
        return customerService.getById(id);
    }

    @PostMapping("/list")
    public CommonResult<PageResult<CustomerDTO>> list(@RequestBody CustomerQueryDTO queryDTO) {
        return CommonResult.success(customerService.list(queryDTO));
    }

    @PostMapping
    public CommonResult<CustomerDTO> save(@RequestBody CustomerSaveDTO saveDTO) {
        return customerService.save(saveDTO);
    }

    @PutMapping("/{id}")
    public CommonResult<CustomerDTO> update(@PathVariable Long id, @RequestBody CustomerSaveDTO saveDTO) {
        return customerService.update(id, saveDTO);
    }

    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return customerService.delete(id);
    }

    @PutMapping("/{id}/status/{status}")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        return customerService.updateStatus(id, status);
    }

    /**
     * 手动触发客户数据上传
     * 将客户数据全量同步到中间微服务（psi-sync）
     */
    @PostMapping("/upload")
    public CommonResult<String> uploadData() {
        customerSyncProducer.syncAllAsync();
        return CommonResult.success("客户数据上传任务已触发，请稍后查看日志确认上传结果");
    }
}