package com.simplon_project.skillhub.skillhub.user.application.port.in;

import com.simplon_project.skillhub.skillhub.user.application.port.in.command.UpdateUserCommand;
import com.simplon_project.skillhub.skillhub.user.domain.model.User;

public interface UpdateUserPort {
    User update(UpdateUserCommand command);
}
