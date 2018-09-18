package com.demo.entity;
import org.springframework.data.couchbase.repository.CouchbaseRepository;

public interface LockRepository extends CouchbaseRepository<Lock,String> {
    Lock findFirstByName(String name);
}

