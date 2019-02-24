package com.oreilly.springdata.neo4j.core;

import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author mh
 * @since 01.07.12
 */
@NodeEntity
public class Category extends AbstractEntity implements Comparable<Category> {
    @Indexed(unique = true) String name;
    @Fetch // loads all children eagerly (cascading!)
    @RelatedTo(type="SUB_CAT")
    Set<Category> children = new TreeSet<Category>();
    
    public void addChild(Category cat) {
        this.children.add(cat);
    }

    @Override
    public int compareTo(Category o) {
        return name.compareTo(o.name);
    }
}
