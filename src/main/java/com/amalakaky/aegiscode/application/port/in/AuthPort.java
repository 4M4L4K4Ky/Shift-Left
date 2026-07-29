package com.amalakaky.aegiscode.application.port.in;

import com.amalakaky.aegiscode.domain.model.JwtToken;
import com.amalakaky.aegiscode.domain.model.User;

public interface AuthPort {

  JwtToken login(String username, String password);

  User register(String username, String password, String scopes);
}
