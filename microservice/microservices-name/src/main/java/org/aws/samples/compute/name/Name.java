package org.aws.samples.compute.name;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Arun Gupta
 */
@XmlRootElement
public class Name implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    
    private String name;
    
    public Name() { }
    
    public Name(String name) {
        this.name = name;
    }

    public Name(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
