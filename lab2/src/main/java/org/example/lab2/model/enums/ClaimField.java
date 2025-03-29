package org.example.lab2.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ClaimField implements DictionaryEnum {
	USERNAME("username"),
	ROLE("role"),
	USER_ID("user_id");

	private final String name;
}
