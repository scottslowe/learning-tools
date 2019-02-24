package com.oreilly.springdata.roo.web;

import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.roo.addon.web.mvc.controller.converter.RooConversionService;

import com.oreilly.springdata.roo.domain.EmailAddress;

/**
 * A central place to register application converters and formatters. 
 */
@RooConversionService
public class ApplicationConversionServiceFactoryBean extends FormattingConversionServiceFactoryBean {

	@Override
	protected void installFormatters(FormatterRegistry registry) {
		super.installFormatters(registry);
		// Register application converters and formatters
		registry.addConverter(getStringToEmailAddressConverter());
		registry.addConverter(getEmailAddressConverterToString());
	}

	public Converter<String, EmailAddress> getStringToEmailAddressConverter() {
		return new Converter<String, EmailAddress>() {
			@Override
			public EmailAddress convert(String source) {
				EmailAddress emailAddress = new EmailAddress();
				emailAddress.setAddress(source);
				return emailAddress;
			}
		};
	}

	public Converter<EmailAddress, String> getEmailAddressConverterToString() {
		return new Converter<EmailAddress, String>() {
			@Override
			public String convert(EmailAddress source) {
				return source.getAddress();
			}
		};
	}
}
