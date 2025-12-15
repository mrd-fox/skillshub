package com.simplon_project.skillhub.skillhub.user.application.port.in;

import com.simplon_project.skillhub.skillhub.user.application.port.in.command.GetUserByExternalIdCommand;
import com.simplon_project.skillhub.skillhub.user.application.port.in.command.GetUserByIdCommand;
import com.simplon_project.skillhub.skillhub.user.domain.model.User;

public interface GetUserByExternalIdPort {
    User getUserByExternalId(GetUserByExternalIdCommand command);
}
