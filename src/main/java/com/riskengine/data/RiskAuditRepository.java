package com.riskengine.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.riskengine.data.jpa.RiskAuditEntity;

@Repository
public interface RiskAuditRepository extends JpaRepository<RiskAuditEntity, java.util.UUID> {
}