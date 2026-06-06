package com.inventra.config;

import com.inventra.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;

    @AfterReturning("execution(* com.inventra.service.ProductService.createProduct(..))")
    public void logProductCreation(JoinPoint joinPoint) {
        log("CREATE_PRODUCT");
    }

    @AfterReturning("execution(* com.inventra.service.ProductService.updateProduct(..))")
    public void logProductUpdate(JoinPoint joinPoint) {
        log("UPDATE_PRODUCT");
    }

    @AfterReturning("execution(* com.inventra.service.ProductService.deleteProduct(..))")
    public void logProductDeletion(JoinPoint joinPoint) {
        log("DELETE_PRODUCT");
    }

    @AfterReturning("execution(* com.inventra.service.InventoryService.stockIn(..))")
    public void logStockIn(JoinPoint joinPoint) {
        log("STOCK_IN");
    }

    @AfterReturning("execution(* com.inventra.service.InventoryService.stockOut(..))")
    public void logStockOut(JoinPoint joinPoint) {
        log("STOCK_OUT");
    }

    private void log(String action) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            auditLogService.log(auth.getName(), action);
        }
    }
}
