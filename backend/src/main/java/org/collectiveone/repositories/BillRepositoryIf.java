package org.collectiveone.repositories;

import java.util.UUID;

import org.collectiveone.model.support.Bill;
import org.springframework.data.repository.CrudRepository;

public interface BillRepositoryIf extends CrudRepository<Bill, UUID> {

}
