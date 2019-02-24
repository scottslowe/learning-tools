package com.oreilly.springdata.jdbc.domain;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QCustomer is a Querydsl query type for QCustomer
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QCustomer extends com.mysema.query.sql.RelationalPathBase<QCustomer> {

    private static final long serialVersionUID = -325328342;

    public static final QCustomer customer = new QCustomer("CUSTOMER");

    public final StringPath emailAddress = createString("EMAIL_ADDRESS");

    public final StringPath firstName = createString("FIRST_NAME");

    public final NumberPath<Long> id = createNumber("ID", Long.class);

    public final StringPath lastName = createString("LAST_NAME");

    public final com.mysema.query.sql.PrimaryKey<QCustomer> sysPk10026 = createPrimaryKey(id);

    public final com.mysema.query.sql.ForeignKey<QAddress> _addressCustomerRef = createInvForeignKey(id, "CUSTOMER_ID");

    public QCustomer(String variable) {
        super(QCustomer.class, forVariable(variable), "PUBLIC", "CUSTOMER");
    }

    @SuppressWarnings("all")
    public QCustomer(Path<? extends QCustomer> path) {
        super((Class)path.getType(), path.getMetadata(), "PUBLIC", "CUSTOMER");
    }

    public QCustomer(PathMetadata<?> metadata) {
        super(QCustomer.class, metadata, "PUBLIC", "CUSTOMER");
    }

}

