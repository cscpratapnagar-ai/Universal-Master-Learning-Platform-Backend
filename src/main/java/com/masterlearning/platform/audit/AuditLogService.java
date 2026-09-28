package com.masterlearning.platform.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditLogService {
    private final AuditLogRepository repository;
    public AuditLogService(AuditLogRepository repository){this.repository=repository;}

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void success(UUID actorUserId,String action,String resourceType,String resourceId,String details){
        repository.save(new AuditLog(actorUserId,action,resourceType,resourceId,"SUCCESS",details,null));
    }

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void failure(UUID actorUserId,String action,String resourceType,String resourceId,String details){
        repository.save(new AuditLog(actorUserId,action,resourceType,resourceId,"FAILURE",details,null));
    }

    @Transactional(readOnly=true)
    public java.util.List<AuditLog> latest(){return repository.findTop100ByOrderByCreatedAtDesc();}
}