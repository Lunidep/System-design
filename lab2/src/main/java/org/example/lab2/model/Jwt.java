package org.example.lab2.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Jwt implements Serializable {
	private String token;
}
