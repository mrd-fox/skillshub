package com.simplon_project.skillhub.skillhub.user.application.port.in;

import com.simplon_project.skillhub.skillhub.user.application.port.in.command.GetUserByExternalIdCommand;
import com.simplon_project.skillhub.skillhub.user.application.port.in.dto.GetUserByExternalIdResult;

public interface GetUserByExternalIdPort {
    GetUserByExternalIdResult getUserByExternalId(GetUserByExternalIdCommand command);
}
