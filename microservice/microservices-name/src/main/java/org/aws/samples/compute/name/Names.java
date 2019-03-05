package org.aws.samples.compute.name;

import java.util.Arrays;
import java.util.List;

public class Names {
    private static List<Name> names = Arrays.asList(new Name(1, "Penny"),
            new Name(2, "Sheldon"),
            new Name(3, "Leonard"),
            new Name(4, "Howard"),
            new Name(5, "Raj"),
            new Name(6, "Amy"),
            new Name(7, "Bernadette")
            );

    public static Name[] findAll() {
        return (Name[])names.toArray();
    }

    public static Name findById(int id) {
        return names.get(id);
    }
}
