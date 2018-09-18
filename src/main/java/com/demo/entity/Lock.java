package com.demo.entity;

import com.couchbase.client.java.repository.annotation.Field;
import com.couchbase.client.java.repository.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.couchbase.core.mapping.Document;


@Data
@Document
@AllArgsConstructor
public class Lock {
    @Id
    String _id;
    @Field
    String name;
    @Field
    String lockedUntil;
    @Field
    String lockedAt;
    @Field
    String lockedBy;
}
