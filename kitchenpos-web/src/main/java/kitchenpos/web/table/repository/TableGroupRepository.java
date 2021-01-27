package kitchenpos.web.table.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kitchenpos.domain.table.domain.TableGroup;

public interface TableGroupRepository extends JpaRepository<TableGroup, Long> {
}
