package com.oreilly.springdata.neo4j.core;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * @author mh
 * @since 02.06.12
 */
public class EmailAddressConverter implements GenericConverter {
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return new HashSet<ConvertiblePair>(asList(new ConvertiblePair(EmailAddress.class,String.class), new ConvertiblePair(String.class, EmailAddress.class)));
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source instanceof EmailAddress) {
            return ((EmailAddress) source).getEmail();
        }
        if (source instanceof String) {
            return new EmailAddress((String)source);
        }
        throw new ConversionFailedException(sourceType,targetType,source,null);
    }
}
