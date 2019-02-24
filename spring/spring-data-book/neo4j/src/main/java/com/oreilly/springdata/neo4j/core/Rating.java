package com.oreilly.springdata.neo4j.core;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

/**
 * @author mh
 * @since 02.07.12
 */
@RelationshipEntity(type = "RATED")
public class Rating extends AbstractEntity {
    @StartNode
    private Customer customer;
    @EndNode
    private Product product;
    private int stars;
    private String comment;

    public Rating() {
    }

    public Rating(Customer customer, Product product, int stars, String comment) {
        this.customer = customer;
        this.product = product;
        this.stars = stars;
        this.comment = comment;
    }
}
