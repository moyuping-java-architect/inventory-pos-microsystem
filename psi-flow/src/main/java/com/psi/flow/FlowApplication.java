package com.psi.flow;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@ComponentScan(basePackages = {"com.psi.flow", "com.psi.order", "com.psi.common.async.facade", "com.psi.common.exception"})
public class FlowApplication {
}