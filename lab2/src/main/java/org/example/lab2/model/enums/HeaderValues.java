package org.example.lab2.model.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum HeaderValues implements DictionaryEnum {
	AUTHORIZATION("Authorization"),
	BEARER("Bearer");

	private final String name;
}